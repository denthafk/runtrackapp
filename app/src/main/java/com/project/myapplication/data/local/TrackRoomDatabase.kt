package com.project.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.myapplication.data.model.TrackEntity

@Database(entities = [TrackEntity::class], version = 1, exportSchema = false)
abstract class TrackRoomDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var INSTANCE: TrackRoomDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): TrackRoomDatabase {
            if (INSTANCE == null) {
                synchronized(TrackRoomDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        TrackRoomDatabase::class.java, "RuntrackDB"
                    )
                        .build()
                }
            }
            return INSTANCE as TrackRoomDatabase
        }
    }
}