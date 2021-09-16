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

    fun deleteAllVideos() {
        subscribeOnBackground {
            videoDao.deleteAllVideos()
        }
    }

    fun getAllVideos(): LiveData<List<VideoModel>> {
        return allVideos
    }
    fun getVideos(): List<VideoModel> {
           return videoDao.getVideos(false)
    }
    fun getAllVideosList(): List<VideoModel> {
        return videoDao.getAllVideosList()
    }
    fun updateSyncStatus(id:Int?):Int{
        var status:Int=-1
        subscribeOnBackground {
            status= videoDao.update(false, id!!)
        }
        return status
    }
}