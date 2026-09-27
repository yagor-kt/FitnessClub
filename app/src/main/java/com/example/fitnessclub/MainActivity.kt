package com.example.fitnessclub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.fitnessclub.ui.components.LoadingIndicator
import com.example.fitnessclub.ui.navigation.NavGraph
import com.example.fitnessclub.ui.navigation.Screen
import com.example.fitnessclub.ui.theme.FitnessClubTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as App
        setContent {
            FitnessClubTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    startDestination = try {
                        if (app.sessionManager.userId.first() != null) {
                            Screen.Dashboard.route
                        } else {
                            Screen.Login.route
                        }
                    } catch (_: Exception) {
                        Screen.Login.route
                    }
                }

                val destination = startDestination
                if (destination == null) {
                    LoadingIndicator()
                } else {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = destination,
                        userRepository = app.userRepository,
                        workoutRepository = app.workoutRepository,
                        bookingRepository = app.bookingRepository,
                        trainerRepository = app.trainerRepository,
                        sessionManager = app.sessionManager
                    )
                }
            }
        }
    }
}