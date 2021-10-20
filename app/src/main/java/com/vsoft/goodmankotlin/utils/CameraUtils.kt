package com.vsoft.goodmankotlin.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat

class CameraUtils {
     companion object {
         fun checkPermissions(context: Context?): Boolean {
             return ActivityCompat.checkSelfPermission(
                 context!!,
                 Manifest.permission.CAMERA
             ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                 context,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE
             ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                 context,
                 Manifest.permission.RECORD_AUDIO
             ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                 context,
                 Manifest.permission.ACCESS_COARSE_LOCATION
             ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                 context,
                 Manifest.permission.ACCESS_FINE_LOCATION
             ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                 context,
                 Manifest.permission.READ_EXTERNAL_STORAGE
             ) == PackageManager.PERMISSION_GRANTED
         }

         /**
          * Checks whether device has camera or not. This method not necessary if
          * android:required="true" is used in manifest file
          */
         fun isDeviceSupportCamera(context: Context): Boolean {
             // this device has a camera
             // no camera on this device
             return context.packageManager.hasSystemFeature(
                 PackageManager.FEATURE_CAMERA_ANY
             )
         }

         /**
          * Open device app settings to allow user to enable permissions
          */
         fun openSettings(context: Context) {
             context.startActivity(Intent().apply {
                 action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                 data = Uri.fromParts("package", context.packageName, null)
             })
         }
     }
}