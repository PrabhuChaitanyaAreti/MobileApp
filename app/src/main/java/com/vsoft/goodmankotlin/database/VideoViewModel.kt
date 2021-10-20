package com.vsoft.goodmankotlin.database


import android.app.Application
import androidx.lifecycle.AndroidViewModel

class VideoViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = VideoRepository(app)

    fun insert(video: VideoModel) {
        repository.insert(video)
    }

    fun update(video: VideoModel):Int {
       return repository.update(video)
    }

    fun delete(video: VideoModel) {
        repository.delete(video)
    }
    fun getVideos(): List<VideoModel> {
        return repository.getVideos()
    }
    fun getSyncedVideos(): List<VideoModel> {
        return repository.getSyncedVideos()
    }

    fun getDieCount(dieIdStr: String, partIdStr: String, dieTypeStr1: String):Int{
        return repository.getDieCount(dieIdStr,partIdStr,dieTypeStr1)
    }

    fun isDieTypeExist(dieTypeStr: String):Boolean{
        return repository.isDieTypeExist(dieTypeStr)
    }
}