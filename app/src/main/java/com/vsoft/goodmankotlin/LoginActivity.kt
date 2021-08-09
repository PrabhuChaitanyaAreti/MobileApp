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
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.model.UserAuthRequest
import com.vsoft.goodmankotlin.model.UserAuthResponse
import com.vsoft.goodmankotlin.utils.*
import kotlinx.android.synthetic.main.activity_add_die.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener,
    CustomDialogCallback {
    private lateinit var loginButton: Button
    private lateinit var empIdEditText: EditText
    private lateinit var pinEditText: EditText
    private var empIdStr = ""
    private var pinStr:String? = ""
    private val minEmpIdDigits = 6
    private var maxEmpIdDigits:Int = 8
    private var pinMaxDigits:Int = 4
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog
    private var sharedPreferences: SharedPreferences?=null

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
        sharedPreferences = this.getSharedPreferences(CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE)
    }
    private fun initProgress(){
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(this@LoginActivity.resources.getString(R.string.progress_dialog_message_login))


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
            empIdEditText.isFocusable = true
            empIdEditText.isFocusableInTouchMode = true
        } else if (view === pinEditText) {
            pinEditText.isFocusable = true
            pinEditText.isFocusableInTouchMode = true
        }
        return false
    }
    private fun validations() {
        empIdStr = empIdEditText.text.toString()
        pinStr = pinEditText.text.toString()
        if (empIdStr.isNotEmpty() && !TextUtils.isEmpty(empIdStr) && empIdStr != "null") {
            if (empIdStr.length in minEmpIdDigits..maxEmpIdDigits) {
                if (pinStr != null && pinStr!!.isNotEmpty() && !TextUtils.isEmpty(pinStr) && pinStr != "null") {
                    if (pinStr!!.length == pinMaxDigits) {
                        screenNavigationWithPermissions()
                    } else {
                        validationAlert("Please enter 4 digits Pin.",listOf<String>("Ok"))
                    }
                } else {
                    validationAlert("Please enter 4 digits Pin.",listOf<String>("Ok"))
                }
            } else {
                if (empIdStr.length < minEmpIdDigits) {
                    validationAlert("Username must be minimum 6 digits.",listOf<String>("Ok"))
                } else {
                    validationAlert("Username must be minimum 6 digits and maximum 8 digits.",listOf<String>("Ok"))
                }
            }
        } else {
            validationAlert("Username must be minimum 6 digits and maximum 8 digits.",listOf<String>("Ok"))
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
                                editor.putBoolean(CommonUtils.LOGIN_STATUS,true)
                                editor.apply()
                                navigateToDashBoard()
                            }else if(statusCode==401){
                                DialogUtils.showNormalAlert(
                                    this@LoginActivity,
                                    this@LoginActivity.resources.getString(R.string.alert_title),
                                    this@LoginActivity.resources.getString(R.string.login_alert_message)
                                )
                            }else{
                                DialogUtils.showNormalAlert(
                                    this@LoginActivity,
                                    this@LoginActivity.resources.getString(R.string.alert_title),
                                    this@LoginActivity.resources.getString(R.string.login_alert_message)
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
                        this@LoginActivity.resources.getString(R.string.alert_title),
                        this@LoginActivity.resources.getString(R.string.api_failure_alert_title)
                    )
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                }
            })
        } else {
            showCustomAlert("Please check your internet connection and try again","internetConnectionErrorDialog",
                listOf("Ok"))
        }
    }
    private fun navigateToOperatorSelection() {
        val mainIntent = Intent(this@LoginActivity, OperatorSelectActivityJava::class.java)
        startActivity(mainIntent)
        finish()
    }
    private fun navigateToDashBoard() {
        val mainIntent = Intent(this@LoginActivity, DashBoardActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
    private fun validationAlert(alertMessage: String,buttonList:List<String>) {
        showCustomAlert(alertMessage,"validationDialog",buttonList)
    }
    private fun showCustomAlert(alertMessage: String, functionality: String,buttonList:List<String>){
        var customDialogModel= CustomDialogModel(getString(R.string.app_name),alertMessage,null,
            buttonList
        )
        DialogUtils.showCustomAlert(this,customDialogModel,this,functionality)
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

    override fun onCustomDialogButtonClicked(buttonName: String,functionality:String) {
        if(buttonName.equals("Ok",true)){
            if(functionality.equals("validationDialog",true)){
                //No action required, just display
            }
            if(functionality.equals("internetConnectionErrorDialog",true)){
                //No action required on internet connection error
            }
        }
    }
}