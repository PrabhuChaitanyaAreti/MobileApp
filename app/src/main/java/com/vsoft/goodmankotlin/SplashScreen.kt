package com.vsoft.goodmankotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.vsoft.goodmankotlin.utils.CameraUtils
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity(), CustomDialogCallback {

    private var screenWidth:Int = 0
    private var screenHeight:Int = 0

    private var sharedPreferences: SharedPreferences? = null
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val displayMetrics = DisplayMetrics()
        this@SplashScreen.windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        Log.d(
            "TAG",
            "SplashScreen device width and height " + screenWidth + "x" + screenHeight
        )

        AppCenter.start(
            application, CommonUtils.APP_CENTER_ANALYTICS_SECRET_KEY,
            Analytics::class.java, Crashes::class.java
        )

        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )

        userId = sharedPreferences!!.getString(CommonUtils.LOGIN_USER_ID, "").toString()

    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (CameraUtils.checkPermissions(applicationContext)) {
                if (sharedPreferences!!.getBoolean(CommonUtils.LOGIN_STATUS, false)) {
                    if (userId.isNotEmpty() && !TextUtils.isEmpty(userId) && userId != "null") {
                        navigateToDashBoard()
                    } else {
                        navigateToLogin()
                    }
                } else {
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
                            if (sharedPreferences!!.getBoolean(CommonUtils.LOGIN_STATUS, false)) {
                                if (userId.isNotEmpty() && !TextUtils.isEmpty(userId) && userId != "null") {
                                    navigateToDashBoard()
                                } else {
                                    navigateToLogin()
                                }
                            } else {
                                navigateToLogin()
                            }
                        } else if (report.isAnyPermissionPermanentlyDenied) {
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
        DialogUtils.showCustomAlert(
                this, CustomDialogModel(
                this.resources.getString(R.string.app_name),
                this@SplashScreen.resources.getString(R.string.settings_message),
                null,
                listOf(
                        this@SplashScreen.resources.getString(R.string.settings_option),
                        this@SplashScreen.resources.getString(R.string.alert_ok)
                )
        ), this, CommonUtils.PERMISSIONS_DIALOG
        )
    }

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(this@SplashScreen.resources.getString(R.string.alert_ok), true)) {
            if (functionality.equals(CommonUtils.PERMISSIONS_DIALOG, true)) {
                //No action required, just display
                super.onBackPressed()
            }
        }
        if (buttonName.equals(
                        this@SplashScreen.resources.getString(R.string.settings_option),
                        true
                )
        ) {
            if (functionality.equals(CommonUtils.PERMISSIONS_DIALOG, true)) {
                CameraUtils.openSettings(this@SplashScreen)
            }
        }
    }


}