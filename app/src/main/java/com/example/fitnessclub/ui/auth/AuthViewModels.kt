package com.example.fitnessclub.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessclub.R
import com.example.fitnessclub.data.repository.UserRepository
import com.example.fitnessclub.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Loading : LoginUiState
    data class Success(val signedInUserId: Long? = null) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

sealed interface RegistrationUiState {
    data object Loading : RegistrationUiState
    data class Success(val signedInUserId: Long? = null) : RegistrationUiState
    data class Error(val message: String) : RegistrationUiState
}

class LoginViewModel(
    application: Application,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Success())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val user = userRepository.getByEmail(email.trim())
                if (user == null || user.password != password) {
                    _uiState.value = LoginUiState.Error(
                        getApplication<Application>().getString(R.string.login_error)
                    )
                    return@launch
                }

                sessionManager.saveUserId(user.id)
                _uiState.value = LoginUiState.Success(user.id)
            } catch (_: Exception) {
                _uiState.value = LoginUiState.Error(
                    getApplication<Application>().getString(R.string.error_generic)
                )
            }
        }
    }

    fun retry() {
        _uiState.value = LoginUiState.Success()
    }
}

class RegistrationViewModel(
    application: Application,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : AndroidViewModel(application) {
    private val _uiState =
        MutableStateFlow<RegistrationUiState>(RegistrationUiState.Success())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, name: String) {
        val app = getApplication<Application>()
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _uiState.value = RegistrationUiState.Error(
                app.getString(R.string.validation_required)
            )
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = RegistrationUiState.Error(
                app.getString(R.string.validation_email)
            )
            return
        }
        if (password.length < 4) {
            _uiState.value = RegistrationUiState.Error(
                app.getString(R.string.validation_password)
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = RegistrationUiState.Loading
            try {
                val userId = userRepository.register(
                    email = email.trim(),
                    password = password,
                    name = name.trim()
                )
                sessionManager.saveUserId(userId)
                _uiState.value = RegistrationUiState.Success(userId)
            } catch (_: Exception) {
                _uiState.value = RegistrationUiState.Error(
                    app.getString(R.string.email_already_exists)
                )
            }
        }
    }

    fun retry() {
        _uiState.value = RegistrationUiState.Success()
    }
}