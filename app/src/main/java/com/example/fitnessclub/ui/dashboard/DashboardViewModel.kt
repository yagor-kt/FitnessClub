package com.example.fitnessclub.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.User
import com.example.fitnessclub.data.local.relation.BookingWithWorkout
import com.example.fitnessclub.data.repository.BookingRepository
import com.example.fitnessclub.data.repository.UserRepository
import com.example.fitnessclub.util.DateFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.collections.asSequence

data class DashboardData(
    val user: User,
    val nearestBooking: BookingWithWorkout?,
    val completedThisMonth: Int,
    val streakWeeks: Int
)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val data: DashboardData) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class DashboardViewModel(
    application: Application,
    private val userId: Long,
    private val userRepository: UserRepository,
    private val bookingRepository: BookingRepository
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeDashboard()
    }

    fun retry() {
        observeDashboard()
    }

    private fun observeDashboard() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            val now = System.currentTimeMillis()
            val monthStart = DateFormatter.monthStart()

            combine(
                userRepository.observeById(userId),
                bookingRepository.getNearestBooking(userId),
                bookingRepository.countCompletedThisMonth(userId, monthStart, now),
                bookingRepository.getBookingsWithWorkouts(userId)
            ) { user, nearestBooking, completedThisMonth, bookings ->
                if (user == null) {
                    DashboardUiState.Error(
                        getApplication<Application>().getString(R.string.error_generic)
                    )
                } else {
                    DashboardUiState.Success(
                        DashboardData(
                            user = user,
                            nearestBooking = nearestBooking,
                            completedThisMonth = completedThisMonth,
                            streakWeeks = calculateCurrentStreak(bookings, now)
                        )
                    )
                }
            }
                .catch {
                    _uiState.value = DashboardUiState.Error(
                        getApplication<Application>().getString(R.string.error_generic)
                    )
                }
                .collect { _uiState.value = it }
        }
    }

    private fun calculateCurrentStreak(
        bookings: List<BookingWithWorkout>,
        now: Long
    ): Int {
        val completedWeekStarts = bookings
            .asSequence()
            .map { it.workout.dateTime }
            .filter { it <= now }
            .map(::weekStart)
            .toSet()

        var week = weekStart(now)
        if (week !in completedWeekStarts) return 0

        var streak = 0
        while (week in completedWeekStarts) {
            streak++
            week -= WEEK_MILLIS
        }
        return streak
    }

    private fun weekStart(timestamp: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = timestamp
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private companion object {
        const val WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000
    }
}