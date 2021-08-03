package com.vsoft.goodmankotlin.database


import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VideoDao {

    @Insert
    fun insert(video: VideoModel)

    @Update
    fun update(video: VideoModel)

    @Delete
    fun delete(video: VideoModel)

    @Query("delete from video_table")
    fun deleteAllVideos()

    @Query("select * from video_table where status=false")
    fun getAllVideos(): LiveData<List<VideoModel>>
}