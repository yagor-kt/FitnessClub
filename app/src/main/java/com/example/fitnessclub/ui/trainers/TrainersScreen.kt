package com.example.fitnessclub.ui.trainers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.Trainer
import com.example.fitnessclub.ui.components.ClubButton
import com.example.fitnessclub.ui.components.ClubCard
import com.example.fitnessclub.ui.components.ClubOutlinedButton
import com.example.fitnessclub.ui.components.LoadingIndicator

@Composable
fun TrainersScreen(viewModel: TrainersViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(current.data.trainers, key = Trainer::id) { trainer ->
                            TrainerCard(
                                trainer = trainer,
                                isBooked = trainer.name in current.data.bookedTrainerNames,
                                onBook = { viewModel.bookPersonal(trainer) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainerCard(
    trainer: Trainer,
    isBooked: Boolean,
    onBook: () -> Unit
) {
    ClubCard {
        AsyncImage(
            model = trainer.photoUrl,
            contentDescription = trainer.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(text = trainer.name, style = MaterialTheme.typography.titleLarge)
        Text(text = stringResource(R.string.specialization_label, trainer.specialization))
        Text(text = stringResource(R.string.experience_label, trainer.experienceYears))
        Spacer(Modifier.height(12.dp))

        if (isBooked) {
            Text(
                text = stringResource(R.string.already_booked),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            ClubButton(
                text = stringResource(R.string.book_personal_workout),
                onClick = onBook,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}