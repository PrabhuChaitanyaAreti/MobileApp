package com.vsoft.goodmankotlin

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
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
import java.util.concurrent.TimeUnit


class VideoRecordActivity : AppCompatActivity(), TextureView.SurfaceTextureListener,
    View.OnClickListener, CustomDialogCallback {
    private var mCamera: Camera? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mOutputFile: File? = null
    private var isRecording = false
    private var isPauseResume = false

    private var parameters: Camera.Parameters? = null;
    private var profile: CamcorderProfile? = null

    private val TAG = VideoRecordActivity::class.java.simpleName

    private var CAMERA_PERMISSION = Manifest.permission.CAMERA
    private var RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

    private var RC_PERMISSION = 101
    private lateinit var progressDialog: ProgressDialog
    /**
     * Background and Countdown timer variables
     */
    private var Counter: CountDownTimer? = null
    private val minutesToGo: Long = 1
    private val initialMillisToGo = minutesToGo * 1000 * 60

    private var recordDynamicTimer: Long = 20000
    private var recordmCountDown: CountDownTimer? = null
    private var recordSecondsLeft: Long = 0

    private var isFlashMode = false

    private var cameraSizesArray = emptyArray<String?>()
    private var cameraFPSArray: Array<String>? = null

    private var surface_view: TextureView? = null
    private var settingsImgIcon: ImageView? = null
    private var videoOnlineImageButton: ImageButton? = null
    private var videoRecordPlayPause: ImageButton? = null
    private var flashImgIcon: ImageView? = null
    private var timeleftTxt: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_record)

     /*   val finish= intent.getBooleanExtra("finish", false)
        if(finish) {
            finish();
            return;
        }*/

        surface_view = findViewById(R.id.surface_view)
        settingsImgIcon = findViewById(R.id.settingsImgIcon)
        videoOnlineImageButton = findViewById(R.id.videoOnlineImageButton)
        videoRecordPlayPause = findViewById(R.id.videoRecordPlayPause)
        flashImgIcon = findViewById(R.id.flashImgIcon)
        timeleftTxt = findViewById(R.id.timeleftTxt)

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

            videoOnlineImageButton!!.setOnClickListener(this)
            flashImgIcon!!.setOnClickListener(this)
            settingsImgIcon!!.setOnClickListener(this)
            videoRecordPlayPause!!.setOnClickListener(this)
        } else {
            showCustomAlert(this@VideoRecordActivity.resources.getString(R.string.battery_alert_title),
                this@VideoRecordActivity.resources.getString(R.string.battery_alert_message),CommonUtils.BATTERY_DIALOG,
                listOf(this@VideoRecordActivity.resources.getString(R.string.alert_exit)))
        }
    }
    private fun initProgress(){
        progressDialog = ProgressDialog(this)
        progressDialog!!.setCancelable(false)
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
                parameters!!.setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                mCamera!!.setParameters(parameters!!)
            } else {
                flashImgIcon!!.setImageResource(R.drawable.flash_on)
                isFlashMode = true
                parameters!!.setFlashMode(Camera.Parameters.FLASH_MODE_ON)
                parameters!!.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                mCamera!!.setParameters(parameters!!)
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
                        recordSecondsLeft = millisUntilFinished;
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
                videoRecordPlayPause!!.setImageResource(R.drawable.video_record_play)
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
            flashImgIcon!!.setImageResource(R.drawable.flash_off)
            settingsImgIcon!!.visibility = View.GONE
            videoRecordPlayPause!!.visibility = View.GONE
            videoOnlineImageButton!!.setImageResource(R.drawable.video_record_start_new)
            releaseMediaRecorder() // release the MediaRecorder object
            mCamera!!.lock() // take camera access back from MediaRecorder
            isRecording = false
            releaseCamera()
            Log.d(TAG, "onVideoSaved ${mOutputFile.toString()}")
            val i = Intent(this@VideoRecordActivity, VideoPreviewActivity::class.java)
            i.putExtra("videoSavingFilePath", mOutputFile.toString())
            startActivity(i)
        } catch (e: RuntimeException) {
            Log.d(
                TAG,
                "RuntimeException: stop() is called immediately after start()"
            )
            mOutputFile!!.delete()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(CAMERA_PERMISSION, RECORD_AUDIO_PERMISSION),
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
        override fun onPreExecute() {
            super.onPreExecute()
                progressDialog.show()
        }
        override fun doInBackground(vararg params: Void?): Boolean? {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                try {
                    mMediaRecorder!!.start()

                    isRecording = true

                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        // enable stop button
                        runOnUiThread {
                            if(progressDialog.isShowing){
                                progressDialog.dismiss()
                            }
                            videoRecordPlayPause!!.setImageResource(R.drawable.video_record_pause)
                            settingsImgIcon!!.visibility = View.GONE
                            videoRecordPlayPause!!.visibility = View.VISIBLE
                            videoOnlineImageButton!!.setImageResource(R.drawable.video_record_stop_new)
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
                    }, 1000)
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
            Log.e(TAG, "onSurfaceTextureAvailable surface_view!!.width::: " + surface_view!!.width)
            Log.e(
                TAG,
                "onSurfaceTextureAvailable surface_view!!.height::: " + surface_view!!.height
            )

            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
            profile!!.videoFrameWidth = optimalSize!!.width
            profile!!.videoFrameHeight = optimalSize.height

            parameters!!.setPreviewSize(profile!!.videoFrameWidth, profile!!.videoFrameHeight)

            val focusModes: List<String> = parameters!!.getSupportedFocusModes()
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters!!.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters!!.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO)
            }

            mCamera!!.parameters = parameters

            mCamera!!.setPreviewTexture(p0)
            surface_view!!.alpha = 1.0f
            mCamera!!.startPreview()

            val optimalSize1: Camera.Size? = mCamera!!.parameters.previewSize

            Log.d(
                TAG,
                "onSurfaceTextureAvailable optimalSize1!!.width ${optimalSize1!!.width}"
            )
            Log.d(
                TAG,
                "onSurfaceTextureAvailable optimalSize1!!.height ${optimalSize1.height}"
            )

            getCameraCharacteristics()
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
                showCustomAlert(this@VideoRecordActivity.resources.getString(R.string.video_recording_timer_alert_title),
                    this@VideoRecordActivity.resources.getString(R.string.video_recording_timer_alert_message),CommonUtils.TIMER_DIALOG,
                    listOf(this@VideoRecordActivity.resources.getString(R.string.continue_str),
                        this@VideoRecordActivity.resources.getString(R.string.alert_exit)))
            }
        }
        Counter!!.start()
    }

    private fun getCameraCharacteristics() {
        Log.d(TAG, "getCameraCharacteristics")
        try {

            // val sizes: List<Camera.Size> = parameters!!.getSupportedPreviewSizes()
            val mSupportedSizes = parameters!!.supportedPreviewSizes
            //  val mSupportedSizes = parameters!!.supportedVideoSizes
            cameraSizesArray = arrayOfNulls<String?>(mSupportedSizes.size)
            for (i in mSupportedSizes.indices) {
                val str = mSupportedSizes[i].width.toString() + "x" + mSupportedSizes[i].height
                Log.i(TAG, "imageDimension $str")
                cameraSizesArray[i] = str
            }

            val sb1 = StringBuffer()
            val supportedPreviewFps: List<IntArray> = parameters!!.getSupportedPreviewFpsRange()
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
                TAG,
                "printSupportFormats: cameraFPSArray " + Arrays.toString(cameraFPSArray)
            )
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "CameraAccessException: " + e.message)
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
            Log.d(TAG, "spinner ok click size $size")
            val separated = size.split("x").toTypedArray()
            Log.d(
                TAG,
                "spinner ok click separated.length " + separated.size
            )
            Log.d(
                TAG,
                "spinner ok click Integer.parseInt(separated[0]) " + separated[0].toInt()
            )
            Log.d(
                TAG,
                "spinner ok click Integer.parseInt(separated[1]) " + separated[1].toInt()
            )
            // Camera camera = Camera.open();
            //int cameraSizeResolution =camera.new Size(Integer.parseInt(separated[0]),Integer.parseInt(separated[1]));
            val selectedWidth = separated[0].toInt()
            val selectedHeight = separated[1].toInt()
            Log.d(
                TAG,
                "spinner ok click selectedWidth $selectedWidth"
            )
            Log.d(
                TAG,
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
                        TAG,
                        "spinner ok click optimalSize!!.width ${optimalSize!!.width}"
                    )
                    Log.d(
                        TAG,
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



    fun choosePreviewSize(parms: Camera.Parameters, width: Int, height: Int): Camera.Size? {
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        val ppsfv = parms.preferredPreviewSizeForVideo
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
        for (size: Camera.Size in parms.supportedPreviewSizes) {
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
                    val previewIntent = Intent()
                    setResult(RESULT_CANCELED, previewIntent)
                    finishAffinity()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }else if(functionality.equals(CommonUtils.TIMER_DIALOG, true)){
                try {
                   // val intent = Intent(this@VideoRecordActivityNew, VideoRecordActivityNew::class.java)
                    //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    //intent.putExtra("finish", true)
                  //  finish()
                    val previewIntent = Intent()
                    setResult(RESULT_CANCELED, previewIntent)
                    finishAffinity()
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
                val intent = Intent(this, DashBoardActivity::class.java)
                intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }else if(buttonName.equals(this@VideoRecordActivity.resources.getString(R.string.alert_cancel),true)) {
                //No action required. Just exit dialog.
        }
    }

}
