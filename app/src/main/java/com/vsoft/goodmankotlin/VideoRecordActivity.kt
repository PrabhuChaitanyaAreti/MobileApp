package com.vsoft.goodmankotlin

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.utils.BatteryUtil
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils
import java.io.File
import java.io.IOException
import java.util.*
import android.media.AudioManager
import android.text.TextUtils
import com.vsoft.goodmankotlin.utils.CameraHelper

class VideoRecordActivity : AppCompatActivity(), TextureView.SurfaceTextureListener,
    View.OnClickListener, CustomDialogCallback {
    private var mCamera: Camera? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mOutputFile: File? = null
    private var isRecording = false
    private var isPauseResume = false

    private var parameters: Camera.Parameters? = null
    private var profile: CamcorderProfile? = null

    private val tag = VideoRecordActivity::class.java.simpleName

    private var CAMERA_PERMISSION = Manifest.permission.CAMERA
   // private var RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

    private var RC_PERMISSION = 101
    private lateinit var progressDialog: ProgressDialog
    /**
     * Background and Countdown timer variables
     */
    private var Counter: CountDownTimer? = null
    private val idleTime: Long = 5
    private val idleTimeInMillis = idleTime * 1000 * 60

    private var videoMaxTimeInMillis: Long = 2*60*1000
    private var totalTimer: Long = videoMaxTimeInMillis/1000
    private var recordMCountDown: CountDownTimer? = null
    private var recordSecondsLeft: Long = 0

    private var isFlashMode = false

    private var cameraSizesArray = emptyArray<String?>()
    private var cameraFPSArray: Array<String>? = null

    private var surface_view: TextureView? = null
    private var settingsImgIcon: ImageView? = null
    private var videoOnlineImageButton: ImageButton? = null
    private var videoRecordPlayPause: ImageButton? = null
    private var flashImgIcon: ImageView? = null
    private var timeLeftTxt: TextView? = null
    private var partIdTxt: TextView? = null
    private var dieIdTxt: TextView? = null
    private var dieTypeTxt: TextView? = null

    private var sharedPreferences: SharedPreferences? = null

    private var dieIdStr = ""
    private var partIdStr = ""
    private var dieTypeStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_record)
     /*   val finish= intent.getBooleanExtra("finish", false)
        if(finish) {
            finish();
            return;
        }*/
        //CommonUtils.freeMemory()

        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )

        dieIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_ID, "").toString()
        partIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_PART_ID, "").toString()
        dieTypeStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_TYPE, "").toString()

        Log.d("TAG", "VideoRecordActivity  sharedPreferences  dieIdStr $dieIdStr")
        Log.d("TAG", "VideoRecordActivity sharedPreferences  partIdStr $partIdStr")
        Log.d("TAG", "VideoRecordActivity sharedPreferences  dieTypeStr $dieTypeStr")

        surface_view = findViewById(R.id.surface_view)
        settingsImgIcon = findViewById(R.id.settingsImgIcon)
        videoOnlineImageButton = findViewById(R.id.videoOnlineImageButton)
        videoRecordPlayPause = findViewById(R.id.videoRecordPlayPause)
        flashImgIcon = findViewById(R.id.flashImgIcon)
        timeLeftTxt = findViewById(R.id.timeleftTxt)
        partIdTxt = findViewById(R.id.partIdTxt)
        dieIdTxt = findViewById(R.id.dieIdTxt)
        dieTypeTxt = findViewById(R.id.dieTypeTxt)


        initProgress()

        val batterLevel: Int = BatteryUtil.getBatteryPercentage(this@VideoRecordActivity)

        Log.d("TAG", "getBatteryPercentage  batterLevel $batterLevel")

        if (batterLevel >= 15) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (checkPermissions()) {
                surface_view!!.surfaceTextureListener = this
            } else {
                requestPermissions()
            }
            videoRecordPlayPause!!.visibility = View.GONE
            settingsImgIcon!!.visibility = View.GONE


            if (dieIdStr.isNotEmpty() && !TextUtils.isEmpty(dieIdStr) && dieIdStr != "null") {
                dieIdTxt!!.text= "Die ID: $dieIdStr"
                dieIdTxt!!.visibility=View.VISIBLE
            }else{
                dieIdTxt!!.visibility=View.GONE
            }
            if (partIdStr.isNotEmpty() && !TextUtils.isEmpty(partIdStr) && partIdStr != "null") {
                partIdTxt!!.text= "Part ID: $partIdStr"
                partIdTxt!!.visibility=View.VISIBLE
            }else{
                partIdTxt!!.visibility=View.GONE
            }

            if (dieTypeStr.isNotEmpty() && !TextUtils.isEmpty(dieTypeStr) && dieTypeStr != "null") {
                dieTypeTxt!!.text="Die Type: "+ dieTypeStr.uppercase(Locale.getDefault())
                dieTypeTxt!!.visibility=View.VISIBLE
            }else{
                dieTypeTxt!!.visibility=View.GONE
            }

            videoOnlineImageButton!!.setOnClickListener(this)
            flashImgIcon!!.setOnClickListener(this)
            settingsImgIcon!!.setOnClickListener(this)
            videoRecordPlayPause!!.setOnClickListener(this)
        } else {
            showCustomAlert(this@VideoRecordActivity.resources.getString(R.string.battery_alert_title),
                this@VideoRecordActivity.resources.getString(R.string.battery_alert_message),CommonUtils.BATTERY_DIALOG,
                listOf(this@VideoRecordActivity.resources.getString(R.string.alert_exit)))
        }

        //setMicMuted(false)
    }
