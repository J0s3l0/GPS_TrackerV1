package com.itesm.csf.tracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.itesm.csf.tracker.utils.CSFFont;
import com.itesm.csf.tracker.utils.CSFGlobalVariables;
import com.itesm.csf.tracker.utils.CSFValidations;
import com.itesm.csf.tracker.utils.CSFWebBridge;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CSFLoginActivity extends Activity implements CSFWebBridge.WebBridgeListener {

    EditText user,pass;
    TextView txtError;
    Button btnAccess, btnRegister, btnForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_csflogin);

        initialize();

    }

    private void initialize(){
        user        = (EditText) findViewById(R.id.login_num_emp);
        pass         = (EditText) findViewById(R.id.login_pass);
        btnAccess    = (Button) findViewById(R.id.login_btn_access);
        //btnRegister  = (Button) findViewById(R.id.login_btn_register);
        //btnForgot    = (Button) findViewById(R.id.login_btn_forgot);
        txtError     = (TextView) findViewById(R.id.login_txt_error);

        /*
        noEmp       .setTypeface(CSFFont.get(this, "fredoka"));
        pass        .setTypeface(CSFFont.get(this, "fredoka"));
        btnAccess   .setTypeface(CSFFont.get(this, "fredoka"));
        btnRegister .setTypeface(CSFFont.get(this, "fredoka"));
        btnForgot   .setTypeface(CSFFont.get(this, "fredoka"));
        txtError    .setTypeface(CSFFont.get(this, "fredoka"));
        */
    }

     /*-----------------*/
	/* CLICK LISTENERS */

    public void homeAction(View v){

        txtError.setVisibility(View.INVISIBLE);

        if (checkInfo()){

            HashMap<String, Object> params = new HashMap<>();
            params.put("username", user.getText());
            params.put("password", pass.getText());

            CSFWebBridge.send(CSFGlobalVariables.API_AUTH, params, "Autenticación", this, this);

        }

    }

     /*--------------------*/
	/* WEBBRIDGE LISTENER */

    @Override
    public void onWebBridgeSuccess(String url, JSONObject json) {

        if (url.contains(CSFGlobalVariables.API_AUTH)) {
            String Success = "";
            try {
                Success = json.getString("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }


            if (Success.equals("1")) {
                pass.setText("");

                SharedPreferences tokenUser = getSharedPreferences("tokenUser", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = tokenUser.edit();

                //Almacenamos los datos del JSON en la estructura
                try {
                    editor.putString("Token", json.getString("token"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                editor.putString("User", user.getText().toString());
                editor.commit();

                Intent intent = new Intent(CSFLoginActivity.this, CSFTrackerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent, 1);

            } else {
                new MaterialDialog.Builder(this)
                        .title(R.string.txt_error)
                        .content(R.string.txt_error_values_login)
                        .positiveText(R.string.bt_close)
                        .theme(Theme.LIGHT)
                        .show();
            }
        }
    }

    @Override
    public void onWebBridgeFailure(String url, String response) {
        new MaterialDialog.Builder(this)
                .title(R.string.txt_error)
                .content(R.string.txt_error_values_login)
                .positiveText(R.string.bt_close)
                .theme(Theme.LIGHT)
                .show();
    }

/*
    public void registerAction(View v){
        Intent i = new Intent(CSFLoginActivity.this, CSFRegisterActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_up_info, R.anim.no_change);
    }

    public void recoverAction(View v){
        Intent i = new Intent(KCOLoginActivity.this, KCORecoverPassActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.left_in,R.anim.left_out);
    }
*/

     /*-----------------*/
	/* OVERRIDE RESULT */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode != RESULT_OK) {
            finish();
        }
    }

    /*VALIDATE INFO*/
    private boolean checkInfo(){
        boolean infoOk = false;

        if(user.getText().toString().isEmpty() ){
            user.setHintTextColor(getResources().getColor(R.color.red_error));
            txtError.setVisibility(View.VISIBLE);

        }else if( pass.getText().toString().isEmpty() ||
                !CSFValidations.isValidPassword( pass.getText().toString()) ){
            pass.setHintTextColor(getResources().getColor(R.color.red_error));
            txtError.setVisibility(View.VISIBLE);

        }else{
            infoOk = true;
        }
        return infoOk;

    }
}
