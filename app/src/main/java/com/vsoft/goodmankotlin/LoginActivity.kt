package com.vsoft.goodmankotlin

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.vsoft.goodmankotlin.model.PunchResponse
import com.vsoft.goodmankotlin.model.UserAuthRequest
import com.vsoft.goodmankotlin.model.UserAuthResponse
import com.vsoft.goodmankotlin.utils.*
import kotlinx.android.synthetic.main.activity_add_die.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private lateinit var loginButton: Button
    private lateinit var empIdEditText: EditText
    private lateinit var pinEditText: EditText
    private var empIdStr = ""
    private  var pinStr:String? = ""
    private val minEmpIdDigits = 6
    private  var maxEmpIdDigits:Int = 8
    private  var pinMaxDigits:Int = 4
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog
    private val sharedPrefFile = "kotlinsharedpreference"
    var sharedPreferences: SharedPreferences?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
        initProgress()
        initListeners()
    }
    private fun init(){
        empIdEditText = findViewById(R.id.empIdEditText)
        pinEditText = findViewById(R.id.pinEditText)
        loginButton = findViewById(R.id.loginButton)
        sharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
    }
    private fun initProgress(){
        progressDialog = ProgressDialog(this)
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Please wait .. Checking user details..")
    }
    private fun initListeners(){
        empIdEditText.setOnTouchListener(this)
        pinEditText.setOnTouchListener(this)

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
                    validationAlert("Username must be minimum 6 digits.")
                } else {
                    validationAlert("Username must be minimum 6 digits and maximum 8 digits.")
                }
            }
        } else {
            validationAlert("Username must be minimum 6 digits and maximum 8 digits.")
        }
    }
    private fun screenNavigationWithPermissions() {
        if (CameraUtils.checkPermissions(applicationContext)) {
            validateUser(empIdStr,pinStr!!)
        } else {
            requestCameraPermission()
        }
    }
    private fun validateUser(userId:String,password:String){
        if (NetworkUtils.isNetworkAvailable(this@LoginActivity)) {
            Handler(Looper.getMainLooper()).post {
                progressDialog!!.show()
            }
            val call: Call<UserAuthResponse?>? =
                RetrofitClient.getInstance()!!.getMyApi()!!.authenticate(UserAuthRequest(userId,password))
            call!!.enqueue(object : Callback<UserAuthResponse?> {
                override fun onResponse(
                    call: Call<UserAuthResponse?>,
                    response: Response<UserAuthResponse?>
                ) {
                    try {
                        val statusCode=response.body()!!.statusCode
                            if(statusCode==200){
                                val editor: SharedPreferences.Editor =  sharedPreferences!!.edit()
                                editor.putBoolean("loginStatus",true)
                                editor.apply()
                                navigateToDashBoard()
                            }else if(statusCode==401){
                                DialogUtils.showNormalAlert(
                                    this@LoginActivity,
                                    "Alert!!",
                                    "Invalid Credentials"
                                )
                            }else{
                                DialogUtils.showNormalAlert(
                                    this@LoginActivity,
                                    "Alert!!",
                                    "Invalid Credentials"
                                )
                            }
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }
                    }
                }
                override fun onFailure(call: Call<UserAuthResponse?>, t: Throwable) {
                    DialogUtils.showNormalAlert(
                        this@LoginActivity,
                        "Alert!!",
                        "Unable to communicate with server"
                    )
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                }
            })
        } else {
            DialogUtils.showNormalAlert(
                this@LoginActivity,
                "Alert!!",
                "Please check your internet connection and try again"
            )
        }
    }
    private fun navigateToOperatorSelection() {
        val mainIntent = Intent(this@LoginActivity, OperatorSelectActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
    private fun navigateToDashBoard() {
        val mainIntent = Intent(this@LoginActivity, DashBoardActivity::class.java)
        startActivity(mainIntent)
        finish()
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
        if (!this@LoginActivity.isFinishing) {
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
                        validateUser(empIdStr,pinStr!!)
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