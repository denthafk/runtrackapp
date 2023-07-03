package com.project.myapplication.ui.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.project.myapplication.data.local.TrackRepository
import com.project.myapplication.data.model.TrackEntity

class MainViewModel(application: Application) : ViewModel() {
    private val trackRepository: TrackRepository = TrackRepository(application)

    fun insertTrack(track: TrackEntity) {
        trackRepository.insertTrack(track)
    }

    fun getAllTrack(): LiveData<List<TrackEntity>> = trackRepository.getAllTrack()

    fun deleteAllTrack() {
        trackRepository.deleteAllTrack()
    }
}