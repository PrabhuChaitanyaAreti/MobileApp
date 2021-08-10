package com.vsoft.goodmankotlin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.vsoft.goodmankotlin.database.VideoModel
import com.vsoft.goodmankotlin.database.VideoViewModel
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.model.PunchResponse
import com.vsoft.goodmankotlin.utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VideoPreviewActivity : AppCompatActivity(), CustomDialogCallback {

    private val TAG = VideoPreviewActivity::class.java.simpleName
    private var path= ""
    private  var videofilename: String? = null
    private var isplay = false
    private var current_pos: Long = 0
    private  var total_duration: Long =0
    private var mHandler: Handler? = null
    private  var handler: Handler? = null
    private var absPlayerInternal: SimpleExoPlayer? = null

    private var progressDialog: ProgressDialog? = null
    private var alertDialog: android.app.AlertDialog? = null

    private var pv_main:PlayerView?=null
    private var pause:ImageView?=null

    private var  retakeVideo:TextView?=null
    private var videoSubmit:TextView?=null
    private var  current:TextView?=null
    private var seekBar:SeekBar?=null
    private var  total:TextView?=null

    private lateinit var vm: VideoViewModel


    private var sharedPreferences: SharedPreferences?=null

    private var dieIdStr=  ""
    private  var partIdStr = ""
    private var dieTypeStr =""
    private var isNewDie=false
    private var isDieTop =false
    private var isDieBottom=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)

        sharedPreferences = this.getSharedPreferences(CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE)


        dieIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_DIE_ID,"").toString()
        partIdStr = sharedPreferences!!.getString(CommonUtils.SAVE_PART_ID,"").toString()
        dieTypeStr= sharedPreferences!!.getString(CommonUtils.SAVE_DIE_TYPE,"").toString()
        isNewDie=sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_NEW_DIE,false)
        isDieTop=sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_DIE_TOP,false)
        isDieBottom=sharedPreferences!!.getBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM,false)

        Log.d("TAG", "VideoPreviewActivity  sharedPreferences  dieIdStr $dieIdStr")
        Log.d("TAG", "VideoPreviewActivity sharedPreferences  partIdStr $partIdStr")
        Log.d("TAG", "VideoPreviewActivity sharedPreferences  dieTypeStr $dieTypeStr")
        Log.d("TAG", "VideoPreviewActivity sharedPreferences  IsNewDie $isNewDie")
        Log.d("TAG", "VideoPreviewActivity sharedPreferences  isDieTop $isDieTop")
        Log.d("TAG", "VideoPreviewActivity sharedPreferences  isDieBottom $isDieBottom")


        vm = ViewModelProviders.of(this)[VideoViewModel::class.java]

        pv_main=findViewById(R.id.pv_main)
        current=findViewById(R.id.current)
        total=findViewById(R.id.total)
        seekBar=findViewById(R.id.seekbar)


        val batterLevel: Int = BatteryUtil.getBatteryPercentage(this@VideoPreviewActivity)

        Log.d("TAG", "getBatteryPercentage  batterLevel $batterLevel")

        if (batterLevel >= 15) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        path = intent.extras!!.getString("videoSavingFilePath").toString()
        Log.d(TAG, "VideoPreviewActivity onCreate $path")
        videofilename = CommonUtils.getFileName(path)
        println("VideoPreviewActivity videofilename is $videofilename")

        val appNameStringRes = R.string.app_name
        val trackSelectorDef: TrackSelector = DefaultTrackSelector()
        //absPlayerInternal =
          //  ExoPlayerFactory.newSimpleInstance(this, trackSelectorDef) //creating a player instance
        absPlayerInternal = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelectorDef)
            .build()

        val userAgent = Util.getUserAgent(this, this.getString(appNameStringRes))
        val defdataSourceFactory = DefaultDataSourceFactory(this, userAgent)
        val uriOfContentUrl = Uri.parse(path)
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(defdataSourceFactory)
            .createMediaSource(uriOfContentUrl) // creating a media source


        absPlayerInternal!!.prepare(mediaSource)
        //absPlayerInternal.setPlayWhenReady(true); // start loading video and play it at the moment a chunk of it is available offline

        //absPlayerInternal.setPlayWhenReady(true); // start loading video and play it at the moment a chunk of it is available offline
        pv_main!!.setPlayer(absPlayerInternal) // attach surface to the view

        absPlayerInternal!!.repeatMode = Player.REPEAT_MODE_ALL
        pv_main!!.setKeepScreenOn(true)

        pv_main!!.hideController()
        pv_main!!.setControllerVisibilityListener(object : PlayerControlView.VisibilityListener {
            override fun onVisibilityChange(i: Int) {
                if (i == 0) {
                    pv_main!!.hideController()
                }
            }
        })
            pause = findViewById<ImageView>(R.id.pause)
        pause!!.setImageResource(R.drawable.video_record_play)
        pause!!.setOnClickListener {
            if (isplay) {
                isplay = false
                //  pvMain.onPause();
                absPlayerInternal!!.setPlayWhenReady(false)
                pause!!.setImageResource(R.drawable.video_record_play)
            } else {
                isplay = true
                //  pvMain.onResume();
                absPlayerInternal!!.setPlayWhenReady(true)
                pause!!.setImageResource(R.drawable.video_record_pause)
            }
        }

         retakeVideo = findViewById<TextView>(R.id.retakeVideo)
        retakeVideo!!.setOnClickListener {
            if (absPlayerInternal!!.isPlaying()) {
                absPlayerInternal!!.stop()
            }
            val intent = Intent(this@VideoPreviewActivity, VideoRecordActivityNew::class.java)
            startActivity(intent)
            finish()
        }
         videoSubmit = findViewById<TextView>(R.id.videoSubmit)
            if(isNewDie){
                videoSubmit!!.text=this@VideoPreviewActivity.resources.getString(R.string.save)
            }else{
                videoSubmit!!.text=this@VideoPreviewActivity.resources.getString(R.string.btn_submit)
            }
        videoSubmit!!.setOnClickListener {
            if(isNewDie){
                if(isDieTop&&isDieBottom){
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    vm.insert(VideoModel(dieIdStr, partIdStr, path,timeStamp,false,dieTypeStr))

                    showCustomAlert( this@VideoPreviewActivity.resources.getString(R.string.app_name),
                        this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_1),CommonUtils.DIE_BOTH_DIALOG,
                        listOf( this@VideoPreviewActivity.resources.getString(R.string.alert_ok)))

                }else if(isDieTop){
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    vm.insert(VideoModel(dieIdStr, partIdStr, path,timeStamp,false,dieTypeStr))

                    showCustomAlert( this@VideoPreviewActivity.resources.getString(R.string.app_name),
                        this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_2),CommonUtils.DIE_TOP_DIALOG,
                        listOf( this@VideoPreviewActivity.resources.getString(R.string.alert_ok),
                            this@VideoPreviewActivity.resources.getString(R.string.alert_cancel)))

                }else{
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    vm.insert(VideoModel(dieIdStr, partIdStr, path,timeStamp,false,dieTypeStr))

                    showCustomAlert( this@VideoPreviewActivity.resources.getString(R.string.app_name),
                        this@VideoPreviewActivity.resources.getString(R.string.video_preview_save_click_3),CommonUtils.DIE_BOTTOM_DIALOG,
                        listOf( this@VideoPreviewActivity.resources.getString(R.string.alert_ok),
                            this@VideoPreviewActivity.resources.getString(R.string.alert_cancel)))
                }
            }else{

            if (absPlayerInternal!!.isPlaying()) {
                absPlayerInternal!!.stop()
            }
            pause!!.isEnabled = false
            pause!!.setOnClickListener(null)
            seekBar!!.isEnabled = false
            seekBar!!.setOnSeekBarChangeListener(null)
            Log.d("TAG", "btnSendEdge onClick imagePath::: $path")
            progressDialog = ProgressDialog(this@VideoPreviewActivity)
            progressDialog!!.setCancelable(false)
            progressDialog!!.setMessage(this@VideoPreviewActivity.resources.getString(R.string.progress_dialog_message_video_preview))
            if (NetworkUtils.isNetworkAvailable(this@VideoPreviewActivity)) {
                Handler(Looper.getMainLooper()).post {
                    progressDialog!!.show()
                }
                val file = File(path) // initialize file here
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    RequestBody.create(MediaType.parse("image/*"), file)
                )
                val call: Call<PunchResponse?>? =
                    RetrofitClient.getInstance()!!.getMyApi()!!.uploadDyeVideo(filePart)
                call!!.enqueue(object : Callback<PunchResponse?> {
                    override fun onResponse(
                        call: Call<PunchResponse?>,
                        response: Response<PunchResponse?>
                    ) {
                        try {
                            Log.d(
                                "TAG",
                                "submit onClick onResponse message ::: " + response.message()
                            )
                            Log.d(
                                "TAG",
                                "submit onClick onResponse code ::: " + response.code()
                            )
                            // Storing data into SharedPreferences
                            val sharedPreferences =
                                getSharedPreferences(CommonUtils.SHARED_PREF_FILE, MODE_PRIVATE)
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

                        } catch (e: Exception) {
                            e.printStackTrace()
                            if (progressDialog!!.isShowing) {
                                progressDialog!!.dismiss()
                            }
                        }
                    }

                    override fun onFailure(call: Call<PunchResponse?>, t: Throwable) {
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
                showCustomAlert( this@VideoPreviewActivity.resources.getString(R.string.app_name),
                    this@VideoPreviewActivity.resources.getString(R.string.network_alert_message),CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG,
                    listOf( this@VideoPreviewActivity.resources.getString(R.string.alert_ok)))
            }
            }
        }

        // video_index = getIntent().getIntExtra("pos" , 0);
        mHandler = Handler(Looper.getMainLooper())
        handler = Handler(Looper.getMainLooper())
        absPlayerInternal!!.addListener(object : Player.EventListener {
            override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
                val dur = absPlayerInternal!!.duration
                Log.e("dur", "::::$dur:::")
                if (dur > 0) {
                    total_duration = absPlayerInternal!!.duration.toDouble().toLong()
                    setVideoProgress()
                }
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray,
                trackSelections: TrackSelectionArray
            ) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}
            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
            override fun onPlayerError(error: ExoPlaybackException) {}
            override fun onPositionDiscontinuity(reason: Int) {}
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
            override fun onSeekProcessed() {}
        })
        } else {
            showCustomAlert(this@VideoPreviewActivity.resources.getString(R.string.battery_alert_title),
                this@VideoPreviewActivity.resources.getString(R.string.battery_alert_message),CommonUtils.BATTERY_DIALOG,
                listOf(this@VideoPreviewActivity.resources.getString(R.string.alert_exit)))
        }
    }

    // display video progress
    fun setVideoProgress() {
        //get the video duration
        current_pos = absPlayerInternal!!.getCurrentPosition()

        //display video duration
        total!!.text = timeConversion(total_duration as Long)
        current!!.text = timeConversion(current_pos.toLong())
        seekBar!!.max = total_duration.toInt()
        val handler = Handler(Looper.getMainLooper())
        val runnable: Runnable = object : Runnable {
            override fun run() {
                try {
                    current_pos = absPlayerInternal!!.getCurrentPosition()
                    current!!.text = timeConversion(current_pos.toLong())
                    seekBar!!.progress = current_pos.toInt()
                    handler.postDelayed(this, 1000)
                } catch (ed: IllegalStateException) {
                    ed.printStackTrace()
                }
            }
        }
        handler.postDelayed(runnable, 1000)

        //seekbar change listner
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                current_pos = seekBar.progress.toLong()
                absPlayerInternal!!.seekTo(current_pos.toLong())
            }
        })
    }
    //time conversion
    fun timeConversion(value: Long): String? {
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
        if (absPlayerInternal!!.isPlaying()) {
            absPlayerInternal!!.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (absPlayerInternal!!.isPlaying()) {
            absPlayerInternal!!.stop()
        }
    }

   override fun onBackPressed() {
       showCustomAlert(this@VideoPreviewActivity.resources.getString(R.string.app_name),
           this@VideoPreviewActivity.resources.getString(R.string.video_preview_alert_message),
           CommonUtils.BACK_PRESSED_DIALOG,
           listOf(this@VideoPreviewActivity.resources.getString(R.string.alert_ok),
               this@VideoPreviewActivity.resources.getString(R.string.alert_cancel)))
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
        if(buttonName.equals(this@VideoPreviewActivity.resources.getString(R.string.alert_exit),true)) {
            if (functionality.equals(CommonUtils.BATTERY_DIALOG, true)) {
                try {
                    val previewIntent = Intent()
                    setResult(RESULT_CANCELED, previewIntent)
                    finishAffinity()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }else if(buttonName.equals(this@VideoPreviewActivity.resources.getString(R.string.alert_ok),true)) {
            if (functionality.equals(CommonUtils.BACK_PRESSED_DIALOG, true)) {
                val intent = Intent(this, DashBoardActivity::class.java)
                intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }else if (functionality.equals(CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG, true)) {
                //No action required. Just exit dialog.
            }else if (functionality.equals(CommonUtils.DIE_BOTH_DIALOG, true)) {
                val intent = Intent(this@VideoPreviewActivity, DashBoardActivity::class.java)
                intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }else if (functionality.equals(CommonUtils.DIE_TOP_DIALOG, true)) {
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, true)
                editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_BOTTOM)
                editor.apply()

                val intent = Intent(this@VideoPreviewActivity, VideoRecordActivityNew::class.java)
                startActivity(intent)
                finish()
            }else if (functionality.equals(CommonUtils.DIE_BOTTOM_DIALOG, true)) {
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, true)
                editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_TOP)
                editor.apply()

                val intent = Intent(this@VideoPreviewActivity, VideoRecordActivityNew::class.java)
                startActivity(intent)
                finish()
            }
        }else if(buttonName.equals(this@VideoPreviewActivity.resources.getString(R.string.alert_cancel),true)) {
            if (functionality.equals(CommonUtils.DIE_TOP_DIALOG, true)) {
                val intent = Intent(this@VideoPreviewActivity, DashBoardActivity::class.java)
                intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }else if (functionality.equals(CommonUtils.DIE_BOTTOM_DIALOG, true)) {
                val intent = Intent(this@VideoPreviewActivity, DashBoardActivity::class.java)
                intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}