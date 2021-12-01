package com.vsoft.goodmankotlin.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class NetworkSniffTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "TestIntranetIP's" + "nstask";
    private ProgressDialog pd;
    private WeakReference<Context> mContextRef;
    private NetworkSniffCallBack networkSniffCallBack;

    public NetworkSniffTask(Context context,NetworkSniffCallBack networkSniffCallBack1) {
        mContextRef = new WeakReference<Context>(context);
        networkSniffCallBack=networkSniffCallBack1;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(mContextRef.get());
        pd.setMessage("Checking server availability...");
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

            }
        }
        catch(UnknownHostException e1)
        {
            e1.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        System.out.println(connections);
        networkSniffCallBack.networkSniffResponse("success",connections);
//        try {
//            Context context = mContextRef.get();
//
//            if (context != null) {
//
//                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//                WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//
//                WifiInfo connectionInfo = wm.getConnectionInfo();
//                int ipAddress = connectionInfo.getIpAddress();
//                String ipString = Formatter.formatIpAddress(ipAddress);
//
//
//                Log.d(TAG, "activeNetwork: " + String.valueOf(activeNetwork));
//                Log.d(TAG, "ipString: " + String.valueOf(ipString));
//
//                String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
//                Log.d(TAG, "prefix: " + prefix);
//
//                for (int i = 0; i < 255; i++) {
//                    String testIp = prefix + String.valueOf(i);
//
//                    InetAddress address = InetAddress.getByName(testIp);
//                    boolean reachable = address.isReachable(1000);
//                    String hostName = address.getCanonicalHostName();
//
//                    if (reachable)
//                        Log.i(TAG, "Host: " + String.valueOf(hostName) + "(" + String.valueOf(testIp) + ") is reachable!");
//                }
//            }
//        } catch (Throwable t) {
//            Log.e(TAG, "Well that's not good.", t);
//        }

        return null;
    }
    public String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }
}
