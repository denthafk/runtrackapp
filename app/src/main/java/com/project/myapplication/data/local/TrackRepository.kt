package com.project.myapplication.data.local

import android.app.Application
import androidx.lifecycle.LiveData
import com.project.myapplication.data.model.TrackEntity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TrackRepository(application: Application) {
    private val trackDao: TrackDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val database = TrackRoomDatabase.getInstance(application)
        trackDao = database.trackDao()
    }

    fun insertTrack(track: TrackEntity) {
        executorService.execute { trackDao.insertTrack(track) }
    }

    fun getAllTrack(): LiveData<List<TrackEntity>> = trackDao.getAllTrack()

    fun deleteAllTrack() {
        executorService.execute { trackDao.deleteAllTrack() }
    }
}