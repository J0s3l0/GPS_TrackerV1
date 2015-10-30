package com.itesm.csf.tracker.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

public class CSFTrackerBootReceiver extends BroadcastReceiver {
    private static final String TAG = "CSFTrackerBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent gpsTrackerIntent = new Intent(context, CSFTrackerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);

        SharedPreferences sharedPreferences = context.getSharedPreferences("gpstracker.prefs", Context.MODE_PRIVATE);
        int intervalInSeconds = sharedPreferences.getInt("intervalInSeconds", 1);
        Boolean currentlyTracking = sharedPreferences.getBoolean("currentlyTracking", false);

        if (currentlyTracking) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    intervalInSeconds * 1000,
                    pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }
}
