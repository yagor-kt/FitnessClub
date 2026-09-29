package com.example.fitnessclub.ui.trainers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.Trainer
import com.example.fitnessclub.data.repository.PersonalBookingResult
import com.example.fitnessclub.data.repository.TrainerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class TrainerFilter {
    ALL,
    YOGA,
    STRENGTH,
    CARDIO
}

data class TrainersData(
    val trainers: List<Trainer>,
    val bookedTrainerNames: Set<String>,
    val filter: TrainerFilter
)

sealed interface TrainersUiState {
    data object Loading : TrainersUiState
    data class Success(val data: TrainersData) : TrainersUiState
    data class Error(val message: String) : TrainersUiState
}

class TrainersViewModel(
    application: Application,
    private val userId: Long,
    private val trainerRepository: TrainerRepository
) : AndroidViewModel(application) {
    private val selectedFilter = MutableStateFlow(TrainerFilter.ALL)

    private val _uiState = MutableStateFlow<TrainersUiState>(TrainersUiState.Loading)
    val uiState: StateFlow<TrainersUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    private var observeJob: Job? = null

    init {
        observeTrainers()
    }

    fun retry() = observeTrainers()

    fun selectFilter(filter: TrainerFilter) {
        selectedFilter.value = filter
    }

    private fun observeTrainers() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = TrainersUiState.Loading
            combine(
                trainerRepository.getAll(),
                trainerRepository.observePersonalTrainerNames(userId),
                selectedFilter
            ) { trainers, bookedNames, filter ->
                val filteredTrainers = trainers.filter { trainer ->
                    when (filter) {
                        TrainerFilter.ALL -> true
                        TrainerFilter.YOGA ->
                            trainer.specialization.contains("йог", ignoreCase = true) ||
                                    trainer.specialization.contains("растяж", ignoreCase = true)
                        TrainerFilter.STRENGTH ->
                            trainer.specialization.contains("сил", ignoreCase = true) ||
                                    trainer.specialization.contains("кроссфит", ignoreCase = true)
                        TrainerFilter.CARDIO ->
                            trainer.specialization.contains("кардио", ignoreCase = true) ||
                                    trainer.specialization.contains("функцион", ignoreCase = true)
                    }
                }

                TrainersUiState.Success(
                    TrainersData(
                        trainers = filteredTrainers,
                        bookedTrainerNames = bookedNames.toSet(),
                        filter = filter
                    )
                )
            }
                .catch {
                    _uiState.value = TrainersUiState.Error(
                        getApplication<Application>().getString(R.string.error_generic)
                    )
                }
                .collect { _uiState.value = it }
        }
    }

    fun bookPersonal(trainer: Trainer) {
        viewModelScope.launch {
            try {
                val messageId = when (
                    trainerRepository.bookPersonal(userId, trainer)
                ) {
                    PersonalBookingResult.BOOKED -> R.string.personal_booking_success
                    PersonalBookingResult.ALREADY_BOOKED -> R.string.already_booked
                }
                _messages.emit(getApplication<Application>().getString(messageId))
            } catch (_: Exception) {
                _messages.emit(
                    getApplication<Application>().getString(
                        R.string.personal_booking_error
                    )
                )
            }
        }
    }
}