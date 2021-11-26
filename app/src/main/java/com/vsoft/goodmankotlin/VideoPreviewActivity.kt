@file:Suppress("ControlFlowWithEmptyBody")

package com.vsoft.goodmankotlin

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.vsoft.goodmankotlin.database.VideoModel
import com.vsoft.goodmankotlin.database.VideoViewModel
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.utils.*
import com.vsoft.goodmankotlin.video_response.VideoAnnotationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.*

class VideoPreviewActivity : AppCompatActivity(), CustomDialogCallback, View.OnClickListener {

    private var path = ""
    private var videoFileName: String? = null
    private var isPlay = false
    private var currentPos: Long = 0
    private var totalDuration: Long = 0
    private var absPlayerInternal: SimpleExoPlayer? = null

    private var progressDialog: ProgressDialog? = null

    private var pvMain: PlayerView? = null
    private var pause: ImageView? = null

    private var retakeVideo: TextView? = null
    private var videoSubmit: TextView? = null
    private var current: TextView? = null
    private var seekBar: SeekBar? = null
    private var total: TextView? = null
    private var partIdTxt: TextView? = null
    private var dieIdTxt: TextView? = null
    private var dieTypeTxt: TextView? = null

    private lateinit var vm: VideoViewModel

    private var sharedPreferences: SharedPreferences? = null

