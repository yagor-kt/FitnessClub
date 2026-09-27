package com.example.fitnessclub.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.User
import com.example.fitnessclub.data.repository.UserRepository
import com.example.fitnessclub.data.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val user: User) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    application: Application,
    private val userId: Long,
    private val userRepository: UserRepository,
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
            userRepository.observeById(userId)
                .catch {
                    _uiState.value = ProfileUiState.Error(
                        getApplication<Application>().getString(R.string.error_generic)
                    )
                }
                .collect { user ->
                    _uiState.value = if (user != null) {
                        ProfileUiState.Success(user)
                    } else {
                        ProfileUiState.Error(
                            getApplication<Application>().getString(R.string.error_generic)
                        )
                    }
                }
        }
    }

    fun updateProfile(name: String, weightText: String, goal: String) {
        val currentUser = (_uiState.value as? ProfileUiState.Success)?.user ?: return
        val parsedWeight = weightText.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()

        if (name.isBlank() || (weightText.isNotBlank() && parsedWeight == null)) {
            viewModelScope.launch {
                _messages.emit(
                    getApplication<Application>().getString(R.string.profile_update_error)
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                userRepository.update(
                    currentUser.copy(
                        name = name.trim(),
                        weight = parsedWeight,
                        goal = goal.trim().takeIf { it.isNotEmpty() }
                    )
                )
                _messages.emit(
                    getApplication<Application>().getString(R.string.profile_update_success)
                )
            } catch (_: Exception) {
                _messages.emit(
                    getApplication<Application>().getString(R.string.profile_update_error)
                )
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                sessionManager.clear()
                onLoggedOut()
            } catch (_: Exception) {
                _messages.emit(
                    getApplication<Application>().getString(R.string.error_generic)
                )
            }
        }
    }
}