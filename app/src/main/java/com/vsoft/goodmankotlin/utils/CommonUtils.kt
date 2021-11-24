package com.vsoft.goodmankotlin.utils

import android.app.Activity
import android.content.Context
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import android.os.StatFs
import android.text.TextUtils
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.system.exitProcess


class CommonUtils {
    companion object {

        const val BATTERY_LEVEL_PERCENTAGE = 15

        const val IS_NEW_DIE = "is_new_die"

        const val BATTERY_DIALOG = "batteryDialog"
        const val PERMISSIONS_DIALOG = "permissionsDialog"
        const val TIMER_DIALOG = "timerDialog"
        const val BACK_PRESSED_DIALOG = "backPressedDialog"
        const val INTERNET_CONNECTION_ERROR_DIALOG = "internetConnectionErrorDialog"
        const val DIE_BOTH_DIALOG = "dieBoth"
        const val DIE_TOP_DIALOG = "dieTop"
        const val DIE_BOTTOM_DIALOG = "dieBottom"
        const val DIE_TOP_DETAIL_DIALOG = "dieTopDetails"
        const val DIE_BOTTOM_DETAIL_DIALOG = "dieBottomDetails"
        const val DIE_RECORD_OPTIONS_DIALOG = "dieRecordOptionsDialog"
        const val IS_VIDEO_RECORD_SCREEN = "isVideoRecordScreen"
        const val VALIDATION_DIALOG="validationDialog"
        const val WEB_SERVICE_RESPONSE_CODE_401="webServiceResponseCode401"
        const val WEB_SERVICE_RESPONSE_CODE_NON_401="webServiceResponseCodeNon401"
        const val WEB_SERVICE_CALL_FAILED="webServiceCallFailed"
        const val VIDEO_SYNC_DIALOG="videoSyncDialog"
        const val LOGOUT_DIALOG="logoutDialog"
        const val NO_OPERATOR_FUNCTIONALITY_IMPLEMENTED_DIALOG="noOperatorFunctionalityImplementedDialog"
        const val NO_DIE_DATA_DIALOG="noDieDialog"
        const val NO_DIE_ID_IN_LIST_FUNCTIONALITY="noDieIdInList"
        const val NO_PART_ID_RELATED_TO_DIE_ID_IN_LIST_FUNCTIONALITY="noPartIdRelatedToDieIdInList"

        const val VALIDATION_OPERATOR_SELECT_DIALOG = "validation_alert_select_dialog"

        const val VALIDATION_ALERT_DIE_ID_SELECT_DIALOG_NOT_AVAILABLE = "validation_alert_Die_ID_select_dialog_not_available"
        const val VALIDATION_ALERT_PART_ID_SELECT_DIALOG_NOT_AVAILABLE = "validation_alert_part_ID_select_dialog_not_available"

        const val MEMORY_DIALOG="memoryDialog"

        const val RETAKE_DIALOG="retakeDialog"

        const val VIDEO_SAVING_FILE_PATH="videoSavingFilePath"


        const val SHARED_PREF_FILE = "goodman_shared_preference"
        const val APP_CENTER_ANALYTICS_SECRET_KEY="41d26db7-77ac-4c92-bc27-5910ea601d14"
        const val LOGIN_STATUS="loginStatus"
        const val SPLASH_DURATION=5*1000

        const val LOGIN_USER_ID="login_user_id"


        const val IS_DIE_DATA_AVAILABLE="is_die_data_available"
        const val DIE_DATA="die_data"
        const val DIE_DATA_SYNC_TIME="die_data_sync_time"
        const val DIE_DATA_SYNC_DAYS=2
        const val OPERATORS_DATA="operators_data"


        const val SYNC_VIDEO_API_OPERATOR_ID="operator_id"
        const val SYNC_VIDEO_API_USER_ID="user_id"
        const val SYNC_VIDEO_API_DIE_ID="Die Id"
        const val SYNC_VIDEO_API_PART_ID="Part Id"
        const val SYNC_VIDEO_API_DIE_TOP_BOTTOM="top_bottom"
        const val SYNC_VIDEO_API_FILE_NAME="file_name"
        const val SYNC_VIDEO_API_META_DATA="meta_data"
        const val SYNC_VIDEO_API_FILE="file"

        const val SAVE_OPERATOR_ID="operatorIdStr"
        const val SAVE_DIE_ID="dieIdStr"
        const val SAVE_PART_ID="partIdStr"
        const val SAVE_IS_NEW_DIE="IsNewDie"
        const val SAVE_IS_FIRST_DIE_TOP="isFirstDieTop"
        const val SAVE_DIE_TYPE="dieTypeStr"
        const val SAVE_IS_DIE_TOP="isDieTop"
        const val SAVE_IS_DIE_BOTTOM="isDieBottom"
        const val SAVE_IS_DIE_TOP_DETAILS="isDieTopDetails"
        const val SAVE_IS_DIE_BOTTOM_DETAILS="isDieBottomDetails"

        const val ADD_DIE_SELECT="Select"
        const val ADD_DIE_TOP="top"
        const val ADD_DIE_BOTTOM="bottom"

        const val ADD_DIE_TOP_DETAILS="top_details"
        const val ADD_DIE_BOTTOM_DETAILS="bottom_details"


        const val DIE_TYPE_SELECT="Select Die Type"
        const val DIE_TYPE_TOP="Top"
        const val DIE_TYPE_BOTTOM="Bottom"

        const val OPERATOR_SELECTION_DIE_ID="DieID"
        const val OPERATOR_SELECTION_PART_ID="PartID"

        const val OPERATOR_SELECTION_0="Select Operator"
        const val OPERATOR_SELECTION_1="Operator1"
        const val OPERATOR_SELECTION_2="Operator2"
        const val OPERATOR_SELECTION_3="Operator3"
        const val OPERATOR_SELECTION_4="Operator4"
        const val OPERATOR_SELECTION_5="Operator5"
        const val OPERATOR_SELECTION_6="Operator6"
        const val OPERATOR_SELECTION_7="Operator7"
        const val OPERATOR_SELECTION_8="Operator8"
        const val OPERATOR_SELECTION_9="Operator9"
        const val OPERATOR_SELECTION_10="Operator10"

        const val RESPONSE="response"

        const val NO_PUNCH="NoPunch"
        const val PUNCH="Punch"
        const val UNDETECTED="UNDETECTED"

        const val ALL_PUNCHES="All Punches"
        const val MISSED_PUNCHES ="Missed Punches"
        const val INCORRECT_PUNCHES="Incorrect Punches"
        const val UNDETECTED_PUNCHES ="Undetected Punches"

        const val INCORRECT_PUNCH="Incorrect Punch"
        const val MISSED_PUNCH="MISSED Punch"



        fun getFileName(path: String): String {
            var filename = ""
            val pathContents = path.split("[\\\\/]").toTypedArray()
            if (pathContents != null) {
                val pathContentsLength = pathContents.size
                //lastPart: s659629384_752969_4472.jpg
                val lastPart = pathContents[pathContentsLength - 1]
                val lastPartContents = lastPart.split("\\.").toTypedArray()
                if (lastPartContents != null && lastPartContents.size > 1) {
                    val lastPartContentLength = lastPartContents.size
                    //filenames can contain . , so we assume everything before
                    //the last . is the name, everything after the last . is the
                    //extension
                    var name = ""
                    for (i in 0 until lastPartContentLength) {
                        if (i < lastPartContents.size - 1) {
                            name += lastPartContents[i]
                            if (i < lastPartContentLength - 2) {
                                name += "."
                            }
                        }
                    }
                    val extension = lastPartContents[lastPartContentLength - 1]
                    filename = "$name.$extension"
                }
            }
            return filename
        }


        /**
         * Alert dialog to navigate to app settings
         * to enable necessary permissions
         */
        fun showPermissionsAlert(context: Context?) {
            val builder = AlertDialog.Builder(
                context!!
            )
            builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS") { dialog, which ->
                    CameraUtils.openSettings(
                        context
                    )
                }
                .setNegativeButton("CANCEL") { dialog, which -> }.show()
        }



