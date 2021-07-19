package com.vsoft.goodmankotlin.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

class BatteryUtil {
    companion object {
        @SuppressLint("ObsoleteSdkInt")
        fun getBatteryPercentage(context: Context): Int {
            val isConnected = isConnected(context)
            return if (isConnected) {
                20
            } else {
                if (Build.VERSION.SDK_INT >= 21) {
                    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                    bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                } else {
                    val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    val batteryStatus = context.registerReceiver(null, iFilter)
                    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                    val batteryPct = level / scale.toDouble()
                    (batteryPct * 100).toInt()
                }
            }
        }

        fun isConnected(context: Context): Boolean {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val plugged = intent!!.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
        }
    }
}