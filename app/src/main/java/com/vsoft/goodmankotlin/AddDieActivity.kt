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
import com.vsoft.goodmankotlin.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_add_die.*

class AddDieActivity : AppCompatActivity() , View.OnClickListener , View.OnTouchListener {

    private lateinit var dieIdStr: String
    private lateinit var partIdStr: String
    private lateinit var dieTypeStr:String
    private lateinit var alertDialog: AlertDialog
    private lateinit var sharedPreferences: SharedPreferences
  /*  private var dieTypeArray: Array<String> = arrayOf(
        this@AddDieActivity.resources.getString(R.string.add_die_die_type_select),
        this@AddDieActivity.resources.getString(R.string.add_die_die_type_top),
        this@AddDieActivity.resources.getString(R.string.add_die_die_type_bottom))*/

    private var dieTypeArray: Array<String> = arrayOf(CommonUtils.DIE_TYPE_SELECT,CommonUtils.DIE_TYPE_TOP,CommonUtils.DIE_TYPE_BOTTOM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_die)

        sharedPreferences = this.getSharedPreferences(CommonUtils.SHARED_PREF_FILE,
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

            if (dieIdStr.isNotEmpty() && !TextUtils.isEmpty(dieIdStr) && dieIdStr != "null") {
                if (partIdStr.isNotEmpty() && !TextUtils.isEmpty(partIdStr) && partIdStr != "null") {
                    if (dieTypeStr.isNotEmpty() && !TextUtils.isEmpty(dieTypeStr) && dieTypeStr != "null") {
                        if(dieTypeStr.contains(CommonUtils.ADD_DIE_SELECT)){
                            validationAlert(this@AddDieActivity.resources.getString(R.string.add_die_alert_die_type_select))
                        }else{
                            Log.d("TAG", "AddDieActivity   dieIdStr $dieIdStr")
                            Log.d("TAG", "AddDieActivity   partIdStr $partIdStr")
                            Log.d("TAG", "AddDieActivity   dieTypeStr $dieTypeStr")

                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString(CommonUtils.SAVE_DIE_ID, dieIdStr)
                            editor.putString(CommonUtils.SAVE_PART_ID, partIdStr)
                            editor.putBoolean(CommonUtils.SAVE_IS_NEW_DIE, true)

                            if(dieTypeStr.equals(CommonUtils.ADD_DIE_TOP,true)){
                                editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_TOP)
                                editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, true)
                                editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, false)
                            }else{
                                editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_BOTTOM)
                                editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, false)
                                editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, true)
                            }
                            editor.apply()

                            val mainIntent =
                                Intent(this@AddDieActivity, VideoRecordActivityNew::class.java)
                            startActivity(mainIntent)
                        }
                    }else{
                        validationAlert(this@AddDieActivity.resources.getString(R.string.add_die_alert_die_type_select))
                    }
                }else{
                    finish()
                    validationAlert(this@AddDieActivity.resources.getString(R.string.add_die_alert_enter_part_id))
                }
            }else{
                validationAlert(this@AddDieActivity.resources.getString(R.string.add_die_alert_enter_die_id))
            }
        }

    }
    private fun validationAlert(alertMessage: String) {
        val builder = AlertDialog.Builder(this@AddDieActivity)
        builder.setCancelable(false)
        builder.setTitle(this@AddDieActivity.resources.getString(R.string.app_name))
        builder.setMessage(alertMessage)
        builder.setNeutralButton(this@AddDieActivity.resources.getString(R.string.alert_ok)) { dialog, which ->
            dialog.dismiss()
            if (alertDialog.isShowing) {
                alertDialog.dismiss()
            }
            dialog.dismiss()
        }
        alertDialog = builder.create()
        if (!this@AddDieActivity.isFinishing) {
            try {
                alertDialog.show()
            } catch (e: WindowManager.BadTokenException) {
                Log.e("BadTokenException", e.toString())
            }
        }
    }


}