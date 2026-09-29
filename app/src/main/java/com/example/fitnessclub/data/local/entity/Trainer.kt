package com.example.fitnessclub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Trainer")
data class Trainer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val specialization: String,
    val experienceYears: Int,
    val photoUrl: String,
    val rating: Float = 4.5f,
    val description: String = ""
)