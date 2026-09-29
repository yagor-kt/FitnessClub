package com.example.fitnessclub.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessclub.data.repository.BookingRepository
import com.example.fitnessclub.data.repository.TrainerRepository
import com.example.fitnessclub.data.repository.UserRepository
import com.example.fitnessclub.data.repository.WorkoutRepository
import com.example.fitnessclub.data.session.SessionManager
import com.example.fitnessclub.ui.auth.LoginViewModel
import com.example.fitnessclub.ui.auth.RegistrationViewModel
import com.example.fitnessclub.ui.bookings.BookingsViewModel
import com.example.fitnessclub.ui.dashboard.DashboardViewModel
import com.example.fitnessclub.ui.profile.ProfileViewModel
import com.example.fitnessclub.ui.schedule.ScheduleViewModel
import com.example.fitnessclub.ui.trainers.TrainersViewModel

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(
    private val application: Application,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(application, userRepository, sessionManager) as T
}

@Suppress("UNCHECKED_CAST")
class RegistrationViewModelFactory(
    private val application: Application,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RegistrationViewModel(application, userRepository, sessionManager) as T
}

@Suppress("UNCHECKED_CAST")
class DashboardViewModelFactory(
    private val application: Application,
    private val userId: Long,
    private val userRepository: UserRepository,
    private val bookingRepository: BookingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DashboardViewModel(
            application,
            userId,
            userRepository,
            bookingRepository
        ) as T
}

@Suppress("UNCHECKED_CAST")
class ScheduleViewModelFactory(
    private val application: Application,
    private val userId: Long,
    private val workoutRepository: WorkoutRepository,
    private val bookingRepository: BookingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ScheduleViewModel(
            application,
            userId,
            workoutRepository,
            bookingRepository
        ) as T
}

@Suppress("UNCHECKED_CAST")
class BookingsViewModelFactory(
    private val application: Application,
    private val userId: Long,
    private val bookingRepository: BookingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        BookingsViewModel(application, userId, bookingRepository) as T
}

@Suppress("UNCHECKED_CAST")
class ProfileViewModelFactory(
    private val application: Application,
    private val userId: Long,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProfileViewModel(application, userId, userRepository, sessionManager) as T
}

@Suppress("UNCHECKED_CAST")
class TrainersViewModelFactory(
    private val application: Application,
    private val userId: Long,
    private val trainerRepository: TrainerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TrainersViewModel(application, userId, trainerRepository) as T
}