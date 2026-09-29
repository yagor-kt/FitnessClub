package com.example.fitnessclub.ui.bookings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.relation.BookingWithWorkout
import com.example.fitnessclub.ui.components.ClubCard
import com.example.fitnessclub.ui.components.ClubOutlinedButton
import com.example.fitnessclub.ui.components.LoadingIndicator
import com.example.fitnessclub.util.DateFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookingsScreen(viewModel: BookingsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var bookingToCancel by remember { mutableStateOf<BookingWithWorkout?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    when (val current = state) {
        BookingsUiState.Loading -> LoadingIndicator()

        is BookingsUiState.Error -> {
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

        is BookingsUiState.Success -> {
            val bookings = if (selectedTab == 0) {
                current.data.active
            } else {
                current.data.history
            }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Text(
                        text = stringResource(R.string.my_bookings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    ClubCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = stringResource(
                                R.string.total_visits,
                                current.data.totalCompleted
                            )
                        )
                        Text(
                            text = stringResource(
                                R.string.month_visits,
                                current.data.completedThisMonth
                            )
                        )
                    }

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(stringResource(R.string.tab_active)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(stringResource(R.string.tab_history)) }
                        )
                    }

                    if (bookings.isEmpty()) {
                        Text(
                            text = stringResource(R.string.empty_list),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (selectedTab == 0) {
                                items(
                                    items = bookings,
                                    key = { it.booking.id }
                                ) { item ->
                                    BookingCard(
                                        item = item,
                                        isActive = true,
                                        onCancel = { bookingToCancel = item }
                                    )
                                }
                            } else {
                                val groups = bookings.groupBy {
                                    monthHeader(it.workout.dateTime)
                                }

                                groups.forEach { (month, monthBookings) ->
                                    item(key = "month_$month") {
                                        Text(
                                            text = month,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 8.dp
                                            )
                                        )
                                    }

                                    items(
                                        items = monthBookings,
                                        key = { it.booking.id }
                                    ) { item ->
                                        BookingCard(
                                            item = item,
                                            isActive = false,
                                            onCancel = {}
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val selectedBooking = bookingToCancel
            if (selectedBooking != null) {
                AlertDialog(
                    onDismissRequest = { bookingToCancel = null },
                    title = {
                        Text(
                            text = stringResource(
                                R.string.cancel_booking_confirm_title,
                                selectedBooking.workout.title
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                                viewModel.cancel(selectedBooking.booking.id)
                                bookingToCancel = null
                            }
                        ) {
                            Text(stringResource(R.string.cancel_booking))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { bookingToCancel = null }) {
                            Text(stringResource(R.string.keep_booking))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BookingCard(
    item: BookingWithWorkout,
    isActive: Boolean,
    onCancel: () -> Unit
) {
    ClubCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = item.workout.title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = DateFormatter.relativeTime(item.workout.dateTime),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = stringResource(R.string.trainer_label, item.workout.trainerName))
        Text(text = stringResource(R.string.hall_label, item.workout.hall))

        if (isActive) {
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel_booking))
            }
        } else {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val attended = item.workout.dateTime < System.currentTimeMillis()
                androidx.compose.material3.Icon(
                    imageVector = if (attended) Icons.Filled.CheckCircle else Icons.Filled.Close,
                    contentDescription = null,
                    tint = if (attended) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = stringResource(
                        if (attended) R.string.booking_attended
                        else R.string.booking_missed
                    ),
                    modifier = Modifier.padding(start = 6.dp),
                    color = if (attended) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

private fun monthHeader(timestamp: Long): String {
    val formatted = SimpleDateFormat("LLLL yyyy", Locale("ru"))
        .format(Date(timestamp))
    return formatted.replaceFirstChar { it.uppercase(Locale("ru")) }
}