/*
    private fun setMicMuted(state: Boolean) {
        val myAudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // get the working mode and keep it
        val workingAudioMode = myAudioManager.mode
        myAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        // change mic state only if needed
        if (myAudioManager.isMicrophoneMute != state) {
            myAudioManager.isMicrophoneMute = state
        }

        // set back the original working mode
        myAudioManager.mode = workingAudioMode
       *//* val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)*//*
    }*/
    private fun initProgress(){
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog!!.setMessage(this@VideoRecordActivity.resources.getString(R.string.progress_dialog_message_video_recording))
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(v: View?) {
        if (v == videoOnlineImageButton) {
            if (isRecording) {
                stopRecording()
            } else {
                MediaPrepareTask().execute(null, null, null)
            }
        } else if (v == settingsImgIcon) {
            if (Counter != null) {
                Counter!!.cancel()
                Counter = null
            }
            showDialog()

        } else if (v == flashImgIcon) {
            if (isFlashMode) {
                isFlashMode = false
                flashImgIcon!!.setImageResource(R.drawable.flash_off)
                parameters!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
                mCamera!!.parameters = parameters!!
            } else {
                flashImgIcon!!.setImageResource(R.drawable.flash_on)
                isFlashMode = true
                parameters!!.flashMode = Camera.Parameters.FLASH_MODE_ON
                parameters!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                mCamera!!.parameters = parameters!!
            }
        } else if (v == videoRecordPlayPause) {
            if (isPauseResume) {
                isPauseResume = false
                videoRecordPlayPause!!.setImageResource(R.drawable.video_record_pause)
                mMediaRecorder!!.resume()

                println("resume recordSecondsLeft  $recordSecondsLeft")
                if (mMediaRecorder != null) {
                    mMediaRecorder!!.resume()
                }
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                if (recordMCountDown != null) {
                    recordMCountDown!!.cancel()
                    recordMCountDown = null
                }
                backgroundTimer()
                recordMCountDown = object : CountDownTimer(videoMaxTimeInMillis, 1000) {
                    override fun onFinish() {
                        println("resume onFinish  ")
                        stopRecording()
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        //println("resume onTick millisUntilFinished $millisUntilFinished")
                        //recordSecondsLeft = millisUntilFinished;
                        recordSecondsLeft =
                            millisUntilFinished / 1000
                        timeLeftTxt!!.text = "$recordSecondsLeft/$totalTimer"
                    }
                }.start()
            } else {
                isPauseResume = true
                videoRecordPlayPause!!.setImageResource(R.drawable.video_record_play)
                mMediaRecorder!!.pause()

                println("pause recordSecondsLeft  $recordSecondsLeft")
                videoMaxTimeInMillis = (recordSecondsLeft.toInt() * 1000).toLong()
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
                if (recordMCountDown != null) {
                    recordMCountDown!!.cancel()
                    recordMCountDown = null
                }
            }
        }
    }

    private fun stopRecording() {
        try {
            parameters!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            mCamera!!.parameters = parameters
            if (mMediaRecorder != null) {
                mMediaRecorder!!.stop() // stop the recording
            }
            flashImgIcon!!.setImageResource(R.drawable.flash_off)
            settingsImgIcon!!.visibility = View.GONE
            videoRecordPlayPause!!.visibility = View.GONE
            videoOnlineImageButton!!.setImageResource(R.drawable.video_record_start_new)
            releaseMediaRecorder() // release the MediaRecorder object
            mCamera!!.lock() // take camera access back from MediaRecorder
            isRecording = false
            releaseCamera()
            Log.d(tag, "onVideoSaved ${mOutputFile.toString()}")
            val inputPath = mOutputFile.toString()
            Log.d(tag, "onVideoSaved inputPath $inputPath")

            val i = Intent(this@VideoRecordActivity, VideoPreviewActivity::class.java)
            i.putExtra(CommonUtils.VIDEO_SAVING_FILE_PATH, inputPath)
            startActivity(i)

           /* val inputpath = mOutputFile.toString()
            val outputpath=CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO,this@VideoRecordActivity).toString()

            //val command= "ffmpeg -i $inputpath -c copy -an $outputpath"
            val command= "ffmpeg -i $$inputpath -vcodec copy -an $$outputpath"
            Log.i(
                TAG,
                "command  $command"
            )
            val session = FFmpegKit.execute(command)
            if (ReturnCode.isSuccess(session.returnCode)) {
            // SUCCESS
                Log.i(TAG, "Command execution completed successfully.")
                val i = Intent(this@VideoRecordActivity, VideoPreviewActivity::class.java)
                i.putExtra("videoSavingFilePath", outputpath)
                startActivity(i)
            } else if (ReturnCode.isCancel(session.returnCode)) {
            // CANCEL
                Log.i(TAG, "Command execution cancelled by user.")
            } else {
                // FAILURE
                Log.d(
                    TAG,
                    String.format(
                        "Command failed with state %s and rc %s.%s",
                        session.state,
                        session.returnCode,
                        session.failStackTrace
                    )
                )
            }*/
         /*   val rc = FFmpeg.execute(command)
            Log.i(Config.TAG, "rc $rc")

            if (rc == RETURN_CODE_SUCCESS) {
                Log.i(Config.TAG, "Command execution completed successfully.")
                val i = Intent(this@VideoRecordActivity, VideoPreviewActivity::class.java)
                i.putExtra("videoSavingFilePath", outputpath)
                startActivity(i)
            } else if (rc == RETURN_CODE_CANCEL) {
                Log.i(Config.TAG, "Command execution cancelled by user.")
            } else {
                Log.i(
                    Config.TAG,
                    String.format("Command execution failed with rc=%d and the output below.", rc)
                )
                Config.printLastCommandOutput(Log.INFO)
            }*/
           /* val executionId = FFmpeg.executeAsync(command
            ) { executionId, returnCode ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    Log.i(
                        Config.TAG,
                        "Async command execution completed successfully."
                    )
                    val i = Intent(this@VideoRecordActivity, VideoPreviewActivity::class.java)
                    i.putExtra("videoSavingFilePath", outputpath)
                    startActivity(i)
                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.")
                } else {
                    Log.i(
                        Config.TAG,
                        String.format(
                            "Async command execution failed with returnCode=%d.",
                            returnCode
                        )
                    )
                }
            }*/


        } catch (e: RuntimeException) {
            Log.d(
                tag,
                "RuntimeException: stop() is called immediately after start()"
            )
            mOutputFile!!.delete()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(CAMERA_PERMISSION),
            RC_PERMISSION
        )
    }

    private fun checkPermissions(): Boolean {
        return ((ActivityCompat.checkSelfPermission(
            this,
            CAMERA_PERMISSION
        )) == PackageManager.PERMISSION_GRANTED
                && (ActivityCompat.checkSelfPermission(
            this,
            CAMERA_PERMISSION
        )) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
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
                } else {
                    showCustomAlert(this@VideoRecordActivity.resources.getString(R.string.permissions_alert_title),
                        this@VideoRecordActivity.resources.getString(R.string.permissions_alert_message),CommonUtils.PERMISSIONS_DIALOG,
                        listOf(this@VideoRecordActivity.resources.getString(R.string.permissions_alert_option)))
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        setMicMuted(false)
        releaseMediaRecorder()
        releaseCamera()
    }

    override fun onResume() {
        super.onResume()
        //CommonUtils.freeMemory()
        setMicMuted(true)
    }
    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder!!.reset()
            mMediaRecorder!!.release()
            mMediaRecorder = null
            if (mCamera != null) {
                mCamera!!.lock()
            }
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
        if (recordMCountDown != null) {
            recordMCountDown!!.cancel()
            recordMCountDown = null
        }
    }

    private fun prepareVideoRecorder(): Boolean {
        mMediaRecorder = MediaRecorder()

        // Step 1: Unlock and set camera to MediaRecorder
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.unlock()
            mMediaRecorder!!.setCamera(mCamera)

            // Step 2: Set sources
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            // mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)

            /*  mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
          mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
          mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
          mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
          mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)*/
//
//        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            // mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC);
            // mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            //   mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            // mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264);


            ///Log.e("---1----", width+"------------"+height);
//                    mediaRecorder.setVideoSize(camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height);
            /// mMediaRecorder!!.setVideoFrameRate(30);
            //mMediaRecorder!!.setVideoEncodingBitRate(3*1024*1024);
//                    mediaRecorder.setOrientationHint(90);
            //mMediaRecorder!!.setMaxDuration(60*60*1000);

            // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
            mMediaRecorder!!.setProfile(profile)

            // Step 4: Set output file
            mOutputFile = CameraHelper.getOutputMediaFile(
                CameraHelper.MEDIA_TYPE_VIDEO,
                this@VideoRecordActivity
            )
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
                    tag,
                    "IllegalStateException preparing MediaRecorder: " + e.message
                )
                releaseMediaRecorder()
                return false
            } catch (e: IOException) {
                Log.d(
                    tag,
                    "IOException preparing MediaRecorder: " + e.message
                )
                releaseMediaRecorder()
                return false
            }
            return true
        }else
        {
            return false
        }

    }

    inner class MediaPrepareTask : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()
                progressDialog.show()
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                try {
                    setMicMuted(true)
                    mMediaRecorder!!.start()

                    isRecording = true

                    Handler(Looper.getMainLooper()).postDelayed({
                        // enable stop button
                        runOnUiThread {
                            videoRecordPlayPause!!.setImageResource(R.drawable.video_record_pause)
                            settingsImgIcon!!.visibility = View.GONE
                            videoRecordPlayPause!!.visibility = View.VISIBLE
                            videoOnlineImageButton!!.setImageResource(R.drawable.video_record_stop_new)
                            if(progressDialog.isShowing){
                                progressDialog.dismiss()
                            }
                            recordMCountDown = object : CountDownTimer(videoMaxTimeInMillis, 1000) {
                                override fun onFinish() {
                                    stopRecording()
                                }

                                override fun onTick(millisUntilFinished: Long) {
                                    //println(millisUntilFinished)
                                    recordSecondsLeft = millisUntilFinished / 1000
                                    timeLeftTxt!!.text = "$recordSecondsLeft/$totalTimer"
                                }
                            }.start()
                        }
                    },1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // prepare didn't work, release the camera
                    releaseMediaRecorder()
//                    runOnUiThread(Runnable {
//                        if(progressDialog.isShowing)
//                        progressDialog.dismiss()
//                    })
                    return false
                }

            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder()
                return false
            }
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if(progressDialog.isShowing)
                progressDialog.dismiss()
        }

    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
        Log.d("pri","")
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
            Log.e(tag, "onSurfaceTextureAvailable surface_view!!.width::: " + surface_view!!.width)
            Log.e(
                tag,
                "onSurfaceTextureAvailable surface_view!!.height::: " + surface_view!!.height
            )

            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
            profile!!.videoFrameWidth = optimalSize!!.width
            profile!!.videoFrameHeight = optimalSize.height

            parameters!!.setPreviewSize(profile!!.videoFrameWidth, profile!!.videoFrameHeight)

            val focusModes: List<String> = parameters!!.supportedFocusModes
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters!!.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters!!.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            }

            mCamera!!.parameters = parameters

            mCamera!!.setPreviewTexture(p0)
            surface_view!!.alpha = 1.0f
            mCamera!!.startPreview()

            val optimalSize1: Camera.Size? = mCamera!!.parameters.previewSize

            Log.d(
                tag,
                "onSurfaceTextureAvailable optimalSize1!!.width ${optimalSize1!!.width}"
            )
            Log.d(
                tag,
                "onSurfaceTextureAvailable optimalSize1!!.height ${optimalSize1.height}"
            )

            getCameraCharacteristics()
        } catch (e: IOException) {
            Log.e(
                tag,
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
        Counter = object : CountDownTimer(idleTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished1: Long) {
                val secs = (millisUntilFinished1 / 1000).toInt() % 60
                val minutes = (millisUntilFinished1 / (1000 * 60) % 60).toInt()
               // Log.e("secs", secs.toString() + "")
            }

            override fun onFinish() {
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                showCustomAlert(this@VideoRecordActivity.resources.getString(R.string.video_recording_timer_alert_title),
                    this@VideoRecordActivity.resources.getString(R.string.video_recording_timer_alert_message),CommonUtils.TIMER_DIALOG,
                    listOf(this@VideoRecordActivity.resources.getString(R.string.continue_str),
                        this@VideoRecordActivity.resources.getString(R.string.alert_exit)))
            }
        }
        Counter!!.start()
    }

    private fun getCameraCharacteristics() {
        Log.d(tag, "getCameraCharacteristics")
        try {

            // val sizes: List<Camera.Size> = parameters!!.getSupportedPreviewSizes()
            val mSupportedSizes = parameters!!.supportedPreviewSizes
            //  val mSupportedSizes = parameters!!.supportedVideoSizes
            cameraSizesArray = arrayOfNulls<String?>(mSupportedSizes.size)
            for (i in mSupportedSizes.indices) {
                val str = mSupportedSizes[i].width.toString() + "x" + mSupportedSizes[i].height
                Log.i(tag, "imageDimension $str")
                cameraSizesArray[i] = str
            }

            val sb1 = StringBuffer()
            val supportedPreviewFps: List<IntArray> = parameters!!.supportedPreviewFpsRange
            val supportedPreviewFpsIterator = supportedPreviewFps.iterator()
            while (supportedPreviewFpsIterator.hasNext()) {
                val tmpRate = supportedPreviewFpsIterator.next()
                val sb = StringBuffer()
                var b = true
                val i = tmpRate.size
                var j = 0
                while (j < i) {
                    if (b) {
                        sb.append(tmpRate[j].toString() + " - ")
                        b = false
                    } else {
                        sb.append(tmpRate[j])
                    }
                    j++
                }
                sb1.append(sb.toString() + "n")
            }
            Log.v("CameraTest111", sb1.toString())
            var str = sb1.toString()
            str = str.replace("000".toRegex(), "")
            cameraFPSArray = str.split("n").toTypedArray()
            Log.e(
                tag,
                "printSupportFormats: cameraFPSArray " + Arrays.toString(cameraFPSArray)
            )
        } catch (e: java.lang.Exception) {
            Log.d(tag, "CameraAccessException: " + e.message)
        }
    }

    private fun showDialog() {
        val inflater = layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.camera_settings_dialog, null)
        val spResolution1 = alertLayout.findViewById<Spinner>(R.id.spResolution)
        val langAdapter1 = ArrayAdapter<CharSequence>(
            this@VideoRecordActivity,
            R.layout.spinner_text,
            cameraSizesArray
        )
        langAdapter1.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        spResolution1.adapter = langAdapter1
        val spResolution2 = alertLayout.findViewById<Spinner>(R.id.spFPS)
        val langAdapter2 = ArrayAdapter<CharSequence>(
            this@VideoRecordActivity,
            R.layout.spinner_text,
            cameraFPSArray!!
        )
        langAdapter2.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        spResolution2.adapter = langAdapter2
        val alert = AlertDialog.Builder(this)
        alert.setTitle(this@VideoRecordActivity.resources.getString(R.string.settings_str))
        alert.setView(alertLayout)
        alert.setCancelable(false)
        alert.setNegativeButton(
            this@VideoRecordActivity.resources.getString(R.string.alert_cancel)
        ) { dialog, which ->
            //Toast.makeText(getBaseContext(), "Cancel Clicked", Toast.LENGTH_SHORT).show();
        }
        alert.setPositiveButton(
            this@VideoRecordActivity.resources.getString(R.string.alert_ok)
        ) { dialog, which ->
            val size = spResolution1.selectedItem.toString()
            Log.d(tag, "spinner ok click size $size")
            val separated = size.split("x").toTypedArray()
            Log.d(
                tag,
                "spinner ok click separated.length " + separated.size
            )
            Log.d(
                tag,
                "spinner ok click Integer.parseInt(separated[0]) " + separated[0].toInt()
            )
            Log.d(
                tag,
                "spinner ok click Integer.parseInt(separated[1]) " + separated[1].toInt()
            )
            // Camera camera = Camera.open();
            //int cameraSizeResolution =camera.new Size(Integer.parseInt(separated[0]),Integer.parseInt(separated[1]));
            val selectedWidth = separated[0].toInt()
            val selectedHeight = separated[1].toInt()
            Log.d(
                tag,
                "spinner ok click selectedWidth $selectedWidth"
            )
            Log.d(
                tag,
                "spinner ok click selectedHeight $selectedHeight"
            )

            //Camera.Size previewSize = getOptimalPreviewSize(params.getSupportedPreviewSizes(),selectedWidth,selectedHeight);
            //  Log.d(TAG, "spinner ok click previewSize.width " + previewSize.width);
            //Log.d(TAG, "spinner ok click previewSize.height " + previewSize.height);

            if (mCamera != null) {
                if (parameters != null) {
                   // mCamera!!.unlock()
                    // parameters!!.setPreviewSize(selectedWidth,selectedHeight);
                    //params.setPictureSize(previewSize.width, previewSize.height);
                    //   params.setPreviewSize(selectedWidth,selectedHeight);
                    //  params.setPictureSize(selectedWidth,selectedHeight);

                    val mSupportedPreviewSizes = parameters!!.supportedPreviewSizes
                    val mSupportedVideoSizes = parameters!!.supportedVideoSizes
                    /* val optimalSize: Camera.Size? = CameraHelper.getOptimalVideoSize(
                         mSupportedVideoSizes,
                         mSupportedPreviewSizes, selectedWidth,selectedHeight
                     )*/
                    // val optimalSize: Camera.Size? =
                    //   getBestSize(selectedWidth, selectedHeight,  parameters!!.getSupportedPreviewSizes())
                    // val optimalSize : Camera.Size? = getBestSize(parameters!!.getSupportedPreviewSizes())

                    val optimalSize: Camera.Size? =
                        choosePreviewSize(parameters!!, selectedWidth, selectedHeight)
                    Log.d(
                        tag,
                        "spinner ok click optimalSize!!.width ${optimalSize!!.width}"
                    )
                    Log.d(
                        tag,
                        "spinner ok click optimalSize!!.height ${optimalSize.height}"
                    )

                  //  profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
                    //profile!!.videoFrameWidth = optimalSize.width
                    //profile!!.videoFrameHeight = optimalSize.height

                    // parameters!!.setPreviewSize(profile!!.videoFrameWidth, profile!!.videoFrameHeight)
                    //parameters!!.setPreviewSize( optimalSize.width,optimalSize.height)
                    // parameters!!.setPreviewSize( selectedWidth,selectedHeight)
                    //  mCamera!!.parameters = parameters
                }
            }
        }
        val dialog = alert.create()
        dialog.show()
    }



    private fun choosePreviewSize(params: Camera.Parameters, width: Int, height: Int): Camera.Size? {
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        val ppsfv = params.preferredPreviewSizeForVideo
        if (ppsfv != null) {
            Log.d(
                "",
                "Camera preferred preview size for video is " +
                        ppsfv.width + "x" + ppsfv.height
            )
        }
        //for (Camera.Size size : parms.getSupportedPreviewSizes()) {
        //    Log.d(TAG, "supported: " + size.width + "x" + size.height);
        //}
        for (size: Camera.Size in params.supportedPreviewSizes) {
            if (size.width == width && size.height == height) {
                // parms.setPreviewSize(width, height)
                return size
            }
        }
        Log.d("", "Unable to set preview size to " + width + "x" + height)
        //if (ppsfv != null) {
        //parms.setPreviewSize(ppsfv.width, ppsfv.height)
        return ppsfv
        //}
        // else use whatever the default size is
    }
    override fun onBackPressed() {
        showCustomAlert(this@VideoRecordActivity.resources.getString(R.string.app_name),
            this@VideoRecordActivity.resources.getString(R.string.dashboard_navigation_alert_message),
            CommonUtils.BACK_PRESSED_DIALOG,
            listOf(this@VideoRecordActivity.resources.getString(R.string.alert_ok),
                this@VideoRecordActivity.resources.getString(R.string.alert_cancel)))

    }


    private fun showCustomAlert(alertTitle: String,alertMessage: String, functionality: String,buttonList:List<String>){
        val customDialogModel= CustomDialogModel(alertTitle,alertMessage,null,
            buttonList
        )
        DialogUtils.showCustomAlert(this,customDialogModel,this,functionality)
    }

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        Log.d("",
            "onCustomDialogButtonClicked buttonName::: $buttonName:::: functionality:::: $functionality"
        )
        if(buttonName.equals(this@VideoRecordActivity.resources.getString(R.string.alert_exit),true)) {
            if (functionality.equals(CommonUtils.BATTERY_DIALOG, true)) {
                try {
//                    val previewIntent = Intent()
//                    setResult(RESULT_CANCELED, previewIntent)
//                    finishAffinity()
                    CommonUtils.appExit(this@VideoRecordActivity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }else if(functionality.equals(CommonUtils.TIMER_DIALOG, true)){
                try {
                   // val intent = Intent(this@VideoRecordActivityNew, VideoRecordActivityNew::class.java)
                    //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    //intent.putExtra("finish", true)
                  //  finish()
                   /* val previewIntent = Intent()
                    setResult(RESULT_CANCELED, previewIntent)
                    finishAffinity()*/
//                    finishAffinity();
//                    exitProcess(0);

                    CommonUtils.appExit(this@VideoRecordActivity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }else if(buttonName.equals(this@VideoRecordActivity.resources.getString(R.string.permissions_alert_option),true)) {
            if (functionality.equals(CommonUtils.PERMISSIONS_DIALOG, true)) {
                requestPermissions()
            }
        }else if(buttonName.equals(this@VideoRecordActivity.resources.getString(R.string.continue_str),true)) {
            if (functionality.equals(CommonUtils.TIMER_DIALOG, true)) {
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                backgroundTimer()
            }
        }else if(buttonName.equals(this@VideoRecordActivity.resources.getString(R.string.alert_ok),true)) {
            if (functionality.equals(CommonUtils.BACK_PRESSED_DIALOG, true)) {
                if (Counter != null) {
                    Counter!!.cancel()
                    Counter = null
                }
                val intent = Intent(this, DashBoardActivity::class.java)
                intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }else if(buttonName.equals(this@VideoRecordActivity.resources.getString(R.string.alert_cancel),true)) {
                //No action required. Just exit dialog.
        }
    }
    private fun setMicMuted(state: Boolean) {
//        AudioManager myAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        val myAudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // get the working mode and keep it
        val workingAudioMode = myAudioManager.mode
        myAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        // change mic state only if needed
        if (myAudioManager.isMicrophoneMute != state) {
            myAudioManager.isMicrophoneMute = state
        }

        // set back the original working mode
        myAudioManager.mode = workingAudioMode
    }
    override fun onDestroy() {
        super.onDestroy()
        //CommonUtils.freeMemory()
        setMicMuted(false)
        releaseMediaRecorder()
        releaseCamera()
    }
}