    private var userId = ""
    private var operatorStr = ""
    private var dieIdStr = ""
    private var partIdStr = ""
    private var dieTypeStr = ""
    private var isNewDie = false
    private var isDieTop = false
    private var isDieBottom = false
    private var isDieTopDetails = false
    private var isDieBottomDetails = false
    private var isFirstDieTop = false
    private var dieTopBottomDetailsCount=0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)

        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )
        userId = sharedPreferences!!.getString(CommonUtils.LOGIN_USER_ID, "").toString()
        operatorStr = sharedPreferences!!.getString(CommonUtils.SAVE_OPERATOR_ID, "").toString()
        dieTypeStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_TYPE, "").toString()
        isNewDie = sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_NEW_DIE, false)
        isDieTop = sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_DIE_TOP, false)
        isDieBottom = sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, false)
        isDieTopDetails = sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_DIE_TOP_DETAILS, false)
        isDieBottomDetails = sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM_DETAILS, false)
        isFirstDieTop = sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_FIRST_DIE_TOP, false)
        dieIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_ID, "").toString()
        partIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_PART_ID, "").toString()

        vm = ViewModelProviders.of(this)[VideoViewModel::class.java]

        if (dieTypeStr.isNotEmpty() && !TextUtils.isEmpty(dieTypeStr) && dieTypeStr != "null") {
            var typeStr=""
            var detailsCount=0
            if(dieTypeStr.contains("_")){
                val splitArray: List<String> = dieTypeStr.split("_")
                typeStr=splitArray[0]

                detailsCount=splitArray[2].toInt()
            }else{
                typeStr=dieTypeStr
            }
            val isDieType =   vm.isDieTypeExist(typeStr)

            if(isDieType) {
               val isTopDie = typeStr == CommonUtils.ADD_DIE_TOP
                dieTopBottomDetailsCount = if(isTopDie){
                    vm.getDieCount(dieIdStr, partIdStr, "top_details")
                }else{
                    vm.getDieCount(dieIdStr, partIdStr,"bottom_details")
                }

                if(detailsCount!=0){
                    dieTopBottomDetailsCount=detailsCount+1
                }else{
                    dieTopBottomDetailsCount++
                }

            }else{
                dieTopBottomDetailsCount++
            }
        }



        pvMain = findViewById(R.id.pv_main)
        current = findViewById(R.id.current)
        total = findViewById(R.id.total)
        seekBar = findViewById(R.id.seekbar)
        partIdTxt = findViewById(R.id.partIdTxt)
        dieIdTxt = findViewById(R.id.dieIdTxt)
        dieTypeTxt = findViewById(R.id.dieTypeTxt)


        val batterLevel: Int = BatteryUtil.getBatteryPercentage(this@VideoPreviewActivity)


        if (batterLevel >= CommonUtils.BATTERY_LEVEL_PERCENTAGE) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            path = intent.extras!!.getString(CommonUtils.VIDEO_SAVING_FILE_PATH).toString()
            videoFileName = CommonUtils.getFileName(path)

            if (dieIdStr.isNotEmpty() && !TextUtils.isEmpty(dieIdStr) && dieIdStr != "null") {
                dieIdTxt!!.text = this@VideoPreviewActivity.resources.getString(R.string.die_id)+dieIdStr
                dieIdTxt!!.visibility = View.VISIBLE
            } else {
                dieIdTxt!!.visibility = View.GONE
            }
            if (partIdStr.isNotEmpty() && !TextUtils.isEmpty(partIdStr) && partIdStr != "null") {
                partIdTxt!!.text = this@VideoPreviewActivity.resources.getString(R.string.part_id)+partIdStr
                partIdTxt!!.visibility = View.VISIBLE
            } else {
                partIdTxt!!.visibility = View.GONE
            }

            if (dieTypeStr.isNotEmpty() && !TextUtils.isEmpty(dieTypeStr) && dieTypeStr != "null") {
                dieTypeTxt!!.text = this@VideoPreviewActivity.resources.getString(R.string.die_type) + dieTypeStr.uppercase(Locale.getDefault())
                dieTypeTxt!!.visibility = View.VISIBLE
            } else {
                dieTypeTxt!!.visibility = View.GONE
            }

            val appNameStringRes = R.string.app_name
            val trackSelectorDef: TrackSelector = DefaultTrackSelector()
            absPlayerInternal = SimpleExoPlayer.Builder(this)
                .setTrackSelector(trackSelectorDef)
                .build()

            val userAgent = Util.getUserAgent(this, this.getString(appNameStringRes))
            val defaultDataSourceFactory = DefaultDataSourceFactory(this, userAgent)
            val uriOfContentUrl = Uri.parse(path)
            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(defaultDataSourceFactory)
                .createMediaSource(uriOfContentUrl) // creating a media source

            absPlayerInternal!!.prepare(mediaSource)

            //absPlayerInternal.setPlayWhenReady(true); // start loading video and play it at the moment a chunk of it is available offline
            pvMain!!.player = absPlayerInternal // attach surface to the view

            absPlayerInternal!!.repeatMode = Player.REPEAT_MODE_OFF
            pvMain!!.keepScreenOn = true

            pvMain!!.hideController()
            pvMain!!.setControllerVisibilityListener { i ->
                if (i == 0) {
                    pvMain!!.hideController()
                }
            }
            pause = findViewById(R.id.pause)
            pause!!.setImageResource(R.drawable.new_video_record_play)
            pause!!.setOnClickListener(this)

            retakeVideo = findViewById(R.id.retakeVideo)
            retakeVideo!!.setOnClickListener(this)

            videoSubmit = findViewById(R.id.videoSubmit)
            if (isNewDie) {
                videoSubmit!!.text = this@VideoPreviewActivity.resources.getString(R.string.save)
            } else {
                videoSubmit!!.text =
                    this@VideoPreviewActivity.resources.getString(R.string.btn_submit)
            }
            videoSubmit!!.setOnClickListener(this)

            // Video preview with Exoplayer
            videoPreview()

        } else {
            showCustomAlert(
                this@VideoPreviewActivity.resources.getString(R.string.battery_alert_title),
                this@VideoPreviewActivity.resources.getString(R.string.battery_alert_message),
                CommonUtils.BATTERY_DIALOG,
                listOf(this@VideoPreviewActivity.resources.getString(R.string.alert_exit))
            )
        }
    }

    private fun videoPreview() {
        absPlayerInternal!!.addListener(object : Player.EventListener {
            override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
                val dur = absPlayerInternal!!.duration
                if (dur > 0) {
                    totalDuration = absPlayerInternal!!.duration.toDouble().toLong()
                    setVideoProgress()
                }
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray,
                trackSelections: TrackSelectionArray
            ) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    isPlay = false
                    currentPos = 0
                    absPlayerInternal!!.seekTo(currentPos)
                    absPlayerInternal!!.playWhenReady = false
                    pause!!.setImageResource(R.drawable.new_video_record_play)
                    val dur = absPlayerInternal!!.duration
                    if (dur > 0) {
                        totalDuration = absPlayerInternal!!.duration.toDouble().toLong()
                        setVideoProgress()
                    }
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
            override fun onPlayerError(error: ExoPlaybackException) {}
            override fun onPositionDiscontinuity(reason: Int) {}
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
            override fun onSeekProcessed() {}
        })
    }

    override fun onClick(v: View?) {
        if (v == retakeVideo) {
            showCustomAlert(
                this@VideoPreviewActivity.resources.getString(R.string.app_name),
                this@VideoPreviewActivity.resources.getString(R.string.video_preview_ratake),
                CommonUtils.RETAKE_DIALOG,
                listOf(
                    this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                    this@VideoPreviewActivity.resources.getString(R.string.alert_no)
                )
            )
        }
        if (v == videoSubmit) {
            if (isNewDie) {
                saveVideo()
            } else {
                submitVideo()
            }
        }
        if (v == pause) {
            playPauseVideo()
        }
    }

    private fun playPauseVideo() {
        if (isPlay) {
            isPlay = false
            absPlayerInternal!!.playWhenReady = false
            pause!!.setImageResource(R.drawable.new_video_record_play)
        } else {
            isPlay = true
            absPlayerInternal!!.playWhenReady = true
            pause!!.setImageResource(R.drawable.new_video_record_pause)
        }
    }

    private fun saveVideo() {
        if (isDieTop && isDieBottom ) {
            val timeStamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            vm.insert(
                VideoModel(
                    dieIdStr,
                    partIdStr,
                    path,
                    timeStamp,
                    false,
                    dieTypeStr,
                    userId,operatorStr
                )
            )

            if (isDieTop && isDieTopDetails && isFirstDieTop) {
                if (isDieBottom ) {
                    showCustomAlert(
                        this@VideoPreviewActivity.resources.getString(R.string.app_name),
                        this@VideoPreviewActivity.resources.getString(R.string.video_preview_bottom_die_details_message)+" "+dieTopBottomDetailsCount+" ?",
                        CommonUtils.DIE_BOTTOM_DETAIL_DIALOG,
                        listOf(
                            this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                            this@VideoPreviewActivity.resources.getString(R.string.alert_no)
                        )
                    )
                }else {
                    showCustomAlert(
                        this@VideoPreviewActivity.resources.getString(R.string.app_name),
                        this@VideoPreviewActivity.resources.getString(R.string.video_preview_top_die_details_message)+" "+dieTopBottomDetailsCount+" ?",
                        CommonUtils.DIE_TOP_DETAIL_DIALOG,
                        listOf(
                            this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                            this@VideoPreviewActivity.resources.getString(R.string.alert_no)
                        )
                    )
                }
            }else if (isDieBottom && isDieBottomDetails ) {
                if (isDieTop){
                    showCustomAlert(
                        this@VideoPreviewActivity.resources.getString(R.string.app_name),
                        this@VideoPreviewActivity.resources.getString(R.string.video_preview_top_die_details_message)+" "+dieTopBottomDetailsCount+" ?",
                        CommonUtils.DIE_TOP_DETAIL_DIALOG,
                        listOf(
                            this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                            this@VideoPreviewActivity.resources.getString(R.string.alert_no)
                        )
                    )
                }else {
                    showCustomAlert(
                        this@VideoPreviewActivity.resources.getString(R.string.app_name),
                        this@VideoPreviewActivity.resources.getString(R.string.video_preview_bottom_die_details_message)+" "+dieTopBottomDetailsCount+" ?",
                        CommonUtils.DIE_BOTTOM_DETAIL_DIALOG,
                        listOf(
                            this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                            this@VideoPreviewActivity.resources.getString(R.string.alert_no)
                        )
                    )
                }
            }else{
                showCustomAlert(
                    this@VideoPreviewActivity.resources.getString(R.string.app_name),
                    this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_1),
                    CommonUtils.DIE_BOTH_DIALOG,
                    listOf(this@VideoPreviewActivity.resources.getString(R.string.alert_ok))
                )
            }
        } else if (isDieTop) {
            val timeStamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            vm.insert(
                VideoModel(
                    dieIdStr,
                    partIdStr,
                    path,
                    timeStamp,
                    false,
                    dieTypeStr,
                    userId,
                    operatorStr
                )
            )
            showCustomAlert(
                this@VideoPreviewActivity.resources.getString(R.string.app_name),
                this@VideoPreviewActivity.resources.getString(R.string.video_preview_top_die_details_message)+" "+dieTopBottomDetailsCount+" ?",
                CommonUtils.DIE_TOP_DETAIL_DIALOG,
                listOf(
                    this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                    this@VideoPreviewActivity.resources.getString(R.string.alert_no)
                )
            )


        } else {
            val timeStamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            vm.insert(
                VideoModel(
                    dieIdStr,
                    partIdStr,
                    path,
                    timeStamp,
                    false,
                    dieTypeStr,
                    userId,operatorStr
                )
            )
            showCustomAlert(
                this@VideoPreviewActivity.resources.getString(R.string.app_name),
                this@VideoPreviewActivity.resources.getString(R.string.video_preview_bottom_die_details_message)+" "+dieTopBottomDetailsCount+" ?",
                CommonUtils.DIE_BOTTOM_DETAIL_DIALOG,
                listOf(
                    this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                    this@VideoPreviewActivity.resources.getString(R.string.alert_no)
                )
            )
        }
    }

    private fun submitVideo() {

        if (absPlayerInternal!!.isPlaying) {
            absPlayerInternal!!.stop()
        }
        pause!!.isEnabled = false
        pause!!.setOnClickListener(null)
        seekBar!!.isEnabled = false
        seekBar!!.setOnSeekBarChangeListener(null)
        progressDialog = ProgressDialog(this@VideoPreviewActivity)
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage(this@VideoPreviewActivity.resources.getString(R.string.progress_dialog_message_video_preview))
        if (NetworkUtils.isNetworkAvailable(this@VideoPreviewActivity)) {
            Handler(Looper.getMainLooper()).post {
                progressDialog!!.show()
            }
            val file = File(path) // initialize file here
            val filePart = MultipartBody.Part.createFormData(
                "video",
                file.name,
                RequestBody.create(MediaType.parse("video/*"), file)
            )
            val call: Call<VideoAnnotationResponse?>? =
                RetrofitClient.getInstance()!!.getMyApi()!!.uploadDyeVideo(filePart)

            val thread = Thread {
                try {
                    //Your code goes here
                    val request: Request = call!!.clone().request()
                    val client = OkHttpClient()
                    val test = client.newCall(request).execute()
                    println(test.body()!!.string())
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()
            call!!.enqueue(object : Callback<VideoAnnotationResponse?> {
                override fun onResponse(
                    call: Call<VideoAnnotationResponse?>,
                    response: Response<VideoAnnotationResponse?>
                ) {
                    try {
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }

                        if (response.code() == 200) {
                            if (response.body()?.gt != null) {
                                // Storing data into SharedPreferences
                                val sharedPreferences =
                                    getSharedPreferences(
                                        CommonUtils.SHARED_PREF_FILE,
                                        MODE_PRIVATE
                                    )
                                // Creating an Editor object to edit(write to the file)
                                val myEdit = sharedPreferences.edit()
                                // Storing the key and its value as the data fetched from edittext
                                // Once the changes have been made,
                                // we need to commit to apply those changes made,
                                // otherwise, it will throw an error
                                val gson = Gson()
                                val json: String = gson.toJson(response.body())
                                myEdit.putString(CommonUtils.RESPONSE, json)
                                myEdit.apply()
                                val intent = Intent(
                                    this@VideoPreviewActivity,
                                    MaskingActivity::class.java
                                )
                                startActivity(intent)
                                finish()
                            } else {
                                if (progressDialog!!.isShowing) {
                                    progressDialog!!.dismiss()
                                }
                                DialogUtils.showCustomAlert(
                                    this@VideoPreviewActivity,
                                    CustomDialogModel(
                                        this@VideoPreviewActivity.resources.getString(R.string.app_name),
                                        "Empty data, Contact Admin.",
                                        null,
                                        listOf(
                                            this@VideoPreviewActivity.resources.getString(
                                                R.string.alert_ok
                                            )
                                        )
                                    ), this@VideoPreviewActivity, "invalidResponse"
                                )

                            }
                        } else if (response.code() == 404) {
                            if (progressDialog!!.isShowing) {
                                progressDialog!!.dismiss()
                            }
                            DialogUtils.showCustomAlert(
                                this@VideoPreviewActivity,
                                CustomDialogModel(
                                    this@VideoPreviewActivity.resources.getString(R.string.app_name),
                                    "Something went wrong, Contact Admin.",
                                    null,
                                    listOf(
                                        this@VideoPreviewActivity.resources.getString(
                                            R.string.alert_ok
                                        )
                                    )
                                ), this@VideoPreviewActivity, "invalidResponse"
                            )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (progressDialog!!.isShowing) {
                            progressDialog!!.dismiss()
                        }
                    }
                }

                override fun onFailure(call: Call<VideoAnnotationResponse?>, t: Throwable) {
                    // DialogUtils.showNormalAlert(VideoPreviewActivity.this, "Alert!!", "" + t);
                    val intent =
                        Intent(this@VideoPreviewActivity, MaskingActivity::class.java)
                    startActivity(intent)
                    finish()
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                }
            })
        } else {
            showCustomAlert(
                this@VideoPreviewActivity.resources.getString(R.string.app_name),
                this@VideoPreviewActivity.resources.getString(R.string.network_alert_message),
                CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                listOf(this@VideoPreviewActivity.resources.getString(R.string.alert_ok))
            )
        }
    }


    // display video progress
    fun setVideoProgress() {
        //get the video duration
        currentPos = absPlayerInternal!!.currentPosition

        //display video duration
        total!!.text = timeConversion(totalDuration)
        current!!.text = timeConversion(currentPos)
        seekBar!!.max = totalDuration.toInt()
        val handler = Handler(Looper.getMainLooper())
        val runnable: Runnable = object : Runnable {
            override fun run() {
                try {
                    currentPos = absPlayerInternal!!.currentPosition
                    current!!.text = timeConversion(currentPos)
                    seekBar!!.progress = currentPos.toInt()
                    handler.postDelayed(this, 1000)
                } catch (ed: IllegalStateException) {
                    ed.printStackTrace()
                }
            }
        }
        handler.postDelayed(runnable, 1000)

        //seekbar change listener
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                currentPos = seekBar.progress.toLong()
                absPlayerInternal!!.seekTo(currentPos)
            }
        })
    }

    //time conversion
    fun timeConversion(value: Long): String {
        val songTime: String
        val dur = value.toInt()
        val hrs = dur / 3600000
        val mns = dur / 60000 % 60000
        val scs = dur % 60000 / 1000
        songTime = if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mns, scs)
        } else {
            String.format("%02d:%02d", mns, scs)
        }
        return songTime
    }

    override fun onPause() {
        super.onPause()
        if (absPlayerInternal!!.isPlaying) {
            absPlayerInternal!!.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        absPlayerInternal!!.release()
        if (absPlayerInternal!!.isPlaying) {
            absPlayerInternal!!.stop()
        }
    }

    override fun onBackPressed() {
        showCustomAlert(
            this@VideoPreviewActivity.resources.getString(R.string.app_name),
            this@VideoPreviewActivity.resources.getString(R.string.video_preview_alert_message),
            CommonUtils.BACK_PRESSED_DIALOG,
            listOf(
                this@VideoPreviewActivity.resources.getString(R.string.alert_ok),
                this@VideoPreviewActivity.resources.getString(R.string.alert_cancel)
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

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(
                this@VideoPreviewActivity.resources.getString(R.string.alert_exit),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.BATTERY_DIALOG, true)) {
                try {
                    CommonUtils.appExit(this@VideoPreviewActivity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (buttonName.equals(
                this@VideoPreviewActivity.resources.getString(R.string.alert_ok),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.DIE_BOTH_DIALOG, true)) {
                val intent = Intent(this@VideoPreviewActivity, DashBoardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            if (functionality.equals(CommonUtils.BACK_PRESSED_DIALOG, true)) {
                val intent = Intent(this, DashBoardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else if (functionality.equals(CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG, true)) {
                //No action required. Just exit dialog.
            }
        }
        if (buttonName.equals(
                this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                true
            )
        ) {
            when {
                functionality.equals(CommonUtils.DIE_TOP_DETAIL_DIALOG, true) -> {
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, true)
                    editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP_DETAILS, true)
                    editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_TOP_DETAILS+"_"+dieTopBottomDetailsCount)
                    editor.apply()

                    val intent = Intent(this@VideoPreviewActivity, VideoRecordActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                functionality.equals(CommonUtils.DIE_BOTTOM_DETAIL_DIALOG, true) -> {
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, true)
                    editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM_DETAILS, true)
                    editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_BOTTOM_DETAILS+"_"+dieTopBottomDetailsCount)
                    editor.apply()

                    val intent = Intent(this@VideoPreviewActivity, VideoRecordActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                functionality.equals(CommonUtils.DIE_TOP_DIALOG, true) -> {
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, true)
                    editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_BOTTOM)
                    editor.apply()

                    val intent = Intent(this@VideoPreviewActivity, VideoRecordActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                functionality.equals(CommonUtils.DIE_BOTTOM_DIALOG, true) -> {
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, true)
                    editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_TOP)
                    editor.apply()

                    val intent = Intent(this@VideoPreviewActivity, VideoRecordActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                functionality.equals(CommonUtils.RETAKE_DIALOG, true) -> {
                    absPlayerInternal!!.release()
                    if (absPlayerInternal!!.isPlaying) {
                        absPlayerInternal!!.stop()
                    }
                    CommonUtils.deletePath(path)
                    val intent = Intent(this@VideoPreviewActivity, VideoRecordActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        }
        if (buttonName.equals(
                this@VideoPreviewActivity.resources.getString(R.string.alert_no),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.DIE_TOP_DETAIL_DIALOG, true)) {
              if(isDieBottom&&isDieBottomDetails){
                  showCustomAlert(
                      this@VideoPreviewActivity.resources.getString(R.string.app_name),
                      this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_1),
                      CommonUtils.DIE_BOTH_DIALOG,
                      listOf(this@VideoPreviewActivity.resources.getString(R.string.alert_ok))
                  )
              }else{
                  val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                  editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP_DETAILS, true)
                  editor.putBoolean(CommonUtils.IS_VIDEO_RECORD_SCREEN, false)
                  editor.apply()
                  showCustomAlert(
                      this@VideoPreviewActivity.resources.getString(R.string.app_name),
                      this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_2),
                      CommonUtils.DIE_TOP_DIALOG,
                      listOf(
                          this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                          this@VideoPreviewActivity.resources.getString(R.string.alert_no)
                      )
                  )
              }

            } else if (functionality.equals(CommonUtils.DIE_BOTTOM_DETAIL_DIALOG, true)) {
               if(isDieTop&&isDieTopDetails){
                   showCustomAlert(
                       this@VideoPreviewActivity.resources.getString(R.string.app_name),
                       this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_1),
                       CommonUtils.DIE_BOTH_DIALOG,
                       listOf(this@VideoPreviewActivity.resources.getString(R.string.alert_ok))
                   )
               }else{

                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                     editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM_DETAILS, true)
                   editor.putBoolean(CommonUtils.IS_VIDEO_RECORD_SCREEN, false)
                    editor.apply()
                   showCustomAlert(
                       this@VideoPreviewActivity.resources.getString(R.string.app_name),
                       this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_3),
                       CommonUtils.DIE_BOTTOM_DIALOG,
                       listOf(
                           this@VideoPreviewActivity.resources.getString(R.string.alert_yes),
                           this@VideoPreviewActivity.resources.getString(R.string.alert_no)
                       )
                   )
               }
            } else if (functionality.equals(CommonUtils.DIE_TOP_DIALOG, true)) {
                showCustomAlert(
                    this@VideoPreviewActivity.resources.getString(R.string.app_name),
                    this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_1),
                    CommonUtils.DIE_BOTH_DIALOG,
                    listOf(this@VideoPreviewActivity.resources.getString(R.string.alert_ok))
                )
            } else if (functionality.equals(CommonUtils.DIE_BOTTOM_DIALOG, true)) {
                showCustomAlert(
                    this@VideoPreviewActivity.resources.getString(R.string.app_name),
                    this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_1),
                    CommonUtils.DIE_BOTH_DIALOG,
                    listOf(this@VideoPreviewActivity.resources.getString(R.string.alert_ok))
                )
            }
            else if (functionality.equals(CommonUtils.RETAKE_DIALOG, true)) {

            }
        }
    }

}