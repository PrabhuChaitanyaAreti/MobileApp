package com.vsoft.goodmankotlin

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.vsoft.goodmankotlin.utils.CameraUtils
import com.vsoft.goodmankotlin.utils.CommonUtils

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private var loginButton: Button? = null
    private var empIdEditText: EditText? = null
    private  var pinEditText: EditText? = null
    private var empIdStr = ""
    private  var pinStr:String? = ""
    private val minEmpIdDigits = 6
    private  var maxEmpIdDigits:Int = 8
    private  var pinMaxDigits:Int = 4
    private var alertDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        empIdEditText = findViewById(R.id.empIdEditText)
        pinEditText = findViewById(R.id.pinEditText)
        empIdEditText?.setOnTouchListener(this)
        pinEditText?.setOnTouchListener(this)

        loginButton = findViewById(R.id.loginButton)
        loginButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view === loginButton) {
            validations()
        }
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (view === empIdEditText) {
            empIdEditText!!.isFocusable = true
            empIdEditText!!.isFocusableInTouchMode = true
        } else if (view === pinEditText) {
            pinEditText!!.isFocusable = true
            pinEditText!!.isFocusableInTouchMode = true
        }
        return false
    }
    private fun validations() {
        empIdStr = empIdEditText!!.text.toString()
        pinStr = pinEditText!!.text.toString()
        if (empIdStr != null && empIdStr.isNotEmpty() && !TextUtils.isEmpty(empIdStr) && empIdStr != "null") {
            if (empIdStr.length in minEmpIdDigits..maxEmpIdDigits) {
                if (pinStr != null && pinStr!!.isNotEmpty() && !TextUtils.isEmpty(pinStr) && pinStr != "null") {
                    if (pinStr!!.length == pinMaxDigits) {
                        screenNavigationWithPermissions()
                    } else {
                        validationAlert("Please enter 4 digits Pin.")
                    }
                } else {
                    validationAlert("Please enter 4 digits Pin.")
                }
            } else {
                if (empIdStr.length < minEmpIdDigits) {
                    validationAlert("Please enter Emp Id with minimum 6 digits.")
                } else {
                    validationAlert("Please enter Emp Id with minimum 6 digits and maximum 8 digits.")
                }
            }
        } else {
            validationAlert("Please enter Emp Id with minimum 6 digits and maximum 8 digits.")
        }
    }
    private fun screenNavigationWithPermissions() {
        if (CameraUtils.checkPermissions(applicationContext)) {
            operatorSelectionScreenNavigation()
        } else {
            requestCameraPermission()
        }
    }

    private fun operatorSelectionScreenNavigation() {
        val mainIntent = Intent(this@LoginActivity, OperatorSelectActivityNew::class.java)
        startActivity(mainIntent)
    }
    private fun validationAlert(alertMessage: String) {
        val builder = AlertDialog.Builder(this@LoginActivity)
        builder.setCancelable(false)
        builder.setTitle(this@LoginActivity.getResources().getString(R.string.app_name))
        builder.setMessage(alertMessage)
        builder.setNeutralButton("Ok") { dialog, which ->
            dialog.dismiss()
            if (alertDialog!!.isShowing) {
                alertDialog!!.dismiss()
            }
            dialog.dismiss()
        }
        alertDialog = builder.create()
        if (!this@LoginActivity.isFinishing()) {
            try {
                alertDialog?.show()
            } catch (e: WindowManager.BadTokenException) {
                Log.e("BadTokenException", e.toString())
            }
        }
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
                        operatorSelectionScreenNavigation()
                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        CommonUtils.showPermissionsAlert(this@LoginActivity)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }
}