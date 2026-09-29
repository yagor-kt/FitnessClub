package com.example.fitnessclub.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "User",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val name: String,
    val weight: Float? = null,
    val goal: String? = null,
    val subscriptionEnd: Long,
    val goalVisits: Int = 12,
    val joinedAt: Long = System.currentTimeMillis()
)