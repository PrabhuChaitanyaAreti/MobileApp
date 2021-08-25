package com.vsoft.goodmankotlin.database


import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VideoDao {

    @Insert
    fun insert(video: VideoModel)

    @Update
    fun update(video: VideoModel):Int

    @Delete
    fun delete(video: VideoModel)

    @Query("delete from video_table")
    fun deleteAllVideos()

    @Query("select * from video_table where status="+false)
    fun getAllVideos(): LiveData<List<VideoModel>>

    @Query("select * from video_table where status="+false)
    fun getVideos():List<VideoModel>

    @Query("select * from video_table")
    fun getAllVideosList():List<VideoModel>

    @Query("UPDATE video_table SET status=:status WHERE id = :id")
    fun update(status: Boolean?, id: Int):Int
}