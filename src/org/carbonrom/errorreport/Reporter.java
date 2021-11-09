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

import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

import com.raygun.raygun4android.RaygunClient;

import org.carbonrom.errorreport.ErrorReportActivity;

// class for holding functions related to sending an exception to Raygun
public class Reporter {

    public static final String TAG = Reporter.class.getSimpleName();

    public static void report(Context context, ApplicationErrorReport errorReport) {
        if (errorReport == null) {
            return;
        }

        ApplicationErrorReport.CrashInfo crashInfo = errorReport.crashInfo;

        Resources res = context.getResources();

        long crashTime = errorReport.time;
        String crashPackageName = errorReport.packageName;
        String crashProcessName = errorReport.processName;
        String device = SystemProperties.get(res.getString(R.string.property_device));

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

        RaygunClient.send(crashInfo.exception, tags, customData);
    }

    private static String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return new BigInteger(1, md.digest(input.getBytes())).toString(16).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUniqueID(Context context) {
        final String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return digest(context.getPackageName() + id);
    }
}