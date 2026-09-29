package com.example.fitnessclub.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.Workout
import com.example.fitnessclub.ui.components.ClubButton
import com.example.fitnessclub.ui.components.ClubCard
import com.example.fitnessclub.ui.components.ClubOutlinedButton
import com.example.fitnessclub.ui.components.LoadingIndicator
import com.example.fitnessclub.util.DateFormatter
import com.example.fitnessclub.util.WorkoutType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    when (val current = state) {
        ScheduleUiState.Loading -> LoadingIndicator()

        is ScheduleUiState.Error -> {
            LaunchedEffect(current.message) {
                snackbarHostState.showSnackbar(current.message)
            }
            Scaffold(
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
        }

        is ScheduleUiState.Success -> {
            val data = current.data
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
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

                    PullToRefreshBox(
                        isRefreshing = data.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (data.workouts.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EventBusy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.no_workouts_for_day),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(
                                    items = data.workouts,
                                    key = { _, workout -> workout.id }
                                ) { index, workout ->
                                    this@Column.AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(
                                            animationSpec = tween(
                                                durationMillis = 250,
                                                delayMillis = index * 45
                                            )
                                        ) + expandVertically(
                                            animationSpec = tween(
                                                durationMillis = 250,
                                                delayMillis = index * 45
                                            )
                                        )
                                    ) {
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
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    isBooked: Boolean,
    onBook: () -> Unit
) {
    val now = System.currentTimeMillis()
    val placesBooked = workout.currentBookings.coerceIn(0, workout.maxCapacity)
    val fillProgress = if (workout.maxCapacity > 0) {
        placesBooked.toFloat() / workout.maxCapacity
    } else {
        0f
    }
    val startsSoon = workout.dateTime >= now &&
            workout.dateTime - now < 2L * 60 * 60 * 1000
    val typeColor = WorkoutType.colorForType(workout.type)

    ClubCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeChip(type = workout.type, color = typeColor)
            if (startsSoon) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = stringResource(R.string.workout_soon),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = workout.title,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = DateFormatter.formatDateTime(workout.dateTime),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = stringResource(R.string.trainer_label, workout.trainerName))
        Text(text = stringResource(R.string.hall_label, workout.hall))

        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.workout_capacity,
                placesBooked,
                workout.maxCapacity
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        androidx.compose.material3.LinearProgressIndicator(
            progress = { fillProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            color = typeColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(Modifier.height(12.dp))
        when {
            isBooked -> Text(
                text = stringResource(R.string.already_booked),
                color = MaterialTheme.colorScheme.primary
            )

            workout.maxCapacity - workout.currentBookings <= 0 -> ClubButton(
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

@Composable
private fun TypeChip(type: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        contentColor = color,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = type,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}