/*
 * Copyright (C) 2021 Jacob McSwain
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.carbonrom.errorreport;

import android.annotation.Nullable;
import android.app.Activity;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.StringBuilderPrinter;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.ArrayList;

import com.raygun.raygun4android.CrashReportingOnBeforeSend;
import com.raygun.raygun4android.RaygunClient;
import com.raygun.raygun4android.messages.crashreporting.RaygunMessage;
import com.raygun.raygun4android.messages.shared.RaygunUserInfo;

public class ErrorHandler extends Activity {
    private static final String TAG = ErrorHandler.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        
        Exception exception = (Exception) getIntent().getExtras().getSerializable("system_crash_exception");
        long crashTime = getIntent().getExtras().getLong("system_crash_exception_time");
        String crashPackageName = getIntent().getExtras().getString("system_crash_exception_package");
        String crashProcessName = getIntent().getExtras().getString("system_crash_exception_process");

        ApplicationErrorReport.CrashInfo crashInfo = new ApplicationErrorReport.ParcelableCrashInfo(exception);

        if (crashInfo == null) {
            finish();
            return;
        }

        Resources res = ctx.getResources();

        RaygunClient.init(this.getApplication());
        RaygunClient.setOnBeforeSend(new BeforeSendImplementation());
        RaygunClient.setVersion(SystemProperties.get(res.getString(R.string.property_version)));
        String device = SystemProperties.get(res.getString(R.string.property_device));

        RaygunUserInfo user = new RaygunUserInfo(getUniqueID(ctx));
        RaygunClient.setUser(user);

        // Might as well report FCs first-class. We can reuse this in other 
        // apps that could use more detailed instrumentation
        RaygunClient.enableCrashReporting();

        Log.i(TAG, "Process " + crashProcessName + " in package " + crashPackageName + " crashed at " + crashTime);

        ArrayList<String> tags = new ArrayList<String>();
        HashMap<String, String> customData = new HashMap<String, String>();
        customData.put("process_name", crashProcessName);
        customData.put("package_name", crashPackageName);
        customData.put("real_crash_time", "" + crashTime);
        customData.put("exception_class", crashInfo.exceptionClassName);
        customData.put("errored_class_name", crashInfo.throwClassName);
        customData.put("errored_method_name", crashInfo.throwMethodName);
        customData.put("errored_file_name", crashInfo.throwFileName);
        customData.put("errored_file_line", "" + crashInfo.throwLineNumber);
        customData.put("device", device);

        // We will send the error details as soon as the activity starts and 
        // allow the user to add more details if they elect too. We want to avoid
        // the user seeing text boxes and immediatly backing out because they
        // don't want to put any effort in (the text boxes will be/are optional)
        RaygunClient.send(exception, tags, customData);
    }

    private class BeforeSendImplementation implements CrashReportingOnBeforeSend {
        @Override
        public RaygunMessage onBeforeSend(RaygunMessage message) {
            Log.i(TAG, "About to post to Raygun, returning the payload as is...");
            return message;
        }
    }

    public static String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return new BigInteger(1, md.digest(input.getBytes())).toString(16).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

    private static String getUniqueID(Context context) {
        final String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return digest(context.getPackageName() + id);
    }
}
