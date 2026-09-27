package com.example.fitnessclub.data.repository

import androidx.room.withTransaction
import com.example.fitnessclub.data.local.AppDatabase
import com.example.fitnessclub.data.local.dao.BookingDao
import com.example.fitnessclub.data.local.dao.TrainerDao
import com.example.fitnessclub.data.local.dao.WorkoutDao
import com.example.fitnessclub.data.local.entity.Booking
import com.example.fitnessclub.data.local.entity.Trainer
import com.example.fitnessclub.data.local.entity.Workout
import kotlinx.coroutines.flow.Flow

enum class PersonalBookingResult {
    BOOKED,
    ALREADY_BOOKED
}

class TrainerRepository(
    private val database: AppDatabase,
    private val trainerDao: TrainerDao,
    private val bookingDao: BookingDao,
    private val workoutDao: WorkoutDao
) {
    fun getAll(): Flow<List<Trainer>> = trainerDao.getAll()

    fun observePersonalTrainerNames(userId: Long): Flow<List<String>> =
        bookingDao.observePersonalTrainerNames(userId)

    suspend fun bookPersonal(
        userId: Long,
        trainer: Trainer
    ): PersonalBookingResult = database.withTransaction {
        if (bookingDao.isPersonalBooked(userId, trainer.name)) {
            return@withTransaction PersonalBookingResult.ALREADY_BOOKED
        }

        val workoutId = workoutDao.insert(
            Workout(
                title = "Персональная тренировка",
                trainerName = trainer.name,
                dateTime = System.currentTimeMillis() + 24L * 60 * 60 * 1000,
                hall = "Зал персональных тренировок",
                maxCapacity = 1,
                currentBookings = 1,
                isPersonal = true
            )
        )

        bookingDao.insert(
            Booking(
                userId = userId,
                workoutId = workoutId,
                bookingDate = System.currentTimeMillis()
            )
        )

        PersonalBookingResult.BOOKED
    }
}