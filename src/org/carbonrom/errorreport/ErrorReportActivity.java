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

import android.app.Activity;
import android.app.ApplicationErrorReport;
import android.content.Intent;
import android.os.Bundle;

import org.carbonrom.errorreport.Reporter;

public class ErrorReportActivity extends Activity {

    private static final String TAG = ErrorReportActivity.class.getSimpleName();

    public static final String EXTRA_SYSTEM_EXCEPTION_TIME = "system_crash_exception_time";
    public static final String EXTRA_SYSTEM_EXCEPTION = "system_crash_exception";
    public static final String EXTRA_SYSTEM_EXCEPTION_PACKAGE = "system_crash_exception_package";
    public static final String EXTRA_SYSTEM_EXCEPTION_PROCESS = "system_crash_exception_process";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApplicationErrorReport report = getIntent().getExtras().getParcelable(Intent.EXTRA_BUG_REPORT);
        // Filter non-system non-crashes.
        // Also possible are TYPE_ANR reports
        // we might want in the future.
        if (report == null
            || report.type != ApplicationErrorReport.TYPE_CRASH
            || report.crashInfo == null
            || report.systemApp != true) {
            finish();
            return;
        }
        // We will send the error details as soon as the activity starts and 
        // allow the user to add more details if they elect too. We want to avoid
        // the user seeing text boxes and immediatly backing out because they
        // don't want to put any effort in (the text boxes will be/are optional)
        Reporter.report(this, report);
        // Eventually, I'd like this to return some kind of UUID
        // for the specific report and show the UI to allow users
        // to provide additional context if they would like.

        finish();
    }

}
