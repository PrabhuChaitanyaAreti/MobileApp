package com.vsoft.goodmankotlin.utils

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.vsoft.goodmankotlin.CameraActivity.Companion.screenHeight
import com.vsoft.goodmankotlin.CameraActivity.Companion.screenWidth
import java.io.IOException
import kotlin.math.abs


@SuppressLint("ViewConstructor")
class CameraPreview(context: Context?, camera: Camera?) :
    SurfaceView(context), SurfaceHolder.Callback {
    private val mHolder: SurfaceHolder
    private var mCamera: Camera?
    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            Log.d("TAG", "surfaceCreated: ")
            // create the surface and start camera preview
            if (mCamera == null) {
                val parameters = mCamera!!.parameters
                val mSupportedPreviewSizes = parameters.supportedPreviewSizes
                val previewSize =
                    getOptimalPreviewSize(mSupportedPreviewSizes, screenWidth, screenHeight)
                Log.d(
                    "TAG",
                    "previewSize width and height " + previewSize!!.width + "x" + previewSize.height
                )
                parameters.setPreviewSize(previewSize.width, previewSize.height)
                parameters.setPictureSize(previewSize.width, previewSize.height)
                // parameters.setZoom(Camera.Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX);
                val focusModes = parameters.supportedFocusModes
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                }
                //mCamera!!.parameters = parameters
                mCamera!!.setPreviewDisplay(holder)
                mCamera!!.startPreview()
            }
        } catch (e: IOException) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.message)
        }
    }

    fun refreshCamera(camera: Camera?) {
        Log.d("TAG", "refreshCamera: ")
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }
        // stop preview before making changes
        try {
            mCamera!!.stopPreview()
            //  mCamera.release();
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera)
        try {
            val parameters = mCamera!!.parameters
            val mSupportedPreviewSizes = parameters.supportedPreviewSizes
            val previewSize =
                getOptimalPreviewSize(mSupportedPreviewSizes, screenWidth, screenHeight)
            Log.d(
                "TAG",
                "previewSize width and height " + previewSize!!.width + "x" + previewSize.height
            )
            parameters.setPreviewSize(previewSize.width, previewSize.height)
            parameters.setPictureSize(previewSize.width, previewSize.height)
            // parameters.setZoom(Camera.Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX);
            val focusModes = parameters.supportedFocusModes
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            }
            //mCamera!!.parameters = parameters
            mCamera!!.setPreviewDisplay(mHolder)
            mCamera!!.startPreview()
        } catch (e: Exception) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.message)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w1: Int, h1: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d("TAG", "surfaceChanged: ")
        // refreshCamera(mCamera,w,h);
        refreshCamera(mCamera)
    }

    private fun setCamera(camera: Camera?) {
        //method to set a camera instance
        Log.d("TAG", "setCamera: ")
        mCamera = camera
        val parameters = mCamera!!.parameters
        val mSupportedPreviewSizes = parameters.supportedPreviewSizes
        val previewSize = getOptimalPreviewSize(mSupportedPreviewSizes, screenWidth, screenHeight)
        Log.d(
            "TAG",
            "previewSize width and height " + previewSize!!.width + "x" + previewSize.height
        )
        parameters.setPreviewSize(previewSize.width, previewSize.height)
        parameters.setPictureSize(previewSize.width, previewSize.height)
        //  parameters.setZoom(Camera.Parameters.FOCUS_DISTANCE_OPTIMAL_INDEX);
        val focusModes = parameters.supportedFocusModes
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        }
        //mCamera!!.parameters = parameters
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // TODO Auto-generated method stub
        Log.d("TAG", "surfaceDestroyed: ")
    }

    companion object {
        fun getOptimalPreviewSize(sizes: List<Camera.Size>, w: Int, h: Int): Camera.Size? {
            val aspectTolerance = 0.1
            val targetRatio = h.toDouble() / w
            //if (sizes == null) return null
            var optimalSize: Camera.Size? = null
            var minDiff = Double.MAX_VALUE
            for (size in sizes) {
                val ratio = size.width.toDouble() / size.height
                if (abs(ratio - targetRatio) > aspectTolerance) continue
                if (abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = abs(size.height - h).toDouble()
                }
            }
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE
                for (size in sizes) {
                    if (abs(size.height - h) < minDiff) {
                        optimalSize = size
                        minDiff = abs(size.height - h).toDouble()
                    }
                }
            }
            return optimalSize
        }
    }

    init {
        Log.d("TAG", "CameraPreview: ")
        mCamera = camera
        mHolder = holder
        mHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }
}