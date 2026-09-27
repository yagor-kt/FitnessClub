package com.example.fitnessclub.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessclub.data.repository.BookingRepository
import com.example.fitnessclub.data.repository.UserRepository
import com.example.fitnessclub.data.repository.WorkoutRepository
import com.example.fitnessclub.data.session.SessionManager
import com.example.fitnessclub.ui.auth.LoginViewModel
import com.example.fitnessclub.ui.auth.RegistrationViewModel
import com.example.fitnessclub.ui.dashboard.DashboardViewModel
import com.example.fitnessclub.ui.schedule.ScheduleViewModel

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
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DashboardViewModel(application, userId, userRepository) as T
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