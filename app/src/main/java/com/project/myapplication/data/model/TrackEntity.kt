package com.project.myapplication.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "track")
@Parcelize
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "total_steps")
    var totalSteps: String? = null,

    @ColumnInfo(name = "total_duration")
    var totalDuration: String? = null,

    @ColumnInfo(name = "total_distance")
    var totalDistance: String? = null
) : Parcelable