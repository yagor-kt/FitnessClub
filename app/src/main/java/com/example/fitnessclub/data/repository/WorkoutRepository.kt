package com.example.fitnessclub.data.repository

import com.example.fitnessclub.data.local.dao.BookingDao
import com.example.fitnessclub.data.local.dao.WorkoutDao
import com.example.fitnessclub.data.local.entity.Workout
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val bookingDao: BookingDao
) {
    fun getAll(): Flow<List<Workout>> = workoutDao.getAll()

    fun observeBookedWorkoutIds(userId: Long): Flow<List<Long>> =
        bookingDao.observeBookedWorkoutIds(userId)

    suspend fun getById(workoutId: Long): Workout? =
        workoutDao.getById(workoutId)
}