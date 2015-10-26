package com.itesm.csf.tracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.itesm.csf.tracker.utils.CSFGlobalVariables;
import com.itesm.csf.tracker.utils.CSFTrackerAlarmReceiver;

import java.util.UUID;


public class CSFHomeUser extends Fragment {
    private Context context = null;

    private static final String TAG = "CSFTrackerActivity";

    private String defaultUploadWebsite;

    private static TextView username;
    private static Button trackingButton;

    private boolean currentlyTracking;
    private int intervalInMinutes = 1;
    private AlarmManager alarmManager;
    private Intent gpsTrackerIntent;
    private PendingIntent pendingIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        context = this.getActivity();
        View v = inflater.inflate(R.layout.fragment_csfhome_user, null);

        defaultUploadWebsite = CSFGlobalVariables.API_BASE + CSFGlobalVariables.API_LOCATION;

        username = (TextView)v.findViewById(R.id.tv_user);
        trackingButton = (Button) v.findViewById(R.id.trackingButton);

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("gpstracker.prefs", Context.MODE_PRIVATE);
        currentlyTracking = sharedPreferences.getBoolean("currentlyTracking", false);

        boolean firstTimeLoadindApp = sharedPreferences.getBoolean("firstTimeLoadindApp", true);
        if (firstTimeLoadindApp) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTimeLoadindApp", false);
            editor.putString("appID",  UUID.randomUUID().toString());
            editor.apply();
        }

        saveInterval();
        loadData();
        trackingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                trackLocation(view);
            }
        });

        return v;
    }

    private void saveInterval() {
        if (currentlyTracking) {
            Toast.makeText(this.getActivity().getApplicationContext(), R.string.user_needs_to_restart_tracking, Toast.LENGTH_LONG).show();
        }

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("intervalInMinutes", 10);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences userProfile = getActivity().getSharedPreferences("tokenUser", Context.MODE_PRIVATE);
        username.setText(userProfile.getString("User",""));
    }

    private void startAlarmManager() {
        Log.d(TAG, "startAlarmManager");

        //Context context = this.getBaseContext();
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        gpsTrackerIntent = new Intent(context, CSFTrackerAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("gpstracker.prefs", Context.MODE_PRIVATE);
        intervalInMinutes = sharedPreferences.getInt("intervalInMinutes", 1);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                intervalInMinutes * 60000, // 60000 = 1 minute
                pendingIntent);
    }

    private void cancelAlarmManager() {
        Log.d(TAG, "cancelAlarmManager");

        //Context context = getBaseContext();
        Intent gpsTrackerIntent = new Intent(context, CSFTrackerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    // called when trackingButton is tapped
    protected void trackLocation(View v) {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!saveUserSettings()) {
            return;
        }

        if (!checkIfGooglePlayEnabled()) {
            return;
        }

        if (currentlyTracking) {
            cancelAlarmManager();

            currentlyTracking = false;
            editor.putBoolean("currentlyTracking", false);
            editor.putString("sessionID", "");
        } else {
            startAlarmManager();

            currentlyTracking = true;
            editor.putBoolean("currentlyTracking", true);
            editor.putFloat("totalDistanceInMeters", 0f);
            editor.putBoolean("firstTimeGettingPosition", true);
            editor.putString("sessionID",  UUID.randomUUID().toString());
        }

        editor.apply();
        setTrackingButtonState();
    }

    private boolean saveUserSettings() {
        if (textFieldsAreEmptyOrHaveSpaces()) {
            return false;
        }

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("intervalInMinutes", 10);
        editor.putString("userName", username.getText().toString());
        editor.putString("defaultUploadWebsite", defaultUploadWebsite);

        editor.apply();

        return true;
    }

    private boolean textFieldsAreEmptyOrHaveSpaces() {


        if (username.length() == 0 || hasSpaces(username.getText().toString()) ) {
            Toast.makeText(this.getActivity(), R.string.textfields_empty_or_spaces, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    private boolean hasSpaces(String str) {
        return ((str.split(" ").length > 1) ? true : false);
    }

    private boolean checkIfGooglePlayEnabled() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity()) == ConnectionResult.SUCCESS) {
            return true;
        } else {
            Log.e(TAG, "unable to connect to google play services.");
            Toast.makeText(this.getActivity().getApplicationContext(), R.string.google_play_services_unavailable, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void setTrackingButtonState() {
        if (currentlyTracking) {
            trackingButton.setBackgroundResource(R.drawable.green_tracking_button);
            trackingButton.setTextColor(Color.BLACK);
            trackingButton.setText(R.string.tracking_is_on);
        } else {
            trackingButton.setBackgroundResource(R.drawable.red_tracking_button);
            trackingButton.setTextColor(Color.WHITE);
            trackingButton.setText(R.string.tracking_is_off);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        setTrackingButtonState();
    }

    /*
    @Override
    protected void onStop() {
        super.onStop();
    }*/

}
