package com.itesm.csf.tracker.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class CSFTrackerAlarmReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "CSFTrackerAlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, CSFLocationService.class));
    }
}
