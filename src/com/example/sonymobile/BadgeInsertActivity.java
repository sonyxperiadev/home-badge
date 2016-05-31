/*
 * Copyright (C) 2016 Sony Mobile Communications Inc.
 * All rights, including trade secret rights, reserved.
 *
 * This software may be distributed under the terms of the BSD license.
 */

package com.example.sonymobile;

import com.sonymobile.home.resourceprovider.BadgeProviderContract;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BadgeInsertActivity extends Activity {

    private AsyncQueryHandler mQueryHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editText = (EditText)findViewById(R.id.et_badge_count);
        final Button button = (Button)findViewById(R.id.b_set);
        final TextView resultTextView = (TextView)findViewById(R.id.tv_badge_result);

        // Get package and activity names for this activity
        ComponentName componentName = getComponentName();

        final String packageName = componentName.getPackageName();
        final String activityName = componentName.getClassName();

        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String strBadgeCount = editText.getText().toString();
                int badgeCount = Integer.parseInt(strBadgeCount);

                if (badgeCount >= 0) {
                    insertBadgeAsync(badgeCount, packageName, activityName);
                } else {
                    resultTextView.setText("Specify badge count");
                }
            }
        });

        mQueryHandler = new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                if (uri != null) {
                    resultTextView.setText("Success");
                } else {
                    resultTextView.setText("Failure");
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        mQueryHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * Insert a badge associated with the specified package and activity names
     * asynchronously. The package and activity names must correspond to an
     * activity that holds an intent filter with action
     * "android.intent.action.MAIN" and category
     * "android.intent.category.LAUNCHER" in the manifest. Also, it is not
     * allowed to publish badges on behalf of another client, so the package and
     * activity names must belong to the process from which the insert is made.
     * To be able to insert badges, the app must have the PROVIDER_INSERT_BADGE
     * permission in the manifest file. In case these conditions are not
     * fulfilled, or any content values are missing, there will be an unhandled
     * exception on the background thread.
     *
     * @param badgeCount the badge count
     * @param packageName the package name
     * @param activityName the activity name
     */
    private void insertBadgeAsync(int badgeCount, String packageName, String activityName) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(BadgeProviderContract.Columns.BADGE_COUNT, badgeCount);
        contentValues.put(BadgeProviderContract.Columns.PACKAGE_NAME, packageName);
        contentValues.put(BadgeProviderContract.Columns.ACTIVITY_NAME, activityName);

        // The badge must be inserted on a background thread
        mQueryHandler.startInsert(0, null, BadgeProviderContract.CONTENT_URI, contentValues);
    }
}
