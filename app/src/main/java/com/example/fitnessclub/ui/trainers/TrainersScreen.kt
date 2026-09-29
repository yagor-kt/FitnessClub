package com.example.fitnessclub.ui.trainers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.Trainer
import com.example.fitnessclub.ui.components.ClubButton
import com.example.fitnessclub.ui.components.ClubCard
import com.example.fitnessclub.ui.components.ClubOutlinedButton
import com.example.fitnessclub.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainersScreen(viewModel: TrainersViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTrainer by remember { mutableStateOf<Trainer?>(null) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    when (val current = state) {
        TrainersUiState.Loading -> LoadingIndicator()

        is TrainersUiState.Error -> {
            LaunchedEffect(current.message) {
                snackbarHostState.showSnackbar(current.message)
            }
            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
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

        is TrainersUiState.Success -> {
            val data = current.data
            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.trainers_title),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TrainerFilterChip(
                            label = R.string.filter_all,
                            selected = data.filter == TrainerFilter.ALL,
                            onClick = { viewModel.selectFilter(TrainerFilter.ALL) }
                        )
                        TrainerFilterChip(
                            label = R.string.filter_yoga,
                            selected = data.filter == TrainerFilter.YOGA,
                            onClick = { viewModel.selectFilter(TrainerFilter.YOGA) }
                        )
                        TrainerFilterChip(
                            label = R.string.filter_strength,
                            selected = data.filter == TrainerFilter.STRENGTH,
                            onClick = { viewModel.selectFilter(TrainerFilter.STRENGTH) }
                        )
                        TrainerFilterChip(
                            label = R.string.filter_cardio,
                            selected = data.filter == TrainerFilter.CARDIO,
                            onClick = { viewModel.selectFilter(TrainerFilter.CARDIO) }
                        )
                    }

                    if (data.trainers.isEmpty()) {
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
                            items(data.trainers, key = Trainer::id) { trainer ->
                                TrainerCard(
                                    trainer = trainer,
                                    isBooked = trainer.name in data.bookedTrainerNames,
                                    onClick = { selectedTrainer = trainer }
                                )
                            }
                        }
                    }
                }
            }

            selectedTrainer?.let { trainer ->
                val alreadyBooked = trainer.name in data.bookedTrainerNames
                ModalBottomSheet(
                    onDismissRequest = { selectedTrainer = null }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = trainer.photoUrl,
                            contentDescription = trainer.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                        )
                        Text(
                            text = trainer.name,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(
                                R.string.specialization_label,
                                trainer.specialization
                            )
                        )
                        Text(
                            text = stringResource(
                                R.string.experience_label,
                                trainer.experienceYears
                            )
                        )
                        RatingStars(rating = trainer.rating)
                        Text(
                            text = stringResource(
                                R.string.trainer_rating,
                                trainer.rating
                            )
                        )
                        Text(
                            text = trainer.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (alreadyBooked) {
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.already_booked)) }
                            )
                        } else {
                            ClubButton(
                                text = stringResource(R.string.book_personal_workout),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.bookPersonal(trainer)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainerFilterChip(
    label: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(label)) }
    )
}

@Composable
private fun TrainerCard(
    trainer: Trainer,
    isBooked: Boolean,
    onClick: () -> Unit
) {
    ClubCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = trainer.photoUrl,
                contentDescription = trainer.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )
            if (trainer.rating >= 4.8f) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = stringResource(R.string.top_trainer),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(text = trainer.name, style = MaterialTheme.typography.titleLarge)
        Text(text = stringResource(R.string.specialization_label, trainer.specialization))
        Text(text = stringResource(R.string.experience_label, trainer.experienceYears))
        RatingStars(rating = trainer.rating)
        if (isBooked) {
            Spacer(Modifier.height(4.dp))
            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.already_booked)) }
            )
        }
    }
}

@Composable
private fun RatingStars(rating: Float) {
    val safeRating = rating.coerceIn(0f, 5f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (index in 1..5) {
            val icon = when {
                safeRating >= index -> Icons.Filled.Star
                safeRating >= index - 0.5f -> Icons.Filled.StarHalf
                else -> Icons.Filled.Star
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (safeRating >= index - 0.5f) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}