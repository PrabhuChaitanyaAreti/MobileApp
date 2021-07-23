package com.vsoft.goodmankotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.vsoft.goodmankotlin.utils.BatteryUtil
import kotlinx.android.synthetic.main.activity_video_record_new.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class VideoRecordActivityNew : AppCompatActivity(),TextureView.SurfaceTextureListener ,View.OnClickListener{
    private var mCamera: Camera? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mOutputFile: File? = null
    private var isRecording = false
    private var isPauseResume =false

    private var parameters:Camera.Parameters?=null;
    private var  profile:CamcorderProfile?=null

    private val TAG = VideoRecordActivityNew::class.java.simpleName

    private var CAMERA_PERMISSION = Manifest.permission.CAMERA
    private var RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

    private var RC_PERMISSION = 101
    /**
     * Background and Countdown timer variables
     */
    private var Counter: CountDownTimer? = null
    private val minutesToGo: Long = 1
    private val initialMillisToGo = minutesToGo * 1000 * 60
    private var alertDialog: android.app.AlertDialog? = null

    private var recordDynamicTimer: Long = 20000
    private var recordmCountDown: CountDownTimer? = null
    private var recordSecondsLeft: Long = 0

    private  var isFlashMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_record_new)

        val batterLevel: Int = BatteryUtil.getBatteryPercentage(this@VideoRecordActivityNew)

        Log.d("TAG", "getBatteryPercentage  batterLevel $batterLevel")

        if (batterLevel >= 15) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (checkPermissions()){
                surface_view!!.surfaceTextureListener = this
            } else
            {
                requestPermissions()
            }
            videoRecordPlayPause.visibility= View.GONE
            settingsImgIcon.visibility= View.GONE

            videoOnlineImageButton.setOnClickListener(this)
            flashImgIcon.setOnClickListener(this)
            settingsImgIcon.setOnClickListener(this)
            videoRecordPlayPause.setOnClickListener(this)
        } else {
            batterLevelAlert()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(v: View?) {
        if(v==videoOnlineImageButton){
            if (isRecording) {
               stopRecording()
            } else {
                MediaPrepareTask().execute(null, null, null)
            }
        }else  if(v==settingsImgIcon){

        }else  if(v==flashImgIcon){
            if(isFlashMode){
                isFlashMode=false
                flashImgIcon.setImageResource(R.drawable.flash_off)
                parameters!!.setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                mCamera!!.setParameters(parameters!!)
            }else{
                flashImgIcon.setImageResource(R.drawable.flash_on)
                isFlashMode=true
                parameters!!.setFlashMode(Camera.Parameters.FLASH_MODE_ON)
                parameters!!.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                mCamera!!.setParameters(parameters!!)
            }
        }else  if(v==videoRecordPlayPause){
            if (isPauseResume) {
                isPauseResume = false
                videoRecordPlayPause.setImageResource(R.drawable.video_record_pause)
                mMediaRecorder!!.resume()

                println("resume recordSecondsLeft  $recordSecondsLeft")
                if (mMediaRecorder != null) {
                    mMediaRecorder!!.resume()
                }
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                if (recordmCountDown != null) {
                    recordmCountDown!!.cancel()
                    recordmCountDown = null
                }
                backgroundTimer()
                recordmCountDown = object : CountDownTimer(recordDynamicTimer, 1000) {
                    override fun onFinish() {
                        println("resume onFinish  ")
                        stopRecording()
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        println("resume onTick millisUntilFinished $millisUntilFinished")
                        recordSecondsLeft=millisUntilFinished;
                        recordSecondsLeft =
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                            )
                        //String time = "" + String.format(FORMAT, hr, min, sec);
                        timeleftTxt!!.setText("$recordSecondsLeft/20")
                    }
                }.start()
            } else {
                isPauseResume = true
                videoRecordPlayPause.setImageResource(R.drawable.video_record_play)
                mMediaRecorder!!.pause()

                println("pause recordSecondsLeft  $recordSecondsLeft")
                recordDynamicTimer = (recordSecondsLeft.toInt() * 1000).toLong()
                isPauseResume = true
                if (mMediaRecorder != null) {
                    if (isRecording) {
                        mMediaRecorder!!.pause()
                    }
                }
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                if (recordmCountDown != null) {
                    recordmCountDown!!.cancel()
                    recordmCountDown = null
                }
            }
        }
    }

    private fun stopRecording() {
        try {
            parameters!!.setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
            mCamera!!.setParameters(parameters)
            if (mMediaRecorder != null) {
                mMediaRecorder!!.stop() // stop the recording
            }
            flashImgIcon.setImageResource(R.drawable.flash_off)
            settingsImgIcon.visibility= View.GONE
            videoRecordPlayPause.visibility= View.GONE
            videoOnlineImageButton.setImageResource(R.drawable.video_record_start_new)
            releaseMediaRecorder() // release the MediaRecorder object
            mCamera!!.lock() // take camera access back from MediaRecorder
            isRecording = false
            releaseCamera()
            Log.d(TAG, "onVideoSaved ${mOutputFile.toString()}")
            val i = Intent(this@VideoRecordActivityNew, VideoPreviewActivity::class.java)
            i.putExtra("videoSavingFilePath", mOutputFile.toString())
            startActivity(i)
            finish()
        } catch (e: RuntimeException) {
            Log.d(
                TAG,
                "RuntimeException: stop() is called immediately after start()"
            )
            mOutputFile!!.delete()
        }
    }

    private fun batterLevelAlert() {
        val builder = android.app.AlertDialog.Builder(this@VideoRecordActivityNew)
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
        if (!this@VideoRecordActivityNew.isFinishing()) {
            try {
                alertDialog!!.show()
            } catch (e: WindowManager.BadTokenException) {
                Log.e("BadTokenException", e.toString())
            }
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
                if (allPermissionsGranted) {
                    surface_view!!.surfaceTextureListener = this
                } else{
                    permissionsNotGranted()
                }
            }
        }
    }
    private fun permissionsNotGranted() {
        AlertDialog.Builder(this).setTitle("Permissions required")
            .setMessage("These permissions are required to use this app. Please allow Camera and Audio permissions first")
            .setCancelable(false)
            .setPositiveButton("Grant") { dialog, which -> requestPermissions() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        releaseMediaRecorder()
        releaseCamera()
    }

    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder!!.reset()
            mMediaRecorder!!.release()
            mMediaRecorder = null
            mCamera!!.lock()
        }
    }

    private fun releaseCamera() {
        if (mCamera != null) {
            mCamera!!.release()
            mCamera = null
        }
        if (Counter != null) {
            Counter!!.cancel()
            Counter = null
        }
        if (recordmCountDown != null) {
            recordmCountDown!!.cancel()
            recordmCountDown = null
        }
    }

    private fun prepareVideoRecorder(): Boolean {
        mMediaRecorder = MediaRecorder()

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera!!.stopPreview()
        mCamera!!.unlock()
        mMediaRecorder!!.setCamera(mCamera)

        // Step 2: Set sources
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)

      /*  mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)*/

      /*  mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
*/
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder!!.setProfile(profile)

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO)
        if (mOutputFile == null) {
            return false
        }
        mMediaRecorder!!.setOutputFile(mOutputFile!!.path)
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder!!.prepare()
        } catch (e: IllegalStateException) {
            Log.d(
                TAG,
                "IllegalStateException preparing MediaRecorder: " + e.message
            )
            releaseMediaRecorder()
            return false
        } catch (e: IOException) {
            Log.d(
               TAG,
                "IOException preparing MediaRecorder: " + e.message
            )
            releaseMediaRecorder()
            return false
        }
        return true
    }
   inner class MediaPrepareTask() : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean? {
            // initialize video camera
            isRecording = if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder!!.start()
                runOnUiThread {
                    videoRecordPlayPause.setImageResource(R.drawable.video_record_pause)
                    settingsImgIcon.visibility= View.GONE
                    videoRecordPlayPause.visibility= View.VISIBLE
                    videoOnlineImageButton.setImageResource(R.drawable.video_record_stop_new)
                    recordmCountDown = object : CountDownTimer(recordDynamicTimer, 1000) {
                        override fun onFinish() {
                            stopRecording()
                        }
                        override fun onTick(millisUntilFinished: Long) {
                            recordSecondsLeft =
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                                )
                            timeleftTxt!!.setText("$recordSecondsLeft/20")
                        }
                    }.start()
                }
                true
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder()
                return false
            }
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if (!result!!) {
                finish()
            }
        }

    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
        try {
            /*
                   * Background timer initialize
                   */
            backgroundTimer()
        mCamera = CameraHelper.getDefaultCameraInstance()
        parameters = mCamera!!.parameters

        val mSupportedPreviewSizes = parameters!!.supportedPreviewSizes
        val mSupportedVideoSizes = parameters!!.supportedVideoSizes
        val optimalSize: Camera.Size? = CameraHelper.getOptimalVideoSize(
            mSupportedVideoSizes,
            mSupportedPreviewSizes, surface_view!!.width, surface_view!!.height
        )
         profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
        profile!!.videoFrameWidth = optimalSize!!.width
        profile!!.videoFrameHeight = optimalSize.height

        parameters!!.setPreviewSize(profile!!.videoFrameWidth, profile!!.videoFrameHeight)
        mCamera!!.parameters = parameters

            mCamera!!.setPreviewTexture(p0)
            surface_view!!.alpha = 1.0f
            mCamera!!.startPreview()
        } catch (e: IOException) {
            Log.e(
                TAG,
                "Surface texture is unavailable or unsuitable" + e.message
            )
        }

    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
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
            }

            override fun onFinish() {
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                val builder = android.app.AlertDialog.Builder(this@VideoRecordActivityNew)
                builder.setTitle(
                    this@VideoRecordActivityNew.getResources().getString(R.string.app_name)
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
                if (!this@VideoRecordActivityNew.isFinishing()) {
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
