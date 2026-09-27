package com.example.fitnessclub.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Registration : Screen("registration")
    data object Dashboard : Screen("dashboard")
    data object Schedule : Screen("schedule")
    data object Bookings : Screen("bookings")
    data object Profile : Screen("profile")
    data object Trainers : Screen("trainers")
}