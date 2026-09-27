package com.example.fitnessclub.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.Workout
import com.example.fitnessclub.ui.components.ClubButton
import com.example.fitnessclub.ui.components.ClubCard
import com.example.fitnessclub.ui.components.ClubOutlinedButton
import com.example.fitnessclub.ui.components.LoadingIndicator
import com.example.fitnessclub.util.DateFormatter

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(state) {
        if (state is ScheduleUiState.Error) {
            snackbarHostState.showSnackbar((state as ScheduleUiState.Error).message)
        }
    }

    when (val current = state) {
        ScheduleUiState.Loading -> LoadingIndicator()
        is ScheduleUiState.Error -> Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(current.message)
                Spacer(Modifier.height(12.dp))
                ClubOutlinedButton(
                    text = stringResource(R.string.retry),
                    onClick = viewModel::retry
                )
            }
        }
        is ScheduleUiState.Success -> {
            val data = current.data
            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.schedule_title),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = data.filter == ScheduleFilter.TODAY,
                            onClick = { viewModel.selectFilter(ScheduleFilter.TODAY) },
                            label = { Text(stringResource(R.string.filter_today)) }
                        )
                        FilterChip(
                            selected = data.filter == ScheduleFilter.TOMORROW,
                            onClick = { viewModel.selectFilter(ScheduleFilter.TOMORROW) },
                            label = { Text(stringResource(R.string.filter_tomorrow)) }
                        )
                        FilterChip(
                            selected = data.filter == ScheduleFilter.WEEK,
                            onClick = { viewModel.selectFilter(ScheduleFilter.WEEK) },
                            label = { Text(stringResource(R.string.filter_week)) }
                        )
                    }

                    if (data.workouts.isEmpty()) {
                        Text(
                            text = stringResource(R.string.empty_list),
                            modifier = Modifier.padding(top = 24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(data.workouts, key = Workout::id) { workout ->
                                WorkoutCard(
                                    workout = workout,
                                    isBooked = workout.id in data.bookedWorkoutIds,
                                    onBook = { viewModel.book(workout.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    isBooked: Boolean,
    onBook: () -> Unit
) {
    val placesLeft = workout.maxCapacity - workout.currentBookings

    ClubCard {
        Text(
            text = workout.title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(text = DateFormatter.formatDateTime(workout.dateTime))
        Text(text = stringResource(R.string.trainer_label, workout.trainerName))
        Text(text = stringResource(R.string.hall_label, workout.hall))
        Text(
            text = if (placesLeft > 0) {
                stringResource(R.string.available_places, placesLeft)
            } else {
                stringResource(R.string.no_places)
            }
        )
        Spacer(Modifier.height(12.dp))

        when {
            isBooked -> Text(
                text = stringResource(R.string.already_booked),
                color = MaterialTheme.colorScheme.primary
            )
            placesLeft <= 0 -> ClubButton(
                text = stringResource(R.string.no_places),
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            else -> ClubButton(
                text = stringResource(R.string.book_workout),
                onClick = onBook,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}