package com.vsoft.goodmankotlin.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.karumi.dexter.BuildConfig
import java.io.File
import java.util.*

 class CameraUtils {
     companion object {
         /**
          * Refreshes gallery on adding new image/video. Gallery won't be refreshed
          * on older devices until device is rebooted
          */
         fun refreshGallery(context: Context?, filePath: String) {
             // ScanFile so it will be appeared on Gallery
             MediaScannerConnection.scanFile(context, arrayOf(filePath), null,
                 object : MediaScannerConnection.OnScanCompletedListener {
                     override fun onScanCompleted(path: String, uri: Uri) {}
                 })
         }

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
          * Downsizing the bitmap to avoid OutOfMemory exceptions
          */
         fun optimizeBitmap(sampleSize: Int, filePath: String?): Bitmap {
             // bitmap factory
             val options: BitmapFactory.Options = BitmapFactory.Options()

             // downsizing image as it throws OutOfMemory Exception for larger
             // images
             options.inSampleSize = sampleSize
             return BitmapFactory.decodeFile(filePath, options)
         }

         /**
          * Checks whether device has camera or not. This method not necessary if
          * android:required="true" is used in manifest file
          */
         fun isDeviceSupportCamera(context: Context): Boolean {
             // this device has a camera
             // no camera on this device
             return context.packageManager.hasSystemFeature(
                 PackageManager.FEATURE_CAMERA
             )
         }

         /**
          * Open device app settings to allow user to enable permissions
          */
         fun openSettings(context: Context) {
             val intent = Intent()
             intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
             intent.data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
             context.startActivity(intent)
         }

         @RequiresApi(api = Build.VERSION_CODES.KITKAT)
         fun getOutputMediaFileUri(context: Context, file: File?): Uri {
             return FileProvider.getUriForFile(
                 Objects.requireNonNull(context),
                 BuildConfig.APPLICATION_ID.toString() + ".provider", file!!
             )
         }
     }
}