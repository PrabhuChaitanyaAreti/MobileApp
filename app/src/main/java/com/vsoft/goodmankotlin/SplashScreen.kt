package com.vsoft.goodmankotlin

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.vsoft.goodmankotlin.utils.CameraUtils


class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler(Looper.getMainLooper()).postDelayed({
            // Your Code
            if (CameraUtils.checkPermissions(applicationContext)) {
                navigationScreen()
            } else {
                requestCameraPermission()
            }
        }, 1*1000)
    }

    private fun navigationScreen() {
        val i = Intent(this, MaskingActivity::class.java)
        startActivity(i)
        // close this activity
        finish()
    }

    /**
     * Requesting permissions using Dexter library
     */
    private fun requestCameraPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
            .withListener(object : MultiplePermissionsListener {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        navigationScreen()
                    } else if (report.isAnyPermissionPermanentlyDenied()) {
                        showPermissionsAlert()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    /**
     * Alert dialog to navigate to app settings
     * to enable necessary permissions
     */
    private fun showPermissionsAlert() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Permissions required!")
            .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
            .setPositiveButton(
                "GOTO SETTINGS"
            ) { dialog, which -> CameraUtils.openSettings(this@SplashScreen) }
            .setNegativeButton(
                "CANCEL"
            ) { dialog, which -> }.show()
    }
}