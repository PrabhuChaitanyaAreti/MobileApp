package com.vsoft.goodmankotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_video_preview.*

class VideoPreviewActivity : AppCompatActivity() {
    val TAG = VideoPreviewActivity::class.java.simpleName
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
    }
}