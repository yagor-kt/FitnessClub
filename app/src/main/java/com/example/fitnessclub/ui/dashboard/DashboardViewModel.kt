package com.example.fitnessclub.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.User
import com.example.fitnessclub.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardData(
    val user: User,
    val workoutsThisMonth: Int
)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val data: DashboardData) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class DashboardViewModel(
    application: Application,
    private val userId: Long,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            val now = System.currentTimeMillis()
            val monthStart = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            try {
                combine(
                    userRepository.observeById(userId),
                    userRepository.monthlyWorkoutCount(userId, monthStart, now)
                ) { user, count ->
                    user?.let { DashboardUiState.Success(DashboardData(it, count)) }
                        ?: DashboardUiState.Error(
                            getApplication<Application>().getString(R.string.error_generic)
                        )
                }.collect { _uiState.value = it }
            } catch (_: Exception) {
                _uiState.value = DashboardUiState.Error(
                    getApplication<Application>().getString(R.string.error_generic)
                )
            }
        }
    }
}