package com.vsoft.goodmankotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.VideoCapture
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vsoft.goodmankotlin.utils.BatteryUtil
import kotlinx.android.synthetic.main.activity_video_record.*
import java.io.File
import java.util.concurrent.TimeUnit

class VideoRecordingActivity: AppCompatActivity(),View.OnClickListener {
    val TAG = VideoRecordingActivity::class.java.simpleName
    var isRecording = false

    var CAMERA_PERMISSION = Manifest.permission.CAMERA
    var RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

    var RC_PERMISSION = 101
    private var videoRecordingFilePath = ""
    private var videoSavingFilePath = ""
    /**
     * Background and Countdown timer variables
     */
    private var Counter: CountDownTimer? = null
    private val minutesToGo: Long = 1
    private val initialMillisToGo = minutesToGo * 1000 * 60
    private var alertDialog: android.app.AlertDialog? = null

    private val recordDynamicTimer: Long = 20000
    private var recordmCountDown: CountDownTimer? = null
    private var recordSecondsLeft: Long = 0

    var isFlashMode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_record)

        val recordFiles = ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_MOVIES)
        val storageDirectory = recordFiles[0]
         videoRecordingFilePath = "${storageDirectory.absoluteFile}/${System.currentTimeMillis()}_video.mp4"

        Log.d(TAG, "onCreate videoRecordingFilePath  $videoRecordingFilePath")
        val batterLevel: Int = BatteryUtil.getBatteryPercentage(this@VideoRecordingActivity)

        Log.d("TAG", "getBatteryPercentage  batterLevel $batterLevel")

        if (batterLevel >= 15) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (checkPermissions()) startCameraSession() else requestPermissions()

        videoRecordPlayPause.visibility= View.GONE
        settingsImgIcon.visibility= View.VISIBLE

        videoOnlineImageButton.setOnClickListener(this)
        flashImgIcon.setOnClickListener(this)
        settingsImgIcon.setOnClickListener(this)
        videoRecordPlayPause.setOnClickListener(this)
        } else {
            batterLevelAlert()
        }
    }
    private fun batterLevelAlert() {
        val builder = android.app.AlertDialog.Builder(this@VideoRecordingActivity)
        builder.setCancelable(false)
        builder.setTitle("Low Battery")
        builder.setMessage("15% of battery remaining.Please piugin charger")
        builder.setNeutralButton("Exit") { dialog, which ->
            dialog.dismiss()
            if (alertDialog!!.isShowing) {
                alertDialog!!.dismiss()
            }
            dialog.dismiss()
            try {
                val previewIntent = Intent()
                setResult(RESULT_CANCELED, previewIntent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        alertDialog = builder.create()
        if (!this@VideoRecordingActivity.isFinishing()) {
            try {
                alertDialog!!.show()
            } catch (e: WindowManager.BadTokenException) {
                Log.e("BadTokenException", e.toString())
            }
        }
    }
    override fun onClick(v: View?) {
        if(v==videoOnlineImageButton){
            if (isRecording) {
                camera_view.enableTorch(false)
                flashImgIcon.setImageResource(R.drawable.flash_off)
                isRecording = false
                settingsImgIcon.visibility= View.VISIBLE
                videoRecordPlayPause.visibility= View.GONE
                videoOnlineImageButton.setImageResource(R.drawable.video_record_start_new)
                camera_view.stopRecording()
                //videoOnlineImageButton.text = "Record Video"
              //  Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show()
            } else {
                settingsImgIcon.visibility= View.GONE
                videoRecordPlayPause.visibility= View.VISIBLE
                isRecording = true
                //videoOnlineImageButton.text = "Stop Recording"
                videoOnlineImageButton.setImageResource(R.drawable.video_record_stop_new)
                //Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
                recordVideo(videoRecordingFilePath)
                recordmCountDown = object : CountDownTimer(recordDynamicTimer, 1000) {
                    override fun onFinish() {
                        camera_view.stopRecording()

                    }

                    override fun onTick(millisUntilFinished: Long) {
                        //  long hr = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                        //long min = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                        //      TimeUnit.MILLISECONDS.toHours(millisUntilFinished));
                        recordSecondsLeft =
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                            )
                        //String time = "" + String.format(FORMAT, hr, min, sec);
                        timeleftTxt.setText("$recordSecondsLeft/20")
                    }
                }.start()
            }

        }else  if(v==settingsImgIcon){

        }else  if(v==flashImgIcon){
            if(isFlashMode){
                isFlashMode=false
                camera_view.enableTorch(false)
                flashImgIcon.setImageResource(R.drawable.flash_off)
            }else{
                flashImgIcon.setImageResource(R.drawable.flash_on)
                isFlashMode=true
                camera_view.enableTorch(true)
            }
        }else  if(v==videoRecordPlayPause){

        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION, RECORD_AUDIO_PERMISSION), RC_PERMISSION)
    }

    private fun checkPermissions(): Boolean {
        return ((ActivityCompat.checkSelfPermission(this, CAMERA_PERMISSION)) == PackageManager.PERMISSION_GRANTED
                && (ActivityCompat.checkSelfPermission(this, CAMERA_PERMISSION)) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            RC_PERMISSION -> {
                var allPermissionsGranted = false
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                        break
                    } else {
                        allPermissionsGranted = true
                    }
                }
                if (allPermissionsGranted) startCameraSession() else permissionsNotGranted()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCameraSession() {
        /*
        * Background timer initialize
        */
        backgroundTimer()
      /*  val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)

        }.build()

       val videoCapture = VideoCapture(videoCaptureConfig)*/

      //  camera_view.bindToLifecycle(this, preview, imageCapture, videoCapture)
        camera_view.bindToLifecycle(this)
    }

    private fun permissionsNotGranted() {
        AlertDialog.Builder(this).setTitle("Permissions required")
            .setMessage("These permissions are required to use this app. Please allow Camera and Audio permissions first")
            .setCancelable(false)
            .setPositiveButton("Grant") { dialog, which -> requestPermissions() }
            .show()
    }

    private fun recordVideo(videoRecordingFilePath: String) {
        camera_view.startRecording(File(videoRecordingFilePath), ContextCompat.getMainExecutor(this), object: VideoCapture.OnVideoSavedCallback {
            override fun onVideoSaved(file: File) {
                videoSavingFilePath=file.absolutePath
               // Toast.makeText(this@VideoRecordingActivity, "Recording Saved", Toast.LENGTH_SHORT).show()
                if (recordmCountDown != null) {
                    recordmCountDown!!.cancel()
                    recordmCountDown = null
                }
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                Log.d(TAG, "onVideoSaved $videoSavingFilePath")
                val i = Intent(this@VideoRecordingActivity, VideoPreviewActivity::class.java)
                i.putExtra("videoSavingFilePath", videoSavingFilePath)
                startActivity(i)
                finish()
            }

            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                //Toast.makeText(this@VideoRecordingActivity, "Recording Failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "onError $videoCaptureError $message")
            }
        })
    }

    /**
     * Background timer initialize
     */
    private fun backgroundTimer() {
        Counter = object : CountDownTimer(initialMillisToGo, 1000) {
            override fun onTick(millisUntilFinished1: Long) {
                val secs = (millisUntilFinished1 / 1000).toInt() % 60
                val minutes = (millisUntilFinished1 / (1000 * 60) % 60).toInt()
                Log.e("secs", secs.toString() + "")
                Log.e("minutes", minutes.toString() + "")
            }

            override fun onFinish() {
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                val builder = android.app.AlertDialog.Builder(this@VideoRecordingActivity)
                builder.setTitle(
                    this@VideoRecordingActivity.getResources().getString(R.string.app_name)
                )
                builder.setCancelable(false)
                builder.setMessage("No activity detected. Capture screen will close.Select Continue to continue capturing image.")
                builder.setPositiveButton(
                    "Continue"
                ) { dialog, which ->
                    dialog.dismiss()
                    if (alertDialog!!.isShowing) {
                        alertDialog!!.dismiss()
                    }
                    if (Counter != null) {
                        Counter!!.cancel()
                        Counter = null
                    }
                    backgroundTimer()
                }
                builder.setNegativeButton(
                    "Exit"
                ) { dialog, which ->
                    dialog.dismiss()
                    try {
                        val previewIntent = Intent()
                        setResult(RESULT_CANCELED, previewIntent)
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                alertDialog = builder.create()
                if (!this@VideoRecordingActivity.isFinishing()) {
                    try {
                        alertDialog!!.show()
                    } catch (e: WindowManager.BadTokenException) {
                        Log.e("BadTokenException", e.toString())
                    }
                }
            }
        }
        Counter!!.start()
    }

}