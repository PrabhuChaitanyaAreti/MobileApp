@file:Suppress("LocalVariableName")

package com.vsoft.goodmankotlin.utils

import android.content.Context
import android.hardware.Camera
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


/**
 * Camera related utilities.
 */
class CameraHelper {
    companion object {

        private const val MEDIA_TYPE_IMAGE = 1
        const val MEDIA_TYPE_VIDEO = 2

        /**
         * Iterate over supported camera video sizes to see which one best fits the
         * dimensions of the given view while maintaining the aspect ratio. If none can,
         * be lenient with the aspect ratio.
         *
         * @param supportedVideoSizes Supported camera video sizes.
         * @param previewSizes Supported camera preview sizes.
         * @param w     The width of the view.
         * @param h     The height of the view.
         * @return Best match camera video size to fit in the view.
         */
        fun getOptimalVideoSize(
            supportedVideoSizes: List<Camera.Size>?,
            previewSizes: List<Camera.Size>, w: Int, h: Int
        ): Camera.Size? {
            // Use a very small tolerance because we want an exact match.
            val ASPECTTOLERANCE = 0.1
            val targetRatio = w.toDouble() / h

            // Supported video sizes list might be null, it means that we are allowed to use the preview
            // sizes
            val videoSizes: List<Camera.Size> = supportedVideoSizes ?: previewSizes
            var optimalSize: Camera.Size? = null

            // Start with max value and refine as we iterate over available video sizes. This is the
            // minimum difference between view and camera height.
            var minDiff = Double.MAX_VALUE

            // Target view height

            // Try to find a video size that matches aspect ratio and the target view size.
            // Iterate over all available sizes and pick the largest size that can fit in the view and
            // still maintain the aspect ratio.
            for (size: Camera.Size in videoSizes) {
                val ratio = size.width.toDouble() / size.height
                if (abs(ratio - targetRatio) > ASPECTTOLERANCE) continue
                if (abs(size.height - h) < minDiff && previewSizes.contains(size)) {
                    optimalSize = size
                    minDiff = abs(size.height - h).toDouble()
                }
            }

            // Cannot find video size that matches the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE
                for (size: Camera.Size in videoSizes) {
                    if (abs(size.height - h) < minDiff && previewSizes.contains(size)) {
                        optimalSize = size
                        minDiff = abs(size.height - h).toDouble()
                    }
                }
            }
            return optimalSize
        }

        /**
         * @return the default camera on the device. Return null if there is no camera on the device.
         */
        fun getDefaultCameraInstance(): Camera? {
            return Camera.open()
        }

        /**
         * Creates a media file in the `Environment.DIRECTORY_PICTURES` directory. The directory
         * is persistent and available to other applications like gallery.
         *
         * @param type Media type. Can be video or image.
         * @return A file object pointing to the newly created file.
         */
        fun getOutputMediaFile(type: Int,context: Context): File? {

          val sharedPreferences = context.getSharedPreferences(
                CommonUtils.SHARED_PREF_FILE,
                Context.MODE_PRIVATE
            )
            val dieIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_ID, "").toString()
            val partIdStr = sharedPreferences.getString(CommonUtils.SAVE_PART_ID, "").toString()
            val dieTypeStr = sharedPreferences.getString(CommonUtils.SAVE_DIE_TYPE, "").toString()
            val userId=    sharedPreferences.getString(CommonUtils.LOGIN_USER_ID, "").toString()
            var operatorId=    sharedPreferences.getString(CommonUtils.SAVE_OPERATOR_ID, "").toString()

            val mediaStorageDir: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.getExternalFilesDir(null)!!
            } else {
                context.getExternalFilesDir(null)!!
            }
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("CameraSample", "failed to create directory")
                    return null
                }
            }

            // Create a media file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val mediaFile: File
            when (type) {
                MEDIA_TYPE_IMAGE -> {
                    mediaFile = File(
                        mediaStorageDir.path + File.separator +
                                "IMG_" + timeStamp + ".jpg"
                    )
                }
                MEDIA_TYPE_VIDEO -> {
        //                mediaFile = File(
        //                    (mediaStorageDir.path + File.separator +
        //                            userId+"_"+operatorId+"_"+dieIdStr+"_"+partIdStr+"_"+dieTypeStr+"_"+ "VID_" + timeStamp + ".mp4")
        //                )

                    mediaFile = File(
                        (mediaStorageDir.path + File.separator +
                                userId+"_"+dieIdStr+"_"+partIdStr+"_"+dieTypeStr+"_"+ "VID_" + timeStamp + ".mp4")
                    )

                }
                else -> {
                    return null
                }
            }
            return mediaFile
        }
    }
}