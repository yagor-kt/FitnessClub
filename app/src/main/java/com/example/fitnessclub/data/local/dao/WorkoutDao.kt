package com.example.fitnessclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitnessclub.data.local.entity.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM Workout ORDER BY dateTime ASC")
    fun getAll(): Flow<List<Workout>>

    @Query("SELECT * FROM Workout WHERE id = :workoutId LIMIT 1")
    suspend fun getById(workoutId: Long): Workout?

    @Insert
    suspend fun insert(workout: Workout): Long

    @Query(
        """
        UPDATE Workout
        SET currentBookings = currentBookings + 1
        WHERE id = :workoutId AND currentBookings < maxCapacity
        """
    )
    suspend fun incrementBookings(workoutId: Long): Int

    @Query(
        """
        UPDATE Workout
        SET currentBookings = CASE
            WHEN currentBookings > 0 THEN currentBookings - 1
            ELSE 0
        END
        WHERE id = :workoutId
        """
    )
    suspend fun decrementBookings(workoutId: Long): Int
}