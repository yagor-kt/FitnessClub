package com.example.fitnessclub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fitnessclub.data.repository.BookingRepository
import com.example.fitnessclub.data.repository.TrainerRepository
import com.example.fitnessclub.data.repository.UserRepository
import com.example.fitnessclub.data.repository.WorkoutRepository
import com.example.fitnessclub.data.session.SessionManager
import com.example.fitnessclub.ui.DashboardViewModelFactory
import com.example.fitnessclub.ui.LoginViewModelFactory
import com.example.fitnessclub.ui.RegistrationViewModelFactory
import com.example.fitnessclub.ui.ScheduleViewModelFactory
import com.example.fitnessclub.ui.auth.LoginScreen
import com.example.fitnessclub.ui.auth.LoginViewModel
import com.example.fitnessclub.ui.auth.RegistrationScreen
import com.example.fitnessclub.ui.auth.RegistrationViewModel
import com.example.fitnessclub.ui.dashboard.DashboardScreen
import com.example.fitnessclub.ui.dashboard.DashboardViewModel
import com.example.fitnessclub.ui.schedule.ScheduleScreen
import com.example.fitnessclub.ui.schedule.ScheduleViewModel
import kotlinx.coroutines.flow.first

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    userRepository: UserRepository,
    workoutRepository: WorkoutRepository,
    bookingRepository: BookingRepository,
    trainerRepository: TrainerRepository,
    sessionManager: SessionManager
) {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext
            as android.app.Application

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val vm: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(application, userRepository, sessionManager)
            )
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Registration.route)
                }
            )
        }

        composable(Screen.Registration.route) {
            val vm: RegistrationViewModel = viewModel(
                factory = RegistrationViewModelFactory(
                    application,
                    userRepository,
                    sessionManager
                )
            )
            RegistrationScreen(
                viewModel = vm,
                onRegistrationSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            val userId = rememberSessionUserId(sessionManager)
            if (userId != null) {
                val vm: DashboardViewModel = viewModel(
                    key = "dashboard_$userId",
                    factory = DashboardViewModelFactory(
                        application,
                        userId,
                        userRepository
                    )
                )
                DashboardScreen(
                    viewModel = vm,
                    onSchedule = { navController.navigate(Screen.Schedule.route) },
                    onBookings = { navController.navigate(Screen.Bookings.route) },
                    onProfile = { navController.navigate(Screen.Profile.route) },
                    onTrainers = { navController.navigate(Screen.Trainers.route) }
                )
            }
        }

        composable(Screen.Schedule.route) {
            val userId = rememberSessionUserId(sessionManager)
            if (userId != null) {
                val vm: ScheduleViewModel = viewModel(
                    key = "schedule_$userId",
                    factory = ScheduleViewModelFactory(
                        application,
                        userId,
                        workoutRepository,
                        bookingRepository
                    )
                )
                ScheduleScreen(viewModel = vm)
            }
        }

        // Экраны будут подключены на шаге 5.
        composable(Screen.Bookings.route) {}
        composable(Screen.Profile.route) {}
        composable(Screen.Trainers.route) {}
    }
}

@Composable
private fun rememberSessionUserId(sessionManager: SessionManager): Long? {
    val userIdState = androidx.compose.runtime.produceState<Long?>(
        initialValue = null,
        sessionManager
    ) {
        value = sessionManager.userId.first()
    }
    return userIdState.value
}