     private fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState().equals(
            Environment.MEDIA_MOUNTED
        )
    }

     private fun getAvailableInternalMemorySize(): String? {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return formatSize(availableBlocks * blockSize)
    }

    fun getTotalInternalMemorySize(): String? {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return formatSize(totalBlocks * blockSize)
    }

     private fun getAvailableExternalMemorySize(): String? {
        return if (externalMemoryAvailable()) {
            val path: File = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            formatSize(availableBlocks * blockSize)
        } else {
            "10"
        }
    }

    fun getTotalExternalMemorySize(): String? {
        return if (externalMemoryAvailable()) {
            val path: File = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            formatSize(totalBlocks * blockSize)
        } else {
            "10"
        }
    }

     private fun formatSize(size: Long): String {
        var size = size
        var suffix: String? = null
        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024
            }
        }
        val resultBuffer = StringBuilder(size.toString())
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }

    fun checkMemory(): Boolean {
        var isMemory = false
        val internalMemory: String? = getAvailableInternalMemorySize()
        val externalMemory: String? = getAvailableExternalMemorySize()
        println(" checkMemory internalMemory *** $internalMemory")
        println("  checkMemory externalMemory *** $externalMemory")
        if (!internalMemory!!.contains("KB") || !externalMemory!!.contains("KB")) {
            var internalMemory1 = internalMemory.replace("MB".toRegex(), "")
            var externalMemory1 = externalMemory!!.replace("MB".toRegex(), "")
            if (internalMemory1.contains(",")) {
                internalMemory1 = internalMemory1.replace(",".toRegex(), "")
            }
            if (externalMemory1.contains(",")) {
                externalMemory1 = externalMemory1.replace(",".toRegex(), "")
            }
            println(" checkMemory internalMemory1 ***  $internalMemory1")
            println("  checkMemory externalMemory1 ***  $externalMemory1")

            isMemory =
                !(Integer.parseInt(internalMemory1) <500  || Integer.parseInt(externalMemory1) <500)
        } else {
            isMemory = false
        }
        println("  checkMemory isMemory $isMemory")
        return isMemory
    }


    /**
     * delete the video path from file path.
     *
     * @param filePath is a parameter
     * @return returns value
     */
    fun deletePath(filePath: String) {
        try{
            if (!TextUtils.isEmpty(filePath) && filePath.isNotEmpty()) {
                //clearImage(filePath);
                val imgFile = File(filePath)
                if (imgFile.exists()) {
                     imgFile.delete()
                }
            }
        } catch (e: Exception) {
            //ADD THE ERROR MESSAGE
            e.printStackTrace()
        }
    }

    fun appExit(activity: Activity){
    activity.finishAffinity();
    exitProcess(0)
}
    }


}