package com.example.fitnessclub

import android.app.Application
import com.example.fitnessclub.data.local.AppDatabase
import com.example.fitnessclub.data.repository.BookingRepository
import com.example.fitnessclub.data.repository.TrainerRepository
import com.example.fitnessclub.data.repository.UserRepository
import com.example.fitnessclub.data.repository.WorkoutRepository
import com.example.fitnessclub.data.session.SessionManager

class App: Application() {
    val database: AppDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppDatabase.getInstance(applicationContext)
    }

    val sessionManager: SessionManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        SessionManager(applicationContext)
    }

    val userRepository: UserRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        UserRepository(
            userDao = database.userDao(),
            bookingDao = database.bookingDao()
        )
    }

    val workoutRepository: WorkoutRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        WorkoutRepository(
            workoutDao = database.workoutDao(),
            bookingDao = database.bookingDao()
        )
    }

    val bookingRepository: BookingRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        BookingRepository(
            database = database,
            bookingDao = database.bookingDao(),
            workoutDao = database.workoutDao()
        )
    }

    val trainerRepository: TrainerRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        TrainerRepository(
            database = database,
            trainerDao = database.trainerDao(),
            bookingDao = database.bookingDao(),
            workoutDao =  database.workoutDao()
        )
    }
}