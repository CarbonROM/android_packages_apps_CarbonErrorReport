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

import android.app.Application;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.Log;

import com.raygun.raygun4android.RaygunClient;
import com.raygun.raygun4android.messages.shared.RaygunUserInfo;

import org.carbonrom.errorreport.Reporter;

public class ErrorReportApp extends Application {

    public static final String TAG = ErrorReportApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Resources res = this.getResources();

        Log.d(TAG, "Initing raygun");

        RaygunClient.init(this);
    
        String version = SystemProperties.get(res.getString(R.string.property_version));
        Log.d(TAG, "Raygun setVersion: " + version);
        RaygunClient.setVersion(version);

        String reportID = Reporter.getUniqueID(this);
        RaygunUserInfo user = new RaygunUserInfo(reportID);
        Log.d(TAG, "Raygun setUser: " + reportID);
        RaygunClient.setUser(user);

        // Might as well report FCs first-class. We can reuse this in other 
        // apps that could use more detailed instrumentation
        RaygunClient.enableCrashReporting();

        Log.i(TAG, "Raygun enabled");
    }
}
