package com.example.fitnessclub.data.repository

import androidx.room.withTransaction
import com.example.fitnessclub.data.local.AppDatabase
import com.example.fitnessclub.data.local.dao.BookingDao
import com.example.fitnessclub.data.local.dao.WorkoutDao
import com.example.fitnessclub.data.local.entity.Booking
import com.example.fitnessclub.data.local.relation.BookingWithWorkout
import kotlinx.coroutines.flow.Flow

enum class BookingResult {
    BOOKED,
    FULL,
    ALREADY_BOOKED,
    NOT_FOUND
}

class BookingRepository(
    private val database: AppDatabase,
    private val bookingDao: BookingDao,
    private val workoutDao: WorkoutDao
) {
    fun getActiveBookings(userId: Long): Flow<List<BookingWithWorkout>> =
        bookingDao.getActiveBookings(userId, System.currentTimeMillis())

    fun getBookingHistory(userId: Long): Flow<List<BookingWithWorkout>> =
        bookingDao.getBookingHistory(userId, System.currentTimeMillis())

    suspend fun book(userId: Long, workoutId: Long): BookingResult =
        database.withTransaction {
            val workout = workoutDao.getById(workoutId)
                ?: return@withTransaction BookingResult.NOT_FOUND

            if (bookingDao.isBooked(userId, workoutId)) {
                return@withTransaction BookingResult.ALREADY_BOOKED
            }

            if (workout.maxCapacity - workout.currentBookings <= 0) {
                return@withTransaction BookingResult.FULL
            }

            bookingDao.insert(
                Booking(
                    userId = userId,
                    workoutId = workoutId,
                    bookingDate = System.currentTimeMillis()
                )
            )

            val updatedRows = workoutDao.incrementBookings(workoutId)
            if (updatedRows == 0) {
                error("Не удалось обновить количество записей")
            }

            BookingResult.BOOKED
        }

    suspend fun cancel(bookingId: Long): Boolean = database.withTransaction {
        val workoutId = bookingDao.getActiveBookingWorkoutId(
            bookingId = bookingId,
            now = System.currentTimeMillis()
        ) ?: return@withTransaction false

        val deletedRows = bookingDao.deleteById(bookingId)
        if (deletedRows == 0) {
            return@withTransaction false
        }

        workoutDao.decrementBookings(workoutId)
        true
    }
}