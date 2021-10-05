package com.vsoft.goodmankotlin

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener,
    CustomDialogCallback {
    private lateinit var loginButton: Button
    private lateinit var empIdEditText: EditText
    private lateinit var pinEditText: EditText
    private var empIdStr = ""
    private var pinStr: String? = ""
    private val minEmpIdDigits = 6
    private var maxEmpIdDigits: Int = 8
    private var pinMaxDigits: Int = 4
    private lateinit var progressDialog: ProgressDialog
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
        initProgress()
        initListeners()
    }

    private fun init() {
        empIdEditText = findViewById(R.id.empIdEditText)
        pinEditText = findViewById(R.id.pinEditText)
        loginButton = findViewById(R.id.loginButton)
        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )
    }

    private fun initProgress() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(this@LoginActivity.resources.getString(R.string.progress_dialog_message_login))


    }

    private fun initListeners() {
        empIdEditText.setOnTouchListener(this)
        pinEditText.setOnTouchListener(this)

        loginButton.setOnClickListener(this)
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
                        validationAlert(this.resources.getString(R.string.login_pin_validation_message), listOf(this.resources.getString(R.string.alert_ok)))
                    }
                } else {
                    validationAlert(this.resources.getString(R.string.login_password_validation_message_empty), listOf(this.resources.getString(R.string.alert_ok)))
                }
            } else {
                if (empIdStr.length < minEmpIdDigits) {
                    validationAlert(this.resources.getString(R.string.login_username_validation_message_min), listOf(this.resources.getString(R.string.alert_ok)))
                } else {
                    validationAlert(
                        this.resources.getString(R.string.login_username_validation_message_range),
                        listOf(this.resources.getString(R.string.alert_ok))
                    )
                }
            }
        } else {
            validationAlert(
                this.resources.getString(R.string.login_username_validation_message_empty),
                listOf(this.resources.getString(R.string.alert_ok))
            )
        }
    }

    private fun screenNavigationWithPermissions() {
        if (CameraUtils.checkPermissions(applicationContext)) {
            validateUser(empIdStr, pinStr!!)
        } else {
            requestCameraPermission()
        }
    }

    private fun validateUser(userId: String, password: String) {
        if (NetworkUtils.isNetworkAvailable(this@LoginActivity)) {
            Handler(Looper.getMainLooper()).post {
                progressDialog.show()
            }
            val call: Call<UserAuthResponse?>? =
                RetrofitClient.getInstance()!!.getMyApi()!!
                    .authenticate(UserAuthRequest(userId, password))
            call!!.enqueue(object : Callback<UserAuthResponse?> {
                override fun onResponse(
                    call: Call<UserAuthResponse?>,
                    response: Response<UserAuthResponse?>
                ) {
                    try {
                        val statusCode = response.body()!!.statusCode
                        if (statusCode == 200) {
                            val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                            editor.putBoolean(CommonUtils.LOGIN_STATUS, true)
                            editor.putString(CommonUtils.LOGIN_USER_ID, userId)
                            editor.apply()
                            navigateToDashBoard()
                        } else if (statusCode == 400||statusCode == 401) {
                            showCustomAlert(
                                this@LoginActivity.resources.getString(R.string.login_alert_message),
                                CommonUtils.WEB_SERVICE_RESPONSE_CODE_401,
                                listOf(this@LoginActivity.resources.getString(R.string.alert_ok))
                            )
                        } else {
                            showCustomAlert(this@LoginActivity.resources.getString(R.string.api_server_alert_message), CommonUtils.WEB_SERVICE_RESPONSE_CODE_NON_401, listOf(this@LoginActivity.resources.getString(R.string.alert_ok)))
                        }
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }
                }

                override fun onFailure(call: Call<UserAuthResponse?>, t: Throwable) {
                    showCustomAlert(
                        this@LoginActivity.resources.getString(R.string.api_failure_alert_title),
                        CommonUtils.WEB_SERVICE_CALL_FAILED,
                        listOf(this@LoginActivity.resources.getString(R.string.alert_ok))
                    )
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                }
            })
        } else {
            showCustomAlert(
                this.resources.getString(R.string.network_alert_message),
                CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                listOf(this.resources.getString(R.string.alert_ok))
            )
        }
    }

    private fun navigateToDashBoard() {
        val mainIntent = Intent(this@LoginActivity, DashBoardActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    private fun validationAlert(alertMessage: String, buttonList: List<String>) {
        showCustomAlert(alertMessage, CommonUtils.VALIDATION_DIALOG, buttonList)
    }

    private fun showCustomAlert(
        alertMessage: String,
        functionality: String,
        buttonList: List<String>
    ) {
        val customDialogModel = CustomDialogModel(
            getString(R.string.app_name), alertMessage, null,
            buttonList
        )
        DialogUtils.showCustomAlert(this, customDialogModel, this, functionality)
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
                        validateUser(empIdStr, pinStr!!)
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

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(this.resources.getString(R.string.alert_ok), true)) {
            if (functionality.equals(CommonUtils.VALIDATION_DIALOG, true)) {
                //No action required, just display
            }
            if (functionality.equals(CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG, true)) {
                //No action required on internet connection error
            }
            if (functionality.equals(CommonUtils.WEB_SERVICE_RESPONSE_CODE_401, true)) {
                //No action required
            }
            if (functionality.equals(CommonUtils.WEB_SERVICE_CALL_FAILED, true)) {
                //No action required
            }
            if (functionality.equals(CommonUtils.WEB_SERVICE_RESPONSE_CODE_NON_401, true)) {
                //No action required
            }
        }
    }
}