package com.vsoft.goodmankotlin.database

import android.app.Application
import androidx.lifecycle.LiveData

class VideoRepository(application: Application) {

    private var videoDao: VideoDao
    private var allVideos: LiveData<List<VideoModel>>

    private val database = VideoDataBase.getInstance(application)

    init {
        videoDao = database.videoDao()
        allVideos = videoDao.getAllVideos(false)
    }

    fun insert(video: VideoModel) {
        subscribeOnBackground {
            videoDao.insert(video)
        }
    }

    fun update(video: VideoModel):Int {
        var status:Int=-1
        subscribeOnBackground {
            status= videoDao.update(video)
        }
        return status
    }

    fun delete(video: VideoModel) {
        subscribeOnBackground {
            videoDao.delete(video)
        }
    }
    fun getVideos(): List<VideoModel> {
           return videoDao.getVideos(false)
    }
    fun getSyncedVideos(): List<VideoModel> {
        return videoDao.getVideos(true)
    }

    fun getDieCount(dieIdStr: String, partIdStr: String, dieTypeStr1: String):Int{
        return videoDao.getDieCount(dieIdStr, partIdStr,dieTypeStr1)
    }

    fun isDieTypeExist(dieTypeStr: String):Boolean{
        return videoDao.isDieTypeExist(dieTypeStr)
    }
}