package com.vsoft.goodmankotlin


import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.media2.exoplayer.external.ExoPlayerFactory
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.vsoft.goodmankotlin.model.PunchResponse
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils
import com.vsoft.goodmankotlin.utils.NetworkUtils
import com.vsoft.goodmankotlin.utils.RetrofitClient
import kotlinx.android.synthetic.main.activity_video_preview.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class VideoPreviewActivity : AppCompatActivity() {
    val TAG = VideoPreviewActivity::class.java.simpleName
    private var path: String? = null
    private  var videofilename: String? = null
    private var isplay = false
    private var seekBar: SeekBar? = null
    private var current_pos: Long = 0
    private  var total_duration: Long =0
    private var mHandler: Handler? = null
    private  var handler: Handler? = null
    //private var isVisible = true
    private var absPlayerInternal: SimpleExoPlayer? = null

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)
        path = intent.extras!!.getString("videoSavingFilePath")
        Log.d(TAG, "VideoPreviewActivity onCreate $path")
        videofilename = CommonUtils.getFileName(path!!)
        println("VideoPreviewActivity videofilename is $videofilename")
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val width =
                Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
            val height =
                Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            retriever.release()
            println("video width  is $width")
            println("video height  is $height")
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        pv_main.setPlayer(absPlayerInternal) // attach surface to the view

        absPlayerInternal!!.repeatMode = Player.REPEAT_MODE_ALL
        pv_main.setKeepScreenOn(true)

        pv_main.hideController()
        pv_main.setControllerVisibilityListener(object : PlayerControlView.VisibilityListener {
            override fun onVisibilityChange(i: Int) {
                if (i == 0) {
                    pv_main.hideController()
                }
            }
        })
        val pause = findViewById<ImageView>(R.id.pause)
        pause.setImageResource(R.drawable.video_record_play)
        pause.setOnClickListener {
            if (isplay) {
                isplay = false
                //  pvMain.onPause();
                absPlayerInternal!!.setPlayWhenReady(false)
                pause.setImageResource(R.drawable.video_record_play)
            } else {
                isplay = true
                //  pvMain.onResume();
                absPlayerInternal!!.setPlayWhenReady(true)
                pause.setImageResource(R.drawable.video_record_pause)
            }
        }
        val retakeVideo = findViewById<TextView>(R.id.retakeVideo)
        retakeVideo.setOnClickListener {
            if (absPlayerInternal!!.isPlaying()) {
                absPlayerInternal!!.stop()
            }
            val intent = Intent(this@VideoPreviewActivity, VideoRecordingActivity::class.java)
            startActivity(intent)
            finish()
        }
        val videoSubmit = findViewById<TextView>(R.id.videoSubmit)
        videoSubmit.setOnClickListener {
            if (absPlayerInternal!!.isPlaying()) {
                absPlayerInternal!!.stop()
            }
            pause.isEnabled = false
            pause.setOnClickListener(null)
            seekBar!!.isEnabled = false
            seekBar!!.setOnSeekBarChangeListener(null)
            Log.d("TAG", "btnSendEdge onClick imagePath::: $path")
            progressDialog = ProgressDialog(this@VideoPreviewActivity)
            progressDialog!!.setCancelable(false)
            progressDialog!!.setMessage("Please wait .. Processing image may take some time.")
            if (NetworkUtils.isNetworkAvailable(this@VideoPreviewActivity)) {
                Handler().post {
                    progressDialog!!.show()
                }
                val file = File(path) // initialize file here
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    RequestBody.create(MediaType.parse("image/*"), file)
                )
                val call: Call<PunchResponse?>? =
                    RetrofitClient.getInstance()!!.getMyApi()!!.uploadDyeImage(filePart)
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
                            val intent = Intent(
                                this@VideoPreviewActivity,
                                MaskingActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                            /*if (response.isSuccessful()) {
                                    // Storing data into SharedPreferences
                                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                                    // Creating an Editor object to edit(write to the file)
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                    // Storing the key and its value as the data fetched from edittext
                                    // Once the changes have been made,
                                    // we need to commit to apply those changes made,
                                    // otherwise, it will throw an error
                                    Gson gson = new Gson();
                                    String json = gson.toJson(response.body());
                                    myEdit.putString("response", json);
                                    myEdit.apply();
                                    if (progressDialog != null && progressDialog.isShowing())
                                        progressDialog.dismiss();
                                    Intent intent = new Intent(VideoPreviewActivity.this, HamburgerMenuActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    DialogUtils.showNormalAlert(VideoPreviewActivity.this, "Error!!", "Data mismatch, Take an image and try again.");
                                }*/
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
                DialogUtils.showNormalAlert(
                    this@VideoPreviewActivity,
                    "Alert!!",
                    "Please check your internet connection and try again"
                )
            }
        }
        seekBar = findViewById<View>(R.id.seekbar) as SeekBar

        // video_index = getIntent().getIntExtra("pos" , 0);
        mHandler = Handler()
        handler = Handler()
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
    }

    // display video progress
    fun setVideoProgress() {
        //get the video duration
        current_pos = absPlayerInternal!!.getCurrentPosition()

        //display video duration
        total.text = timeConversion(total_duration as Long)
        current!!.text = timeConversion(current_pos.toLong())
        seekBar!!.max = total_duration.toInt()
        val handler = Handler()
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
//
//    // hide progress when the video is playing
//    fun hideLayout() {
//        val runnable = Runnable {
//            showProgress!!.visibility = View.VISIBLE
//            isVisible = false
//        }
//        handler!!.postDelayed(runnable, 5000)
//        relative!!.setOnClickListener {
//            mHandler!!.removeCallbacks(runnable)
//            if (isVisible) {
//                showProgress!!.visibility = View.VISIBLE
//                isVisible = false
//            } else {
//                showProgress!!.visibility = View.VISIBLE
//                mHandler!!.postDelayed(runnable, 5000)
//                isVisible = true
//            }
//        }
//    }
//

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
   /* val TAG = VideoPreviewActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)

        val videoRecordingFilePath = intent.getStringExtra("videoRecordingFilePath")
        Log.d(TAG, "onCreate videoRecordingFilePath $videoRecordingFilePath")

        videoView1.setVideoPath(
            videoRecordingFilePath)

        videoView1.start()

        submit.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@VideoPreviewActivity,
                MaskingActivity::class.java
            )
            startActivity(intent)
            finish()
        })
    }*/
}