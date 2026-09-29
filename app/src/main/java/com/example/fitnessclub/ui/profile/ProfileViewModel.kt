package com.example.fitnessclub.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.User
import com.example.fitnessclub.data.repository.BookingRepository
import com.example.fitnessclub.data.repository.UserRepository
import com.example.fitnessclub.data.session.SessionManager
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

data class ProfileData(
    val user: User,
    val completedThisMonth: Int,
    val totalCompleted: Int
)

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val data: ProfileData) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    application: Application,
    private val userId: Long,
    private val userRepository: UserRepository,
    private val bookingRepository: BookingRepository,
    private val sessionManager: SessionManager
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    private var observeJob: Job? = null

    init {
        observeProfile()
    }

    fun retry() = observeProfile()

    private fun observeProfile() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val now = System.currentTimeMillis()
            val monthStart = DateFormatter.monthStart()

            combine(
                userRepository.observeById(userId),
                bookingRepository.countCompletedThisMonth(userId, monthStart, now),
                bookingRepository.countAllCompleted(userId, now)
            ) { user, completedThisMonth, totalCompleted ->
                if (user == null) {
                    ProfileUiState.Error(
                        getApplication<Application>().getString(R.string.error_generic)
                    )
                } else {
                    ProfileUiState.Success(
                        ProfileData(
                            user = user,
                            completedThisMonth = completedThisMonth,
                            totalCompleted = totalCompleted
                        )
                    )
                }
            }
                .catch {
                    _uiState.value = ProfileUiState.Error(
                        getApplication<Application>().getString(R.string.error_generic)
                    )
                }
                .collect { _uiState.value = it }
        }
    }

    fun updateProfile(
        name: String,
        weightText: String,
        goal: String,
        goalVisits: Int
    ) {
        val user = (_uiState.value as? ProfileUiState.Success)?.data?.user ?: return
        val parsedWeight = weightText.trim().takeIf(String::isNotEmpty)?.toFloatOrNull()

        if (name.isBlank() || (weightText.isNotBlank() && parsedWeight == null)) {
            emitMessage(R.string.profile_update_error)
            return
        }

        viewModelScope.launch {
            try {
                userRepository.update(
                    user.copy(
                        name = name.trim(),
                        weight = parsedWeight,
                        goal = goal.trim().takeIf(String::isNotEmpty),
                        goalVisits = goalVisits.coerceIn(5, 30)
                    )
                )
                emitMessage(R.string.profile_update_success)
            } catch (_: Exception) {
                emitMessage(R.string.profile_update_error)
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                sessionManager.clear()
                onLoggedOut()
            } catch (_: Exception) {
                emitMessage(R.string.error_generic)
            }
        }
    }

    private fun emitMessage(messageId: Int) {
        viewModelScope.launch {
            _messages.emit(getApplication<Application>().getString(messageId))
        }
    }
}