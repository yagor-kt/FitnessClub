package com.example.fitnessclub.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.Workout
import com.example.fitnessclub.data.repository.BookingRepository
import com.example.fitnessclub.data.repository.BookingResult
import com.example.fitnessclub.data.repository.WorkoutRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ScheduleFilter {
    TODAY,
    TOMORROW,
    WEEK
}

data class ScheduleData(
    val workouts: List<Workout>,
    val bookedWorkoutIds: Set<Long>,
    val filter: ScheduleFilter,
    val isRefreshing: Boolean
)

sealed interface ScheduleUiState {
    data object Loading : ScheduleUiState
    data class Success(val data: ScheduleData) : ScheduleUiState
    data class Error(val message: String) : ScheduleUiState
}

class ScheduleViewModel(
    application: Application,
    private val userId: Long,
    private val workoutRepository: WorkoutRepository,
    private val bookingRepository: BookingRepository
) : AndroidViewModel(application) {
    private val filter = MutableStateFlow(ScheduleFilter.TODAY)
    private val refreshTick = MutableStateFlow(0)
    private val isRefreshing = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                workoutRepository.getAll(),
                workoutRepository.observeBookedWorkoutIds(userId),
                filter,
                refreshTick,
                isRefreshing
            ) { workouts, bookedIds, selectedFilter, _, refreshing ->
                val now = System.currentTimeMillis()
                val (start, end) = dateRange(selectedFilter, now)

                ScheduleUiState.Success(
                    ScheduleData(
                        workouts = workouts.filter { it.dateTime in start..end },
                        bookedWorkoutIds = bookedIds.toSet(),
                        filter = selectedFilter,
                        isRefreshing = refreshing
                    )
                )
            }
                .catch {
                    _uiState.value = ScheduleUiState.Error(
                        getApplication<Application>().getString(R.string.error_generic)
                    )
                }
                .collect { _uiState.value = it }
        }
    }

    fun selectFilter(value: ScheduleFilter) {
        filter.value = value
    }

    fun refresh() {
        viewModelScope.launch {
            if (isRefreshing.value) return@launch

            isRefreshing.value = true
            refreshTick.value++
            delay(600)
            isRefreshing.value = false
        }
    }

    fun retry() {
        refresh()
    }

    fun book(workoutId: Long) {
        viewModelScope.launch {
            try {
                val messageId = when (bookingRepository.book(userId, workoutId)) {
                    BookingResult.BOOKED -> R.string.booking_success
                    BookingResult.FULL -> R.string.no_places
                    BookingResult.ALREADY_BOOKED -> R.string.already_booked
                    BookingResult.NOT_FOUND -> R.string.booking_error
                }
                _messages.emit(getApplication<Application>().getString(messageId))
            } catch (_: Exception) {
                _messages.emit(
                    getApplication<Application>().getString(R.string.booking_error)
                )
            }
        }
    }

    private fun dateRange(selectedFilter: ScheduleFilter, now: Long): Pair<Long, Long> {
        val todayStart = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when (selectedFilter) {
            ScheduleFilter.TODAY -> {
                val start = todayStart.timeInMillis
                todayStart.add(Calendar.DAY_OF_YEAR, 1)
                start to todayStart.timeInMillis - 1
            }

            ScheduleFilter.TOMORROW -> {
                todayStart.add(Calendar.DAY_OF_YEAR, 1)
                val start = todayStart.timeInMillis
                todayStart.add(Calendar.DAY_OF_YEAR, 1)
                start to todayStart.timeInMillis - 1
            }

            ScheduleFilter.WEEK -> now to now + 7L * 24 * 60 * 60 * 1000
        }
    }
}