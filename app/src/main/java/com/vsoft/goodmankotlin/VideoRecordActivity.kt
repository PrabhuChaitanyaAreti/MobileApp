package com.vsoft.goodmankotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.AudioManager
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.vsoft.goodmankotlin.database.VideoViewModel
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.touchimage.TouchImageView
import com.vsoft.goodmankotlin.utils.BatteryUtil
import com.vsoft.goodmankotlin.utils.CameraHelper
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils
import java.io.File
import java.io.IOException
import java.util.*

class VideoRecordActivity : AppCompatActivity(), TextureView.SurfaceTextureListener,
    View.OnClickListener, CustomDialogCallback {
    private var mCamera: Camera? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mOutputFile: File? = null
    private var isRecording = false
    private var isPauseResume = false

    private var parameters: Camera.Parameters? = null
    private var profile: CamcorderProfile? = null

    private val CAMERAPERMISSIONOPTION = Manifest.permission.CAMERA

    private val CAMERAPERMISSIONCODE = 101
    private lateinit var progressDialog: ProgressDialog

    /**
     * Background and Countdown timer variables
     */
    private var appExitCounter: CountDownTimer? = null
    private val idleTime: Long = 5
    private val idleTimeInMillis = idleTime * 1000 * 60

    private var videoMaxTimeInMillis: Long = 2 * 60 * 1000
    private var totalTimer: Long = videoMaxTimeInMillis / 1000
    private var recordMCountDown: CountDownTimer? = null
    private var recordSecondsLeft: Long = 0

    private var isFlashMode = false

    private var cameraSizesArray = emptyArray<String?>()
    private var cameraFPSArray: Array<String>? = null

    private var infoIconImgRL:RelativeLayout?=null
    private var infoIconImg: ImageView? = null
    private var surfaceView: TextureView? = null
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
    private var isVideoRecordScreen = false
    private var isNewDie = false
    private var isTopDie = false

    private lateinit var vm: VideoViewModel

    private var dieTopBottomDetailsCount = 0
    private var typeStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_record)

        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )
        isNewDie = sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_NEW_DIE, false)
        dieIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_ID, "").toString()
        partIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_PART_ID, "").toString()
        dieTypeStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_TYPE, "").toString()
        isVideoRecordScreen = sharedPreferences!!.getBoolean(CommonUtils.IS_VIDEO_RECORD_SCREEN, false)

        initCameraView1()

        if (isNewDie) {
            if (isVideoRecordScreen) {
                videoOnlineImageButton!!.visibility = View.VISIBLE
            } else {
                videoOnlineImageButton!!.visibility = View.GONE

                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putBoolean(CommonUtils.IS_VIDEO_RECORD_SCREEN, true)
                editor.apply()

                vm = ViewModelProviders.of(this)[VideoViewModel::class.java]

                if (dieTypeStr.isNotEmpty() && !TextUtils.isEmpty(dieTypeStr) && dieTypeStr != "null") {

                    typeStr = if (dieTypeStr.contains("_")) {
                        val splitArray: List<String> = dieTypeStr.split("_")
                        splitArray[0]
                    } else {
                        dieTypeStr
                    }
                    val isDieType = vm.isDieTypeExist(typeStr)

                    if (isDieType) {
                        isTopDie = typeStr == CommonUtils.ADD_DIE_TOP
                        dieTopBottomDetailsCount = if (isTopDie) {
                            vm.getDieCount(dieIdStr, partIdStr, "top_details")
                        } else {
                            vm.getDieCount(dieIdStr, partIdStr, "bottom_details")
                        }

                    } else {
                        videoOnlineImageButton!!.visibility = View.VISIBLE
                    }
                } else {
                    videoOnlineImageButton!!.visibility = View.VISIBLE
                }
                if (dieTopBottomDetailsCount > 0) {
                    var message = ""
                    var option1 = ""
                    var option2 = ""

                    if (isTopDie) {
                        message =
                            this@VideoRecordActivity.resources.getString(R.string.video_record_message_1)
                        option1 =
                            this@VideoRecordActivity.resources.getString(R.string.video_record_option_1)
                        option2 =
                            this@VideoRecordActivity.resources.getString(R.string.video_record_option_2)
                    } else {
                        message =
                            this@VideoRecordActivity.resources.getString(R.string.video_record_message_2)
                        option1 =
                            this@VideoRecordActivity.resources.getString(R.string.video_record_option_3)
                        option2 =
                            this@VideoRecordActivity.resources.getString(R.string.video_record_option_4)
                    }

                    dieTopBottomDetailsCount++

                    showCustomAlert(
                        this@VideoRecordActivity.resources.getString(R.string.app_name),
                        message,
                        CommonUtils.DIE_RECORD_OPTIONS_DIALOG,
                        listOf(
                            option1,
                            option2
                        )
                    )

                } else {
                    val dieTopBottomCount = vm.getDieCount(dieIdStr, partIdStr, typeStr)
                    if (dieTopBottomCount > 0) {
                        dieTopBottomDetailsCount++

                        var message = ""
                        var option1 = ""
                        var option2 = ""

                        if (isTopDie) {
                            message =
                                this@VideoRecordActivity.resources.getString(R.string.video_record_message_1)
                            option1 =
                                this@VideoRecordActivity.resources.getString(R.string.video_record_option_1)
                            option2 =
                                this@VideoRecordActivity.resources.getString(R.string.video_record_option_2)
                        } else {
                            message =
                                this@VideoRecordActivity.resources.getString(R.string.video_record_message_2)
                            option1 =
                                this@VideoRecordActivity.resources.getString(R.string.video_record_option_3)
                            option2 =
                                this@VideoRecordActivity.resources.getString(R.string.video_record_option_4)
                        }

                        showCustomAlert(
                            this@VideoRecordActivity.resources.getString(R.string.app_name),
                            message,
                            CommonUtils.DIE_RECORD_OPTIONS_DIALOG,
                            listOf(
                                option1,
                                option2
                            )
                        )
                    } else {
                        videoOnlineImageButton!!.visibility = View.VISIBLE
                    }

                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initCameraView1() {
        surfaceView = findViewById(R.id.surface_view)
        settingsImgIcon = findViewById(R.id.settingsImgIcon)
        videoOnlineImageButton = findViewById(R.id.videoOnlineImageButton)
        videoRecordPlayPause = findViewById(R.id.videoRecordPlayPause)
        flashImgIcon = findViewById(R.id.flashImgIcon)
        timeLeftTxt = findViewById(R.id.timeleftTxt)
        partIdTxt = findViewById(R.id.partIdTxt)
        dieIdTxt = findViewById(R.id.dieIdTxt)
        dieTypeTxt = findViewById(R.id.dieTypeTxt)
        infoIconImg = findViewById(R.id.infoIconImg)
        infoIconImgRL=findViewById(R.id.infoIconImgRL)


        if (isNewDie) {
            infoIconImg!!.visibility = View.GONE
            infoIconImgRL!!.visibility = View.GONE
        } else {
            infoIconImg!!.visibility = View.VISIBLE
            infoIconImgRL!!.visibility = View.VISIBLE
        }

        initProgress()

        val batterLevel: Int = BatteryUtil.getBatteryPercentage(this@VideoRecordActivity)

        if (batterLevel >= CommonUtils.BATTERY_LEVEL_PERCENTAGE) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (checkPermissions()) {
                surfaceView!!.surfaceTextureListener = this
            } else {
                requestPermissions()
            }
            videoRecordPlayPause!!.visibility = View.GONE
            settingsImgIcon!!.visibility = View.GONE

            if (dieIdStr.isNotEmpty() && !TextUtils.isEmpty(dieIdStr) && dieIdStr != "null") {
                dieIdTxt!!.text = this@VideoRecordActivity.resources.getString(R.string.die_id)+ dieIdStr
                dieIdTxt!!.visibility = View.VISIBLE
            } else {
                dieIdTxt!!.visibility = View.GONE
            }
            if (partIdStr.isNotEmpty() && !TextUtils.isEmpty(partIdStr) && partIdStr != "null") {
                partIdTxt!!.text = this@VideoRecordActivity.resources.getString(R.string.part_id)+partIdStr
                partIdTxt!!.visibility = View.VISIBLE
            } else {
                partIdTxt!!.visibility = View.GONE
            }

            if (dieTypeStr.isNotEmpty() && !TextUtils.isEmpty(dieTypeStr) && dieTypeStr != "null") {
                dieTypeTxt!!.text = this@VideoRecordActivity.resources.getString(R.string.die_type)+ dieTypeStr.uppercase(Locale.getDefault())
                dieTypeTxt!!.visibility = View.VISIBLE
            } else {
                dieTypeTxt!!.visibility = View.GONE
            }

            videoOnlineImageButton!!.setOnClickListener(this)
            flashImgIcon!!.setOnClickListener(this)
            settingsImgIcon!!.setOnClickListener(this)
            videoRecordPlayPause!!.setOnClickListener(this)
            infoIconImgRL!!.setOnClickListener(this)
        } else {
            showCustomAlert(
                this@VideoRecordActivity.resources.getString(R.string.battery_alert_title),
                this@VideoRecordActivity.resources.getString(R.string.battery_alert_message),
                CommonUtils.BATTERY_DIALOG,
                listOf(this@VideoRecordActivity.resources.getString(R.string.alert_exit))
            )
        }
    }

    private fun initProgress() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(this@VideoRecordActivity.resources.getString(R.string.progress_dialog_message_video_recording))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(v: View?) {
        if (v == videoOnlineImageButton) {
            if (appExitCounter != null) {
                appExitCounter!!.cancel()
                appExitCounter = null
            }
            if (isRecording) {
                stopRecording()
            } else {
                MediaPrepareTask().execute(null, null, null)
            }
        } else if (v == settingsImgIcon) {
            if (appExitCounter != null) {
                appExitCounter!!.cancel()
                appExitCounter = null

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

                if (mMediaRecorder != null) {
                    mMediaRecorder!!.resume()
                }
                if (appExitCounter != null) {
                    appExitCounter!!.cancel()
                    appExitCounter = null

                }
                if (recordMCountDown != null) {
                    recordMCountDown!!.cancel()
                    recordMCountDown = null
                }

                recordMCountDown = object : CountDownTimer(videoMaxTimeInMillis, 1000) {
                    override fun onFinish() {
                        stopRecording()
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onTick(millisUntilFinished: Long) {

                        recordSecondsLeft =
                            millisUntilFinished / 1000
                        timeLeftTxt!!.text = "$recordSecondsLeft/$totalTimer"
                    }
                }.start()
            } else {
                isPauseResume = true
                videoRecordPlayPause!!.setImageResource(R.drawable.video_record_play)
                videoMaxTimeInMillis = (recordSecondsLeft.toInt() * 1000).toLong()
                isPauseResume = true
                if (mMediaRecorder != null) {
                    if (isRecording) {
                        mMediaRecorder!!.pause()
                    }
                }
                if (appExitCounter != null) {
                    appExitCounter!!.cancel()
                    appExitCounter = null
                }
                if (recordMCountDown != null) {
                    recordMCountDown!!.cancel()
                    recordMCountDown = null
                }
            }
        } else if (v == infoIconImgRL) {
            if (isRecording) {
                videoRecordPlayPause!!.setImageResource(R.drawable.video_record_play)
                videoMaxTimeInMillis = (recordSecondsLeft.toInt() * 1000).toLong()
                isPauseResume = true
                if (mMediaRecorder != null) {
                    if (isRecording) {
                        mMediaRecorder!!.pause()
                    }
                }
                if (appExitCounter != null) {
                    appExitCounter!!.cancel()
                    appExitCounter = null
                }
                if (recordMCountDown != null) {
                    recordMCountDown!!.cancel()
                    recordMCountDown = null
                }
            }
            infoDialog()
        }
    }

    private fun infoDialog() {
        val factory = LayoutInflater.from(this@VideoRecordActivity)
        val customDialogView: View = factory.inflate(R.layout.custom_dialog_info, null)
        val customDialog = android.app.AlertDialog.Builder(this@VideoRecordActivity).create()
        val closeImg = customDialogView.findViewById<ImageView>(R.id.closeImg)
        val imageSingle = customDialogView.findViewById<TouchImageView>(R.id.imageSingle)
        customDialog.setCancelable(false)

        closeImg.setOnClickListener {
            if (isRecording) {
                if (isPauseResume) {
                    isPauseResume = false
                    videoRecordPlayPause!!.setImageResource(R.drawable.video_record_pause)

                    if (mMediaRecorder != null) {
                        mMediaRecorder!!.resume()
                    }
                    if (appExitCounter != null) {
                        appExitCounter!!.cancel()
                        appExitCounter = null

                    }
                    if (recordMCountDown != null) {
                        recordMCountDown!!.cancel()
                        recordMCountDown = null
                    }

                    recordMCountDown = object : CountDownTimer(videoMaxTimeInMillis, 1000) {
                        override fun onFinish() {
                            stopRecording()
                        }

                        override fun onTick(millisUntilFinished: Long) {

                            recordSecondsLeft =
                                millisUntilFinished / 1000
                            timeLeftTxt!!.text = "$recordSecondsLeft/$totalTimer"
                        }
                    }.start()
                }

            }
            customDialog.dismiss()
        }
        imageSingle!!.setImageResource(R.drawable.dieimage1)

       /* Glide.with(this@VideoRecordActivity)
            .load("https://sample-videos.com/img/Sample-jpg-image-50kb.jpg")
            .into(object : CustomTarget<Drawable?>() {
                @SuppressLint("SetTextI18n")
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    imageSingle!!.setImageDrawable(resource)
                }

                override fun onLoadCleared(@Nullable placeholder: Drawable?) = Unit

            })*/


        customDialog.setView(customDialogView)
        customDialog.show()
    }

    private fun stopRecording() {
        try {
            isRecording = false
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

            releaseCamera()
            val inputPath = mOutputFile.toString()

            val i = Intent(this@VideoRecordActivity, VideoPreviewActivity::class.java)
            i.putExtra(CommonUtils.VIDEO_SAVING_FILE_PATH, inputPath)
            startActivity(i)
            finish()

        } catch (e: RuntimeException) {
            releaseMediaRecorder() // release the MediaRecorder object
            mCamera!!.lock() // take camera access back from MediaRecorder

            releaseCamera()
            val inputPath = mOutputFile.toString()

            val i = Intent(this@VideoRecordActivity, VideoPreviewActivity::class.java)
            i.putExtra(CommonUtils.VIDEO_SAVING_FILE_PATH, inputPath)
            startActivity(i)
            finish()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(CAMERAPERMISSIONOPTION),
            CAMERAPERMISSIONCODE
        )
    }

    private fun checkPermissions(): Boolean {
        return ((ActivityCompat.checkSelfPermission(
            this,
            CAMERAPERMISSIONOPTION
        )) == PackageManager.PERMISSION_GRANTED
                && (ActivityCompat.checkSelfPermission(
            this,
            CAMERAPERMISSIONOPTION
        )) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERAPERMISSIONCODE -> {
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
                    surfaceView!!.surfaceTextureListener = this
                } else {
                    showCustomAlert(
                        this@VideoRecordActivity.resources.getString(R.string.permissions_alert_title),
                        this@VideoRecordActivity.resources.getString(R.string.permissions_alert_message),
                        CommonUtils.PERMISSIONS_DIALOG,
                        listOf(this@VideoRecordActivity.resources.getString(R.string.permissions_alert_option))
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        setMicMuted(true)
    }

    override fun onPause() {
        super.onPause()
        setMicMuted(true)

    }

    override fun onResume() {
        super.onResume()

        setMicMuted(true)
    }

    private fun releaseMediaRecorder() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder!!.reset()
                mMediaRecorder!!.release()
                mMediaRecorder = null
                if (mCamera != null) {
                    mCamera!!.lock()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera!!.release()
                mCamera = null
            }
            if (appExitCounter != null) {
                appExitCounter!!.cancel()
                appExitCounter = null
            }
            if (recordMCountDown != null) {
                recordMCountDown!!.cancel()
                recordMCountDown = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun prepareVideoRecorder(): Boolean {
        try {
            mMediaRecorder = MediaRecorder()

            // Step 1: Unlock and set camera to MediaRecorder
            if (mCamera != null) {
                mCamera!!.stopPreview()
                mCamera!!.unlock()
                mMediaRecorder!!.setCamera(mCamera)

                // Step 2: Set sources
                mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)

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
                    releaseMediaRecorder()
                    return false
                } catch (e: IOException) {
                    releaseMediaRecorder()
                    return false
                }
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    @SuppressLint("StaticFieldLeak")
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
                            if (progressDialog.isShowing) {
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
                    }, 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // prepare didn't work, release the camera
                    releaseMediaRecorder()

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
            if (progressDialog.isShowing)
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
                mSupportedPreviewSizes, surfaceView!!.width, surfaceView!!.height
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
            surfaceView!!.alpha = 1.0f
            mCamera!!.startPreview()

            val optimalSize1: Camera.Size? = mCamera!!.parameters.previewSize

            getCameraCharacteristics()
        } catch (e: IOException) {
            e.printStackTrace()
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
        if (appExitCounter != null) {
            appExitCounter!!.cancel()
            appExitCounter = null
        }

        appExitCounter = object : CountDownTimer(idleTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished1: Long) {
            }

            override fun onFinish() {
                if (appExitCounter != null) {
                    appExitCounter!!.cancel()
                    appExitCounter = null

                }
                showCustomAlert(
                    this@VideoRecordActivity.resources.getString(R.string.video_recording_timer_alert_title),
                    this@VideoRecordActivity.resources.getString(R.string.video_recording_timer_alert_message),
                    CommonUtils.TIMER_DIALOG,
                    listOf(
                        this@VideoRecordActivity.resources.getString(R.string.continue_str),
                        this@VideoRecordActivity.resources.getString(R.string.alert_exit)
                    )
                )
            }
        }
        appExitCounter!!.start()
    }

    private fun getCameraCharacteristics() {
        try {

            val mSupportedSizes = parameters!!.supportedPreviewSizes
            cameraSizesArray = arrayOfNulls<String?>(mSupportedSizes.size)
            for (i in mSupportedSizes.indices) {
                val str = mSupportedSizes[i].width.toString() + "x" + mSupportedSizes[i].height
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
            var str = sb1.toString()
            str = str.replace("000".toRegex(), "")
            cameraFPSArray = str.split("n").toTypedArray()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
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
            val separated = size.split("x").toTypedArray()
            val selectedWidth = separated[0].toInt()
            val selectedHeight = separated[1].toInt()
            if (mCamera != null) {
                if (parameters != null) {

                    val optimalSize: Camera.Size? =
                        choosePreviewSize(parameters!!, selectedWidth, selectedHeight)

                }
            }
        }
        val dialog = alert.create()
        dialog.show()
    }


    private fun choosePreviewSize(
        params: Camera.Parameters,
        width: Int,
        height: Int
    ): Camera.Size? {
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

        for (size: Camera.Size in params.supportedPreviewSizes) {
            if (size.width == width && size.height == height) {
                return size
            }
        }

        return ppsfv
    }

    override fun onBackPressed() {
        showCustomAlert(
            this@VideoRecordActivity.resources.getString(R.string.app_name),
            this@VideoRecordActivity.resources.getString(R.string.dashboard_navigation_alert_message),
            CommonUtils.BACK_PRESSED_DIALOG,
            listOf(
                this@VideoRecordActivity.resources.getString(R.string.alert_ok),
                this@VideoRecordActivity.resources.getString(R.string.alert_cancel)
            )
        )

    }


    private fun showCustomAlert(
        alertTitle: String,
        alertMessage: String,
        functionality: String,
        buttonList: List<String>
    ) {
        val customDialogModel = CustomDialogModel(
            alertTitle, alertMessage, null,
            buttonList
        )
        DialogUtils.showCustomAlert(this, customDialogModel, this, functionality)
    }

    @SuppressLint("SetTextI18n")
    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(
                this@VideoRecordActivity.resources.getString(R.string.alert_exit),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.BATTERY_DIALOG, true)) {
                try {

                    CommonUtils.appExit(this@VideoRecordActivity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (functionality.equals(CommonUtils.TIMER_DIALOG, true)) {
                try {


                    CommonUtils.appExit(this@VideoRecordActivity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (buttonName.equals(
                this@VideoRecordActivity.resources.getString(R.string.permissions_alert_option),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.PERMISSIONS_DIALOG, true)) {
                requestPermissions()
            }
        } else if (buttonName.equals(
                this@VideoRecordActivity.resources.getString(R.string.continue_str),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.TIMER_DIALOG, true)) {
                if (appExitCounter != null) {
                    appExitCounter!!.cancel()
                    appExitCounter = null
                }
                backgroundTimer()
            }
        } else if (buttonName.equals(
                this@VideoRecordActivity.resources.getString(R.string.alert_ok),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.BACK_PRESSED_DIALOG, true)) {
                try {
                    if (mOutputFile.toString()
                            .isNotEmpty() && !TextUtils.isEmpty(mOutputFile.toString()) && mOutputFile.toString() != "null"
                    ) {
                        val inputPath = mOutputFile.toString()
                        CommonUtils.deletePath(inputPath)
                    }
                    setMicMuted(false)
                    releaseMediaRecorder()
                    releaseCamera()
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                val intent = Intent(this, DashBoardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        } else if (buttonName.equals(
                this@VideoRecordActivity.resources.getString(R.string.alert_cancel),
                true
            )
        ) {
            //No action required. Just exit dialog.
        } else if (buttonName.equals(
                this@VideoRecordActivity.resources.getString(R.string.video_record_option_1),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.DIE_RECORD_OPTIONS_DIALOG, true)) {
                val typeStr = CommonUtils.ADD_DIE_TOP
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, true)
                editor.putString(CommonUtils.SAVE_DIE_TYPE, typeStr)
                editor.apply()

                dieTypeTxt!!.text = this@VideoRecordActivity.resources.getString(R.string.die_type)+ typeStr.uppercase(Locale.getDefault())
                videoOnlineImageButton!!.visibility = View.VISIBLE
            }
        } else if (buttonName.equals(
                this@VideoRecordActivity.resources.getString(R.string.video_record_option_2),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.DIE_RECORD_OPTIONS_DIALOG, true)) {
                val typeStr = CommonUtils.ADD_DIE_TOP_DETAILS + "_" + dieTopBottomDetailsCount
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, true)
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP_DETAILS, true)
                editor.putString(CommonUtils.SAVE_DIE_TYPE, typeStr)
                editor.apply()

                dieTypeTxt!!.text =this@VideoRecordActivity.resources.getString(R.string.die_type) + typeStr.uppercase(Locale.getDefault())
                videoOnlineImageButton!!.visibility = View.VISIBLE

            }
        } else if (buttonName.equals(
                this@VideoRecordActivity.resources.getString(R.string.video_record_option_3),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.DIE_RECORD_OPTIONS_DIALOG, true)) {
                val typeStr = CommonUtils.ADD_DIE_BOTTOM
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, true)
                editor.putString(CommonUtils.SAVE_DIE_TYPE, typeStr)
                editor.apply()

                dieTypeTxt!!.text = this@VideoRecordActivity.resources.getString(R.string.die_type)+ typeStr.uppercase(Locale.getDefault())
                videoOnlineImageButton!!.visibility = View.VISIBLE

            }
        } else if (buttonName.equals(
                this@VideoRecordActivity.resources.getString(R.string.video_record_option_4),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.DIE_RECORD_OPTIONS_DIALOG, true)) {
                val typeStr = CommonUtils.ADD_DIE_BOTTOM_DETAILS + "_" + dieTopBottomDetailsCount
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, true)
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM_DETAILS, true)
                editor.putString(CommonUtils.SAVE_DIE_TYPE, typeStr)
                editor.apply()

                dieTypeTxt!!.text = this@VideoRecordActivity.resources.getString(R.string.die_type)+ typeStr.uppercase(Locale.getDefault())
                videoOnlineImageButton!!.visibility = View.VISIBLE

            }
        }

    }

    private fun setMicMuted(state: Boolean) {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        setMicMuted(false)
        releaseMediaRecorder()
        releaseCamera()
    }
}
