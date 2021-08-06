package com.vsoft.goodmankotlin.database


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "video_table")
data class VideoModel(val die_id: String,
                      val part_id: String,
                      val video_path: String,
                      val time_stamp: String,
                      var status: Boolean,
                      var die_top_bottom: String,
                      @PrimaryKey(autoGenerate = false) val id: Int? = null)