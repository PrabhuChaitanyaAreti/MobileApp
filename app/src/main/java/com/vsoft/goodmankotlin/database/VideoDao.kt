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

    @Query("SELECT * FROM video_table WHERE status=:status")
    fun getAllVideos(status:Boolean):LiveData<List<VideoModel>>

    @Query("SELECT * FROM video_table WHERE status=:status")
    fun getVideos(status:Boolean):List<VideoModel>

    @Query("select * from video_table")
    fun getAllVideosList():List<VideoModel>

    @Query("UPDATE video_table SET status=:status WHERE id = :id")
    fun update(status: Boolean?, id: Int):Int

    //@Query("SELECT * FROM video_table WHERE die_id=:dieIdStr AND part_id=:partIdStr AND die_top_bottom=:dieTypeStr")
    @Query("SELECT COUNT(*) FROM video_table WHERE die_id = :dieIdStr AND part_id = :partIdStr AND die_top_bottom LIKE  '%' || :dieTypeStr  || '%'")
    fun getDieCount(dieIdStr: String, partIdStr: String,dieTypeStr:String): Int
}