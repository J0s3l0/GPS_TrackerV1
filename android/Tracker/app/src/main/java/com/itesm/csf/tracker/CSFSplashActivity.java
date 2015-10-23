package com.itesm.csf.tracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CSFSplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csfsplash);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{

                    SharedPreferences userProfile = getSharedPreferences("tokenUser", Context.MODE_PRIVATE);

                    if(userProfile.contains("Token")){
                        Intent i = new Intent(CSFSplashActivity.this, CSFTrackerActivity.class);
                        startActivity(i);
                    }else{
                        Intent i = new Intent(CSFSplashActivity.this, CSFLoginActivity.class);
                        startActivity(i);
                    }

                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
