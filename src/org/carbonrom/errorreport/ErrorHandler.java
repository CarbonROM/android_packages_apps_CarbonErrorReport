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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;

public class ErrorHandler extends Activity {
    private static final String TAG = ErrorHandler.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate savedInstanceState=" + savedInstanceState);

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

        Log.i(TAG, "Process " + report.processName + " in package " + report.packageName + " crashed at " + report.time);
        StringBuilder reportDump = new StringBuilder();
        report.dump(new StringBuilderPrinter(reportDump), "prefix");
        Log.i(TAG, reportDump.toString());
    }
}
