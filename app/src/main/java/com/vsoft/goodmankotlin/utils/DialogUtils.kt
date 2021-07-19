package com.vsoft.goodmankotlin.utils

import android.app.AlertDialog
import android.content.Context

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
    }
}