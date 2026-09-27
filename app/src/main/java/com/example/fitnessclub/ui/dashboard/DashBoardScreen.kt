package com.example.fitnessclub.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.fitnessclub.ui.components.ClubButton
import com.example.fitnessclub.ui.components.ClubCard
import com.example.fitnessclub.ui.components.ClubOutlinedButton
import com.example.fitnessclub.ui.components.LoadingIndicator
import com.example.fitnessclub.util.DateFormatter

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
            val subscriptionActive = data.user.subscriptionEnd > System.currentTimeMillis()

            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_greeting, data.user.name),
                        style = MaterialTheme.typography.headlineMedium
                    )

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
                        Text(
                            text = stringResource(
                                if (subscriptionActive) R.string.subscription_active
                                else R.string.subscription_expired
                            ),
                            color = if (subscriptionActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }

                    ClubCard {
                        Text(
                            text = stringResource(
                                R.string.workouts_this_month,
                                data.workoutsThisMonth
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    ClubButton(
                        text = stringResource(R.string.schedule),
                        onClick = onSchedule,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ClubButton(
                        text = stringResource(R.string.my_bookings),
                        onClick = onBookings,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ClubButton(
                        text = stringResource(R.string.profile),
                        onClick = onProfile,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ClubButton(
                        text = stringResource(R.string.trainers),
                        onClick = onTrainers,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}