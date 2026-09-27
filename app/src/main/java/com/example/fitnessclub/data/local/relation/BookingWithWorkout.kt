package com.example.fitnessclub.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.fitnessclub.data.local.entity.Booking
import com.example.fitnessclub.data.local.entity.Workout

data class BookingWithWorkout(
    @Embedded
    val booking: Booking,

    @Relation(
        parentColumn = "workoutId",
        entityColumn = "id"
    )
    val workout: Workout
)