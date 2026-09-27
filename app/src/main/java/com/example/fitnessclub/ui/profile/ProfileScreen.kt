package com.example.fitnessclub.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.User
import com.example.fitnessclub.ui.components.ClubButton
import com.example.fitnessclub.ui.components.ClubCard
import com.example.fitnessclub.ui.components.ClubOutlinedButton
import com.example.fitnessclub.ui.components.LoadingIndicator
import com.example.fitnessclub.util.DateFormatter

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    when (val current = state) {
        ProfileUiState.Loading -> LoadingIndicator()

        is ProfileUiState.Error -> {
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

        is ProfileUiState.Success -> {
            val user = current.user
            val isSubscriptionActive = user.subscriptionEnd > System.currentTimeMillis()

            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    AsyncImage(
                        model = "https://i.pravatar.cc/300?u=${user.email}",
                        contentDescription = stringResource(R.string.profile_title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(CircleShape)
                    )

                    ClubCard {
                        Text(text = user.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(text = user.email)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.weight
                            ) + ": " + (user.weight?.toString() ?: "—")
                        )
                        Text(
                            text = stringResource(R.string.goal) + ": " + (user.goal ?: "—")
                        )
                    }

                    ClubCard {
                        Text(
                            text = stringResource(R.string.subscription_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(text = stringResource(R.string.subscription_type))
                        Text(
                            text = stringResource(
                                R.string.subscription_end_date,
                                DateFormatter.formatDate(user.subscriptionEnd)
                            )
                        )
                        Text(
                            text = stringResource(
                                if (isSubscriptionActive) R.string.subscription_active
                                else R.string.subscription_expired
                            ),
                            color = if (isSubscriptionActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }

                    ClubButton(
                        text = stringResource(R.string.edit_profile),
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ClubOutlinedButton(
                        text = stringResource(R.string.logout),
                        onClick = { viewModel.logout(onLoggedOut) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (showEditDialog) {
                EditProfileDialog(
                    user = user,
                    onDismiss = { showEditDialog = false },
                    onSave = { name, weight, goal ->
                        viewModel.updateProfile(name, weight, goal)
                        showEditDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (name: String, weight: String, goal: String) -> Unit
) {
    var name by remember(user.id) { mutableStateOf(user.name) }
    var weight by remember(user.id) {
        mutableStateOf(user.weight?.toString().orEmpty())
    }
    var goal by remember(user.id) { mutableStateOf(user.goal.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.weight)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text(stringResource(R.string.goal)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, weight, goal) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}