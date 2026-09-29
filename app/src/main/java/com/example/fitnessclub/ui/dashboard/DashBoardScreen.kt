package com.example.fitnessclub.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.example.fitnessclub.util.WorkoutType

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSchedule: () -> Unit,
    onBookings: () -> Unit,
    onProfile: () -> Unit,
    onTrainers: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        if (state is DashboardUiState.Error) {
            snackbarHostState.showSnackbar((state as DashboardUiState.Error).message)
        }
    }

    when (val current = state) {
        DashboardUiState.Loading -> LoadingIndicator()

        is DashboardUiState.Error -> Scaffold(
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

        is DashboardUiState.Success -> {
            val data = current.data
            val now = System.currentTimeMillis()
            val subscriptionActive = data.user.subscriptionEnd > now
            val goal = data.user.goalVisits.coerceAtLeast(1)
            val progress = (data.completedThisMonth.toFloat() / goal).coerceIn(0f, 1f)

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.dashboard_time_greeting,
                            DateFormatter.greetingByTime(),
                            data.user.name
                        ),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    AnimatedVisibility(visible = true) {
                        val nearest = data.nearestBooking
                        if (nearest == null) {
                            ClubCard {
                                Text(
                                    text = stringResource(R.string.no_nearest_workout),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = onSchedule,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(stringResource(R.string.to_schedule))
                                }
                            }
                        } else {
                            NearestWorkoutCard(
                                workout = nearest.workout
                            )
                        }
                    }

                    ClubCard {
                        Text(
                            text = stringResource(R.string.monthly_activity),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.monthly_progress,
                                    data.completedThisMonth,
                                    data.user.goalVisits
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (data.completedThisMonth >= data.user.goalVisits) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = stringResource(R.string.goal_reached),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    ClubCard {
                        Text(
                            text = if (data.streakWeeks == 0) {
                                stringResource(R.string.streak_start)
                            } else {
                                stringResource(R.string.streak_weeks, data.streakWeeks)
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    ClubCard {
                        Text(
                            text = stringResource(R.string.subscription_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(text = stringResource(R.string.subscription_type))
                        Text(
                            text = stringResource(
                                R.string.subscription_end_date,
                                DateFormatter.formatDate(data.user.subscriptionEnd)
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    stringResource(
                                        if (subscriptionActive) R.string.subscription_active
                                        else R.string.subscription_expired
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (subscriptionActive) {
                                        Icons.Filled.CheckCircle
                                    } else {
                                        Icons.Filled.Error
                                    },
                                    contentDescription = null
                                )
                            },
                            colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                labelColor = if (subscriptionActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                leadingIconContentColor = if (subscriptionActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        )
                    }

                    NavigationGrid(
                        onSchedule = onSchedule,
                        onBookings = onBookings,
                        onProfile = onProfile,
                        onTrainers = onTrainers
                    )
                }
            }
        }
    }
}

@Composable
private fun NearestWorkoutCard(workout: Workout) {
    ClubCard {
        Text(
            text = stringResource(R.string.nearest_workout_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        TypeChip(type = workout.type)
        Spacer(Modifier.height(8.dp))
        Text(text = workout.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = DateFormatter.relativeTime(workout.dateTime),
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = stringResource(R.string.trainer_label, workout.trainerName))
    }
}

@Composable
private fun TypeChip(type: String) {
    val color = WorkoutType.colorForType(type)
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

@Composable
private fun NavigationGrid(
    onSchedule: () -> Unit,
    onBookings: () -> Unit,
    onProfile: () -> Unit,
    onTrainers: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NavigationButton(
                text = stringResource(R.string.schedule),
                icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                onClick = onSchedule,
                modifier = Modifier.weight(1f)
            )
            NavigationButton(
                text = stringResource(R.string.my_bookings),
                icon = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
                onClick = onBookings,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NavigationButton(
                text = stringResource(R.string.profile),
                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                onClick = onProfile,
                modifier = Modifier.weight(1f)
            )
            NavigationButton(
                text = stringResource(R.string.trainers),
                icon = { Icon(Icons.Filled.FitnessCenter, contentDescription = null) },
                onClick = onTrainers,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavigationButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.8f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon()
            Text(text)
        }
    }
}