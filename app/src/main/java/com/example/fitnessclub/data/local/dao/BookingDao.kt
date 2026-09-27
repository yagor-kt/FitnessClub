package com.example.fitnessclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.fitnessclub.data.local.entity.Booking
import com.example.fitnessclub.data.local.relation.BookingWithWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM Booking
            WHERE userId = :userId AND workoutId = :workoutId
        )
        """
    )
    suspend fun isBooked(userId: Long, workoutId: Long): Boolean

    @Query(
        """
        SELECT workoutId FROM Booking
        WHERE userId = :userId
        """
    )
    fun observeBookedWorkoutIds(userId: Long): Flow<List<Long>>

    @Query(
        """
        SELECT Workout.trainerName
        FROM Booking
        INNER JOIN Workout ON Booking.workoutId = Workout.id
        WHERE Booking.userId = :userId AND Workout.isPersonal = 1
        """
    )
    fun observePersonalTrainerNames(userId: Long): Flow<List<String>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM Booking
            INNER JOIN Workout ON Booking.workoutId = Workout.id
            WHERE Booking.userId = :userId
              AND Workout.trainerName = :trainerName
              AND Workout.isPersonal = 1
        )
        """
    )
    suspend fun isPersonalBooked(userId: Long, trainerName: String): Boolean

    @Insert
    suspend fun insert(booking: Booking): Long

    @Query("DELETE FROM Booking WHERE id = :bookingId")
    suspend fun deleteById(bookingId: Long): Int

    @Query(
        """
        SELECT Booking.workoutId
        FROM Booking
        INNER JOIN Workout ON Booking.workoutId = Workout.id
        WHERE Booking.id = :bookingId AND Workout.dateTime > :now
        LIMIT 1
        """
    )
    suspend fun getActiveBookingWorkoutId(bookingId: Long, now: Long): Long?

    @Transaction
    @Query(
        """
        SELECT Booking.*
        FROM Booking
        INNER JOIN Workout ON Booking.workoutId = Workout.id
        WHERE Booking.userId = :userId AND Workout.dateTime > :now
        ORDER BY Workout.dateTime ASC
        """
    )
    fun getActiveBookings(userId: Long, now: Long): Flow<List<BookingWithWorkout>>

    @Transaction
    @Query(
        """
        SELECT Booking.*
        FROM Booking
        INNER JOIN Workout ON Booking.workoutId = Workout.id
        WHERE Booking.userId = :userId AND Workout.dateTime <= :now
        ORDER BY Workout.dateTime DESC
        """
    )
    fun getBookingHistory(userId: Long, now: Long): Flow<List<BookingWithWorkout>>

    @Query(
        """
        SELECT COUNT(*)
        FROM Booking
        INNER JOIN Workout ON Booking.workoutId = Workout.id
        WHERE Booking.userId = :userId
          AND Workout.dateTime BETWEEN :monthStart AND :now
        """
    )
    fun countWorkoutsBetween(
        userId: Long,
        monthStart: Long,
        now: Long
    ): Flow<Int>
}