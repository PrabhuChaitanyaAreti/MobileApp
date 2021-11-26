package com.vsoft.goodmankotlin.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.vsoft.goodmankotlin.R


class DialogUtils {
    companion object {
    fun showNormalAlert(context: Context?, title: String?, message: String?) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton(
            "Ok"
        ) { arg0, arg1 -> }
        alertDialogBuilder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
        @SuppressLint("UseCompatLoadingForDrawables")
        fun showCustomAlert(context: Context?, customDialogModel: CustomDialogModel, customDialogCallback: CustomDialogCallback, functionality:String){
            val factory = LayoutInflater.from(context)
            val customDialogView: View = factory.inflate(R.layout.custom_dialog_layout, null)
            val customDialog = AlertDialog.Builder(context).create()
            val title=customDialogView.findViewById<TextView>(R.id.title)
            val message=customDialogView.findViewById<TextView>(R.id.message)
            val llButtons=customDialogView.findViewById<LinearLayout>(R.id.llButtons)
            title.text = customDialogModel.title
            message.text = customDialogModel.message
            val buttonsList=customDialogModel.buttons
            if(buttonsList.isNotEmpty()){
                customDialog.setCancelable(false)
                val buttonListIterator = buttonsList.iterator()
                while (buttonListIterator.hasNext()) run {
                    val button = Button(context)
                   val str= buttonListIterator.next()
                    if (context != null) {
                        if(str == context.resources.getString(R.string.alert_cancel) ||
                                str == context.resources.getString(R.string.alert_no)  ||
                                str == context.resources.getString(R.string.alert_exit) ){
                            button.background=context?.resources?.getDrawable(R.drawable.button_bg_red)
                        }else{
                            button.background=context?.resources?.getDrawable(R.drawable.button_bg)
                        }
                    }else{
                        button.background=context?.resources?.getDrawable(R.drawable.button_bg)
                    }
                    button.text = str
                    button.setPadding(20,0,20,0)
                    button.setTextAppearance(context, R.style.TextAppearance_AppCompat_Medium)

                    button.setTextColor(Color.parseColor("#FFFFFF"))
                    button.isAllCaps=false
                    button.setOnClickListener {
                        customDialogCallback.onCustomDialogButtonClicked(
                            button.text as String,
                            functionality
                        )
                        customDialog.dismiss()
                    }
                    llButtons?.addView(button)
                }
            }
            customDialog.setView(customDialogView)
            customDialog.show()
        }

        @JvmStatic
        fun showNormalAlert1(activityContext: Context, s: String, s1: String) {
            val alertDialogBuilder = AlertDialog.Builder(activityContext)
            alertDialogBuilder.setTitle("Fail")
            alertDialogBuilder.setMessage("P")
            alertDialogBuilder.setPositiveButton(
                "Ok"
            ) { arg0, arg1 -> }
            alertDialogBuilder.setNegativeButton(
                "Cancel"
            ) { dialog, which -> }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()

        }
    }
}