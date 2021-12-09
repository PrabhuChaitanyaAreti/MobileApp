package com.vsoft.goodmankotlin.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import com.vsoft.goodmankotlin.interfaces.NetworkSniffTaskProgressCancelClickCallBack;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class NetworkSniffTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "TestIntranetIP's" + "nstask";
    private ProgressDialog pd;
    private final WeakReference<Context> mContextRef;
    private final NetworkSniffCallBack networkSniffCallBack;
    private final NetworkSniffTaskProgressCancelClickCallBack networkSniffTaskProgressCancelClickCallBack;

    public NetworkSniffTask(Context context, NetworkSniffCallBack networkSniffCallBack1, NetworkSniffTaskProgressCancelClickCallBack networkSniffTaskProgressCancelClickCallBack1) {
        mContextRef = new WeakReference<Context>(context);
        networkSniffCallBack=networkSniffCallBack1;
        networkSniffTaskProgressCancelClickCallBack=networkSniffTaskProgressCancelClickCallBack1;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(mContextRef.get());
        pd.setMessage("Checking server availability...");
        pd.setCancelable(false);
        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.dismiss();//dismiss dialog
                networkSniffTaskProgressCancelClickCallBack.onProgressCancelClickCallBack();
            }
        });
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

        System.out.println("Let's sniff the network");
        DhcpInfo d;
        WifiManager wifii;
        Context context = mContextRef.get();
        wifii = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        d = wifii.getDhcpInfo();
        ArrayList<String> connections = new ArrayList<>();
        InetAddress host;
        try
        {
            host = InetAddress.getByName(intToIp(d.ipAddress));
            byte[] ip = host.getAddress();

            for(int i = 1; i <= 254; i++)
            {
                ip[3] = (byte) i;
                InetAddress address = InetAddress.getByAddress(ip);
                if(address.isReachable(100))
                {
                    System.out.println(address + " machine is turned on and can be pinged");
                    connections.add(""+address);
                }
                else if(!address.getHostAddress().equals(address.getHostName()))
                {
                    System.out.println(address + " machine is known in a DNS lookup");
                }
                if(isCancelled()){
                    break;
                }

            }
        } catch(Exception e1)
        {
            e1.printStackTrace();
        }
        System.out.println(connections);
        networkSniffCallBack.networkSniffResponse("success",connections);
        return null;
    }
    public String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }
}
