package com.vsoft.goodmankotlin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import kotlinx.android.synthetic.main.activity_add_die.*

class AddDieActivity : AppCompatActivity() , View.OnClickListener , View.OnTouchListener {

    private var dieIdStr:String? =  ""
    private var partIdStr:String? = ""
    private var dieTypeStr:String?=""

    private lateinit var alertDialog: AlertDialog

    private val sharedPrefFile = "kotlinsharedpreference"
    var sharedPreferences: SharedPreferences?=null

    var dieTypeArray: Array<String> = arrayOf("Select Die Type", "Top", "Bottom")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_die)

        sharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)

        dieIdEditText.setOnTouchListener(this)
        partIdEditText.setOnTouchListener(this)

        continueButton.setOnClickListener(this)

        val langAdapter1 = ArrayAdapter<CharSequence>(
            this@AddDieActivity,
            R.layout.spinner_text,
            dieTypeArray
        )
        langAdapter1.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        dieTypeSpinner.adapter = langAdapter1

    }

    override fun onTouch(view: View?, p1: MotionEvent?): Boolean {
        if (view === dieIdEditText) {
            dieIdEditText!!.isFocusable = true
            dieIdEditText!!.isFocusableInTouchMode = true
        } else if (view === partIdEditText) {
            partIdEditText!!.isFocusable = true
            partIdEditText!!.isFocusableInTouchMode = true
        }
        return false
    }

    override fun onClick(view: View?) {
        if(view == continueButton){
            dieIdStr = dieIdEditText!!.text.toString()
            partIdStr = partIdEditText!!.text.toString()
            dieTypeStr = dieTypeSpinner.selectedItem.toString()

            if (dieIdStr != null && dieIdStr!!.isNotEmpty() && !TextUtils.isEmpty(dieIdStr) && dieIdStr != "null") {
                if (partIdStr != null && partIdStr!!.isNotEmpty() && !TextUtils.isEmpty(partIdStr) && partIdStr != "null") {
                    if (dieTypeStr != null && dieTypeStr!!.isNotEmpty() && !TextUtils.isEmpty(dieTypeStr) && dieTypeStr != "null") {
                        if(dieTypeStr!!.contains("Select")){
                            validationAlert("Please Select Die Type")
                        }else{
                            Log.d("TAG", "AddDieActivity   dieIdStr $dieIdStr")
                            Log.d("TAG", "AddDieActivity   partIdStr $partIdStr")
                            Log.d("TAG", "AddDieActivity   dieTypeStr $dieTypeStr")

                            val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                            editor.putString("dieIdStr", dieIdStr)
                            editor.putString("partIdStr", partIdStr)
                            editor.putBoolean("IsNewDie", true)
                            if(dieTypeStr.equals("top",true)){
                                editor.putString("dieTypeStr", "top")
                                editor.putBoolean("isDieTop", true)
                                editor.putBoolean("isDieBottom", false)
                            }else{
                                editor.putString("dieTypeStr", "bottom")
                                editor.putBoolean("isDieTop", false)
                                editor.putBoolean("isDieBottom", true)
                            }
                            editor.apply()

                            val mainIntent =
                                Intent(this@AddDieActivity, VideoRecordActivityNew::class.java)
                            startActivity(mainIntent)
                            finish()
                        }
                    }else{
                        validationAlert("Please Select Die Type")
                    }
                }else{
                    validationAlert("Please enter Part Id")
                }
            }else{
                validationAlert("Please enter Die Id")
            }
        }
    }
    private fun validationAlert(alertMessage: String) {
        val builder = AlertDialog.Builder(this@AddDieActivity)
        builder.setCancelable(false)
        builder.setTitle(this@AddDieActivity.getResources().getString(R.string.app_name))
        builder.setMessage(alertMessage)
        builder.setNeutralButton("Ok") { dialog, which ->
            dialog.dismiss()
            if (alertDialog!!.isShowing) {
                alertDialog!!.dismiss()
            }
            dialog.dismiss()
        }
        alertDialog = builder.create()
        if (!this@AddDieActivity.isFinishing) {
            try {
                alertDialog?.show()
            } catch (e: WindowManager.BadTokenException) {
                Log.e("BadTokenException", e.toString())
            }
        }
    }


}