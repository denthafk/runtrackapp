package com.project.myapplication.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.project.myapplication.data.model.TrackEntity

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTrack(track: TrackEntity)

    @Query("SELECT * FROM track")
    fun getAllTrack(): LiveData<List<TrackEntity>>

    @Query("DELETE FROM track")
    fun deleteAllTrack()
}