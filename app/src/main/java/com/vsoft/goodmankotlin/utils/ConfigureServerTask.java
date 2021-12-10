package com.vsoft.goodmankotlin.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class ConfigureServerTask extends AsyncTask<Void, Void, Void> {
    private ProgressDialog pd;
    private ConfigureServerTaskCallback configureServerTaskCallback;
    private WeakReference<Context> mContextRef;
    private ArrayList<String> ipAddressList;
    public ConfigureServerTask(Context context, ConfigureServerTaskCallback configureServerTaskCallback1, ArrayList<String> ipAddressList1) {
        mContextRef = new WeakReference<Context>(context);
        configureServerTaskCallback=configureServerTaskCallback1;
        ipAddressList=ipAddressList1;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(mContextRef.get());
        pd.setMessage("Configuring Server...");
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        if (pd.isShowing()){
            pd.dismiss();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String responseString = null;
        if(ipAddressList!=null && ipAddressList.size()>0){
            for(String ipAddress : ipAddressList){
                String myurl="http:/"+ipAddress+":16808/"+"discoverIp";
                System.out.println(myurl);
                try {
                    URL url = new URL(myurl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if(conn.getResponseCode() == HttpsURLConnection.HTTP_OK){
                        // Do normal input or output stream reading
                        ipAddress="http:/"+ipAddress+":16808";
                        Context context=mContextRef.get();
                        var prefs = context.getSharedPreferences(context.getPackageName(), context.MODE_PRIVATE);
                        prefs.edit().putString("EdgeServerIp", ipAddress).apply();
                        System.out.println("ServerAddress:"+ipAddress);
                        responseString="success";
                        configureServerTaskCallback.configureServerResponse("success");
                        break;
                    }
                    else {
                        responseString = "failure"; // See documentation for more info on response handling
                    }
                }  catch (IOException e) {
                    //TODO Handle problems..
                    e.printStackTrace();
                    responseString = "failure"; // See documentation for more info on response handling
                }
            }
        }
        if(responseString!=null && responseString.equalsIgnoreCase("failure")){
            String ipAddress="http://111.93.3.148:16808";
            Context context=mContextRef.get();
            var prefs = context.getSharedPreferences(context.getPackageName(), context.MODE_PRIVATE);
            prefs.edit().putString("EdgeServerIp", ipAddress).apply();
            System.out.println("ServerAddress:"+ipAddress);
            configureServerTaskCallback.configureServerResponse("failure");
        }
        return null;
    }
}
