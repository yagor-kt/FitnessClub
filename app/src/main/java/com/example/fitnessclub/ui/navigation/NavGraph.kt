package com.example.fitnessclub.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fitnessclub.R
import com.example.fitnessclub.data.repository.BookingRepository
import com.example.fitnessclub.data.repository.TrainerRepository
import com.example.fitnessclub.data.repository.UserRepository
import com.example.fitnessclub.data.repository.WorkoutRepository
import com.example.fitnessclub.data.session.SessionManager

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
    // Репозитории и менеджер сессии передаются в граф для подключения экранов на следующих шагах.
    @Suppress("UNUSED_VARIABLE")
    val dependencies = listOf(
        userRepository,
        workoutRepository,
        bookingRepository,
        trainerRepository,
        sessionManager
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            PlaceholderScreen(title = stringResource(R.string.login_title))
        }
        composable(Screen.Registration.route) {
            PlaceholderScreen(title = stringResource(R.string.register_title))
        }
        composable(Screen.Dashboard.route) {
            PlaceholderScreen(title = stringResource(R.string.app_name))
        }
        composable(Screen.Schedule.route) {
            PlaceholderScreen(title = stringResource(R.string.schedule_title))
        }
        composable(Screen.Bookings.route) {
            PlaceholderScreen(title = stringResource(R.string.my_bookings_title))
        }
        composable(Screen.Profile.route) {
            PlaceholderScreen(title = stringResource(R.string.profile_title))
        }
        composable(Screen.Trainers.route) {
            PlaceholderScreen(title = stringResource(R.string.trainers_title))
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title)
    }
}