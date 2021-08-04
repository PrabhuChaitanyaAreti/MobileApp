package com.vsoft.goodmankotlin.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VideoModel::class], version = 1)
abstract class VideoDataBase : RoomDatabase() {

    abstract fun videoDao(): VideoDao

    companion object {
        private var instance: VideoDataBase? = null

        @Synchronized
        fun getInstance(ctx: Context): VideoDataBase {
            if(instance == null)
                instance = Room.databaseBuilder(ctx.applicationContext, VideoDataBase::class.java,
                    "video_database")
                    .fallbackToDestructiveMigration()
                  //  .addCallback(roomCallback)
                    .build()

            return instance!!

        }

    }



}
