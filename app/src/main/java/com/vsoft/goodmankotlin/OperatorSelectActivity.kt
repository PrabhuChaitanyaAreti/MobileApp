@file:Suppress("ControlFlowWithEmptyBody")

package com.vsoft.goodmankotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.Gson
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.*
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils
import java.util.*
import kotlin.collections.ArrayList

class OperatorSelectActivity : Activity(), CustomDialogCallback {
    private lateinit var mainLyt: LinearLayout
    private lateinit var operatorListSpinner: Spinner
    private lateinit var btnContinue: Button
    private var responses: List<String> = java.util.ArrayList()
    private var isNewDie = false
    private lateinit var sharedPreferences: SharedPreferences
    private var isDieDataAvailable = false
    private var operatorsData = ""
    private var dieDataSyncTime = ""
    private var operatorStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_select)

        isNewDie = intent.getBooleanExtra(CommonUtils.IS_NEW_DIE, false)

        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        btnContinue = findViewById(R.id.btnContinue)

        isDieDataAvailable = sharedPreferences.getBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, false)
        operatorsData = sharedPreferences.getString(CommonUtils.OPERATORS_DATA, "").toString()
        dieDataSyncTime = sharedPreferences.getString(CommonUtils.DIE_DATA_SYNC_TIME, "").toString()

        val dataModels: ArrayList<String> = ArrayList()
        dataModels.add("Select Operator")

        if (isDieDataAvailable) {
            val gson = Gson()
            val operatorList: OperatorList =
                gson.fromJson(operatorsData, OperatorList::class.java)
            responses = operatorList.operatorlist

            val iterator = responses.listIterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                dataModels.add("Operator $item")
            }
        }else{
            dataModels.add("Operator 1")
            dataModels.add("Operator 2")
            dataModels.add("Operator 3")
            dataModels.add("Operator 4")
            dataModels.add("Operator 5")
            dataModels.add("Operator 6")
            dataModels.add("Operator 7")
            dataModels.add("Operator 8")
            dataModels.add("Operator 9")
            dataModels.add("Operator 10")
        }

        operatorListSpinner = findViewById(R.id.operatorListSpinner)

        val langAdapter1 = ArrayAdapter(
            this@OperatorSelectActivity,
            R.layout.spinner_text,
            dataModels
        )
        langAdapter1.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        operatorListSpinner.adapter = langAdapter1

        mainLyt = findViewById(R.id.main_lyt)
        mainLyt.setOnClickListener { AddDieOperatorSelectActivity.hideSoftKeyboard(this@OperatorSelectActivity) }

        btnContinue.setOnClickListener {
            operatorStr = operatorListSpinner.selectedItem.toString()
            if (operatorStr.isNotEmpty() && !TextUtils.isEmpty(operatorStr) && operatorStr != "null") {
                if (operatorStr.contains(CommonUtils.ADD_DIE_SELECT)) {
                    showCustomAlert(
                        this@OperatorSelectActivity.resources.getString(R.string.app_name),
                        this@OperatorSelectActivity.resources.getString(R.string.op_se_alert_message_select_operator),
                        CommonUtils.VALIDATION_OPERATOR_SELECT_DIALOG,
                        listOf(this@OperatorSelectActivity.resources.getString(R.string.alert_ok))
                    )
                } else {
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString(CommonUtils.SAVE_OPERATOR_ID, operatorStr)
                    editor.apply()

                    val mainIntent =
                        Intent(
                            this@OperatorSelectActivity,
                            AddDieOperatorSelectActivity::class.java
                        )
                    mainIntent.putExtra(CommonUtils.IS_NEW_DIE, isNewDie)
                    startActivity(mainIntent)
                }
            } else {
                showCustomAlert(
                    this@OperatorSelectActivity.resources.getString(R.string.app_name),
                    this@OperatorSelectActivity.resources.getString(R.string.op_se_alert_message_select_operator),
                    CommonUtils.VALIDATION_OPERATOR_SELECT_DIALOG,
                    listOf(this@OperatorSelectActivity.resources.getString(R.string.alert_ok))
                )
            }
        }
    }

    private fun showCustomAlert(
        alertTitle: String,
        alertMessage: String,
        functionality: String,
        buttonList: List<String>
    ) {
        val customDialogModel = CustomDialogModel(
            alertTitle, alertMessage, null,
            buttonList
        )
        DialogUtils.showCustomAlert(this, customDialogModel, this, functionality)
    }

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(
                this@OperatorSelectActivity.resources.getString(R.string.alert_ok),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG, true)) {
                //No action required. Just exit dialog.
            } else if (functionality.equals(CommonUtils.VALIDATION_OPERATOR_SELECT_DIALOG, true)) {
                //No action required. Just exit dialog.
            }
        }
    }


    companion object {
        fun hideSoftKeyboard(activity: Activity) {
            val inputMethodManager = activity.getSystemService(
                INPUT_METHOD_SERVICE
            ) as InputMethodManager
            if (inputMethodManager.isAcceptingText) {
                inputMethodManager.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken,
                    0
                )
            }
        }
    }
}