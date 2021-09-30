package com.vsoft.goodmankotlin.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [VideoModel::class], version = 3, exportSchema = true)
abstract class VideoDataBase : RoomDatabase() {

    abstract fun videoDao(): VideoDao

    companion object {
        private var instance: VideoDataBase? = null

        @Synchronized
        fun getInstance(ctx: Context): VideoDataBase {
            if(instance == null)
                instance = Room.databaseBuilder(ctx.applicationContext, VideoDataBase::class.java,
                    "video_database")
                   // .fallbackToDestructiveMigration()
                  //  .addCallback(roomCallback)
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .allowMainThreadQueries()
                    .build()

            return instance!!

        }


        /*
     * This is used for Room Database migration 1 to 2
     */
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.e("MIGRATION_1_2", "start")
                   // database.execSQL("ALTER TABLE 'video_table' ADD COLUMN 'status' INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE 'video_table' ADD COLUMN 'die_top_bottom' TEXT NOT NULL DEFAULT 'Unknown'")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /*
   * This is used for Room Database migration 1 to 2
   */
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.e("MIGRATION_2_3", "start")
                    // database.execSQL("ALTER TABLE 'video_table' ADD COLUMN 'status' INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE 'video_table' ADD COLUMN 'user_id' TEXT NOT NULL DEFAULT 'Unknown'")
                    database.execSQL("ALTER TABLE 'video_table' ADD COLUMN 'operator_id' TEXT NOT NULL DEFAULT 'Unknown'")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }



}
