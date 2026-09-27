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
            workoutDao.incrementBookings(workoutId)

            BookingResult.BOOKED
        }

    suspend fun cancel(bookingId: Long): Boolean = database.withTransaction {
        val deleted = bookingDao.deleteById(bookingId)
        if (deleted > 0) {
            // Сначала находим тренировку до удаления записи через связанный запрос в вызывающем слое.
            true
        } else {
            false
        }
    }
}