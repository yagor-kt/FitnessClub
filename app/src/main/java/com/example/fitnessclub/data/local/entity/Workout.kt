package com.example.fitnessclub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Workout")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val trainerName: String,
    val dateTime: Long,
    val hall: String,
    val maxCapacity: Int,
    val currentBookings: Int,
    val isPersonal: Boolean,
    val type: String = "Кардио"
)