package com.example.fitnessclub.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Booking",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("workoutId"),
        Index(value = ["userId", "workoutId"], unique = true)
    ]
)
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val workoutId: Long,
    val bookingDate: Long
)