package com.vsoft.goodmankotlin.utils

import android.annotation.TargetApi
import android.content.Context
import android.hardware.Camera
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Camera related utilities.
 */
class CameraHelper {
    companion object {

        val MEDIA_TYPE_IMAGE = 1
        val MEDIA_TYPE_VIDEO = 2

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
            val ASPECT_TOLERANCE = 0.1
            val targetRatio = w.toDouble() / h

            // Supported video sizes list might be null, it means that we are allowed to use the preview
            // sizes
            val videoSizes: List<Camera.Size>
            if (supportedVideoSizes != null) {
                videoSizes = supportedVideoSizes
            } else {
                videoSizes = previewSizes
            }
            var optimalSize: Camera.Size? = null

            // Start with max value and refine as we iterate over available video sizes. This is the
            // minimum difference between view and camera height.
            var minDiff = Double.MAX_VALUE

            // Target view height
            val targetHeight = h

            // Try to find a video size that matches aspect ratio and the target view size.
            // Iterate over all available sizes and pick the largest size that can fit in the view and
            // still maintain the aspect ratio.
            for (size: Camera.Size in videoSizes) {
                val ratio = size.width.toDouble() / size.height
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
                if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - targetHeight).toDouble()
                }
            }

            // Cannot find video size that matches the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE
                for (size: Camera.Size in videoSizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
                        optimalSize = size
                        minDiff = Math.abs(size.height - targetHeight).toDouble()
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
         * @return the default rear/back facing camera on the device. Returns null if camera is not
         * available.
         */
        fun getDefaultBackFacingCameraInstance(): Camera? {
            return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_BACK)
        }

        /**
         * @return the default front facing camera on the device. Returns null if camera is not
         * available.
         */
        fun getDefaultFrontFacingCameraInstance(): Camera? {
            return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT)
        }


        /**
         *
         * @param position Physical position of the camera i.e Camera.CameraInfo.CAMERA_FACING_FRONT
         * or Camera.CameraInfo.CAMERA_FACING_BACK.
         * @return the default camera on the device. Returns null if camera is not available.
         */
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        private fun getDefaultCamera(position: Int): Camera? {
            // Find the total number of cameras available
            val mNumberOfCameras = Camera.getNumberOfCameras()

            // Find the ID of the back-facing ("default") camera
            val cameraInfo = Camera.CameraInfo()
            for (i in 0 until mNumberOfCameras) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == position) {
                    return Camera.open(i)
                }
            }
            return null
        }

        /**
         * Creates a media file in the `Environment.DIRECTORY_PICTURES` directory. The directory
         * is persistent and available to other applications like gallery.
         *
         * @param type Media type. Can be video or image.
         * @return A file object pointing to the newly created file.
         */
        fun getOutputMediaFile(type: Int,context: Context): File? {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.
            /*  if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return  null;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraSample");*/

          var  sharedPreferences = context.getSharedPreferences(
                CommonUtils.SHARED_PREF_FILE,
                Context.MODE_PRIVATE
            )
        var userId=    sharedPreferences!!.getString(CommonUtils.LOGIN_USER_ID, "").toString()
            var opeartorId=    sharedPreferences!!.getString(CommonUtils.SAVE_OPERATOR_ID, "").toString()

            val mediaStorageDir: File
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                mediaStorageDir = File(
//                    Environment
//                        .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).path + "/Goodman/Videos"
//                )
              //  mediaStorageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.path+ "/Goodman/Videos")
                mediaStorageDir = context.getExternalFilesDir(null)!!
            } else {
//                mediaStorageDir = File(
//                    Environment
//                        .getExternalStorageDirectory().path + "/Goodman/Videos"
//                )
                //mediaStorageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.path+ "/Goodman/Videos")
                mediaStorageDir = context.getExternalFilesDir(null)!!
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
            if (type == MEDIA_TYPE_IMAGE) {
                mediaFile = File(
                    mediaStorageDir.path + File.separator +
                            "IMG_" + timeStamp + ".jpg"
                )
            } else if (type == MEDIA_TYPE_VIDEO) {
                mediaFile = File(
                    (mediaStorageDir.path + File.separator +
                            userId+"_"+opeartorId+"_"+ "VID_" + timeStamp + ".mp4")
                )

              /*  mediaFile = File(
                    (mediaStorageDir.path + File.separator +
                            "check1.mp4")
                )*/
            } else {
                return null
            }
            return mediaFile
        }
    }
}