package com.example.fitnessclub.ui.bookings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.relation.BookingWithWorkout
import com.example.fitnessclub.data.repository.BookingRepository
import com.example.fitnessclub.util.DateFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class BookingsData(
    val active: List<BookingWithWorkout>,
    val history: List<BookingWithWorkout>,
    val totalCompleted: Int,
    val completedThisMonth: Int
)

sealed interface BookingsUiState {
    data object Loading : BookingsUiState
    data class Success(val data: BookingsData) : BookingsUiState
    data class Error(val message: String) : BookingsUiState
}

class BookingsViewModel(
    application: Application,
    private val userId: Long,
    private val bookingRepository: BookingRepository
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<BookingsUiState>(BookingsUiState.Loading)
    val uiState: StateFlow<BookingsUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    private var observeJob: Job? = null

    init {
        observeBookings()
    }

    fun retry() {
        observeBookings()
    }

    private fun observeBookings() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = BookingsUiState.Loading

            val now = System.currentTimeMillis()
            val monthStart = DateFormatter.monthStart()

            combine(
                bookingRepository.getActiveBookings(userId),
                bookingRepository.getBookingHistory(userId),
                bookingRepository.countAllCompleted(userId, now),
                bookingRepository.countCompletedThisMonth(userId, monthStart, now)
            ) { active, history, total, thisMonth ->
                BookingsUiState.Success(
                    BookingsData(
                        active = active,
                        history = history,
                        totalCompleted = total,
                        completedThisMonth = thisMonth
                    )
                )
            }
                .catch {
                    _uiState.value = BookingsUiState.Error(
                        getApplication<Application>().getString(R.string.error_generic)
                    )
                }
                .collect { _uiState.value = it }
        }
    }

    fun cancel(bookingId: Long) {
        viewModelScope.launch {
            try {
                val messageId = if (bookingRepository.cancel(bookingId)) {
                    R.string.cancel_booking_success
                } else {
                    R.string.cancel_booking_error
                }
                _messages.emit(getApplication<Application>().getString(messageId))
            } catch (_: Exception) {
                _messages.emit(
                    getApplication<Application>().getString(R.string.cancel_booking_error)
                )
            }
        }
    }
}