package com.vsoft.goodmankotlin.cumulocity;

import static android.content.Context.BATTERY_SERVICE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttException;


public class DeviceManagement {


    public static String getDeviceMemory() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable;
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } else {
            bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        }
        long megAvailable = bytesAvailable / (1024 * 1024);
        Log.e("BATT", "Available MB : " + megAvailable);

        return bytesAvailable + "";

    }

    public static String getBatteryManagement(Context context){
        
        String percent = null;
        BatteryManager bm = (BatteryManager)context.getSystemService(BATTERY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            percent = String.valueOf(percentage);
            Log.e("BATT", "Available Per : " + percent);

        }
        return percent;
    }

    public static String getDeviceInfo(Context context){
        String deviceInfo =  null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String android_id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String deviceModel = Build.MANUFACTURER;
                    deviceInfo = android_id+","+deviceModel;
        }
        Log.e("BATT", "Available Info : " + deviceInfo);
        return deviceInfo;

    }

    public static String batteryTemperature(Context context)
    {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        float  temp   = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)) / 10;
        Log.e("BATT", "Temp : " + String.valueOf(temp));
        return String.valueOf(temp);

    }


}