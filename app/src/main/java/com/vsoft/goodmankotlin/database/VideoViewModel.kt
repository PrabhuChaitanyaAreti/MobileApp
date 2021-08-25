package com.vsoft.goodmankotlin.database


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class VideoViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = VideoRepository(app)
    private val allVideos = repository.getAllVideos()

    fun insert(video: VideoModel) {
        repository.insert(video)
    }

    fun update(video: VideoModel):Int {
       return repository.update(video)
    }

    fun delete(video: VideoModel) {
        repository.delete(video)
    }

    fun deleteAllVideos() {
        repository.deleteAllVideos()
    }

    fun getAllVideos(): LiveData<List<VideoModel>> {
        return allVideos
    }
    fun getVideos():List<VideoModel>?{
        return repository.getVideos()
    }
    fun getAllVideosList():List<VideoModel>?{
        return repository.getAllVideosList()
    }
    fun updateSyncStatus(id:Int?):Int{
        return repository.updateSyncStatus(id)
    }
}