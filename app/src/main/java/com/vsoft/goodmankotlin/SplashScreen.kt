package com.vsoft.goodmankotlin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.vsoft.goodmankotlin.utils.CommonUtils

class SplashScreen : AppCompatActivity() {

    private var sharedPreferences: SharedPreferences?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        AppCenter.start(
            application, CommonUtils.APP_CENTER_ANALYTICS_SECRET_KEY,
            Analytics::class.java, Crashes::class.java
        )

        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            // Your Code
            if (CameraUtils.checkPermissions(applicationContext)) {
                if(sharedPreferences!!.getBoolean(CommonUtils.LOGIN_STATUS,false)){
                    navigateToDashBoard()
                }else {
                    navigateToLogin()
                }
            } else {
                requestCameraPermission()
            }
        }, CommonUtils.SPLASH_DURATION.toLong())
    }

    private fun navigateToDashBoard() {
        val i = Intent(this, DashBoardActivity::class.java)
        startActivity(i)
        // close this activity
        finish()
    }
    private fun navigateToLogin() {
        val i = Intent(this, LoginActivity::class.java)
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
                        if(sharedPreferences!!.getBoolean(CommonUtils.LOGIN_STATUS,false)){
                            navigateToDashBoard()
                        }else {
                            navigateToLogin()
                        }
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
        builder.setTitle(this@SplashScreen.resources.getString(R.string.settings_title))
            .setMessage(this@SplashScreen.resources.getString(R.string.settings_message))
            .setPositiveButton(
                this@SplashScreen.resources.getString(R.string.settings_option)
            ) { dialog, which -> CameraUtils.openSettings(this@SplashScreen) }
            .setNegativeButton(
                this@SplashScreen.resources.getString(R.string.alert_ok)
            ) { dialog, which -> }.show()
    }
}