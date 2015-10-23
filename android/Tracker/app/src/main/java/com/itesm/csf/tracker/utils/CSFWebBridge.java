package com.itesm.csf.tracker.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.itesm.csf.tracker.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class CSFWebBridge {
    private MaterialDialog progress;
    private WebBridgeListener callback;
    private AsyncHttpClient client;
    private String url;

    static public ArrayList<CSFWebBridge> instances = new ArrayList<>();

    static public String url(String url) {
        if (url.indexOf("http://") == 0) return url;
        String u = CSFGlobalVariables.API_BASE + url;
        Log.e("REQUEST URL", u);
        return u;
    }

    static private void addVersion(Map<String,Object> params, Activity activity) {
        String app_version = "N/A";
        try {
            app_version = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (Exception e) {}

        params.put("app_version", app_version);
        params.put("app_os", "android");
    }

    static public CSFWebBridge send(String url, HashMap<String,Object> params, String message, Activity activity, WebBridgeListener callback) {
        CSFWebBridge wb = CSFWebBridge.getInstance(activity, message, callback);
        if (wb != null) {
            wb.send(CSFWebBridge.url(url), params);
        }
        return wb;
    }

    static public CSFWebBridge send(String url, HashMap<String,Object> params,  Activity activity, WebBridgeListener callback) {
        CSFWebBridge wb = CSFWebBridge.getInstance(activity, null, callback);
        if (wb != null) {
            wb.send(CSFWebBridge.url(url), params);
        }
        return wb;
    }

    static public CSFWebBridge send(String url, String message, Activity activity, WebBridgeListener callback) {
        CSFWebBridge wb = CSFWebBridge.getInstance(activity, message, callback);
        if (wb != null) {
            wb.send(CSFWebBridge.url(url), null);
        }
        return wb;
    }


    static public CSFWebBridge send(String url, HashMap<String,Object> params, Activity activity) {
        CSFWebBridge wb = CSFWebBridge.getInstance(activity, null, null);
        if (wb != null) {
            wb.send(CSFWebBridge.url(url), params);
        }
        return wb;
    }

    static public CSFWebBridge send(String url, Activity activity, WebBridgeListener callback) {
        CSFWebBridge wb = CSFWebBridge.getInstance(activity, null, callback);
        if (wb != null) {
            wb.send(CSFWebBridge.url(url), null);
        }
        return wb;
    }


    static CSFWebBridge getInstance(Activity activity, String message, WebBridgeListener callback) {

        if (!CSFWebBridge.haveNetworkConnection(activity)) {
            Toast.makeText(activity, activity.getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
            return null;
        }

        CSFWebBridge wb = new CSFWebBridge();
        wb.callback  = callback;
        //wb.client    = new AsyncHttpClient(true,80,443);
        wb.client    = new AsyncHttpClient();


        PersistentCookieStore cookie = new PersistentCookieStore(activity);
        wb.client.setCookieStore(cookie);

        if (message != null) wb.progress = new MaterialDialog.Builder(activity)
                .title(message)
                .content(R.string.please_wait)
                .progress(true, 0)
                .theme(Theme.LIGHT)
                .show();

        CSFWebBridge.instances.add(wb);

        return wb;
    }

    protected void send(String url, HashMap<String,Object> p) {

        this.url = url;

        /*
        JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                success(response.toString());
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                success(response.toString());
            }
        };
        */


        AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = null;
                try {
                    response = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                success(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = "";
                try {
                    response = new String(responseBody, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                failure(response);
            }
        };


        if (p != null) {
            RequestParams params = new RequestParams();

            for (Map.Entry<String,Object> entry : p.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (v instanceof File) {
                    File f = (File)v;
                    try {
                        params.put(k, f, "image/jpeg");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    params.put(k, v);
                }
            }

            Log.e("PARAMS:", p.toString());
            client.post(url, params, handler);
        } else {
            client.get(url, handler);
        }

    }

    public void failure (String response) {
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
        if (callback != null) {
            callback.onWebBridgeFailure(url, response);
        }
    }

    public void success (String response) {

        Log.e("RESPONSE", response);

        if (progress != null) {
            progress.dismiss();
            progress = null;
        }

        if (callback != null) {
            JSONObject json = null;
            try {
                json = new JSONObject(response);
            } catch (Exception ignored) {}
            callback.onWebBridgeSuccess(url, json);
        }

        CSFWebBridge.instances.remove(this);

    }

    static public boolean haveNetworkConnection(Activity a) {

        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) a.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) {
                    haveConnectedWifi = true;
                }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {
                    haveConnectedMobile = true;
                }
        }

        return haveConnectedWifi || haveConnectedMobile;
    }

    public interface WebBridgeListener {
        void onWebBridgeSuccess(String url, JSONObject json);
        void onWebBridgeFailure(String url, String response);
    }
}
