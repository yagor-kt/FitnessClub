package com.example.fitnessclub.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitnessclub.R
import com.example.fitnessclub.data.local.entity.User
import com.example.fitnessclub.ui.components.ClubCard
import com.example.fitnessclub.ui.components.ClubButton
import com.example.fitnessclub.ui.components.ClubOutlinedButton
import com.example.fitnessclub.ui.components.LoadingIndicator
import com.example.fitnessclub.util.DateFormatter
import com.example.fitnessclub.util.UserInitials

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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
            val data = current.data
            val user = data.user
            val isSubscriptionActive = user.subscriptionEnd > System.currentTimeMillis()

            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ProfileProgressAvatar(
                        user = user,
                        completed = data.completedThisMonth
                    )

                    Text(user.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        user.email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ClubCard {
                        Text(
                            text = stringResource(R.string.profile_stats_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(
                                R.string.monthly_progress,
                                data.completedThisMonth,
                                user.goalVisits
                            )
                        )
                        Text(
                            stringResource(R.string.total_visits, data.totalCompleted)
                        )
                        Text(
                            text = stringResource(R.string.weight_value, user.weight?.toString() ?: "—")
                        )
                        Text(
                            text = stringResource(R.string.goal_value, user.goal ?: "—")
                        )
                    }

                    ClubCard {
                        Text(
                            text = stringResource(R.string.achievements_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        AchievementGrid(
                            user = user,
                            totalCompleted = data.totalCompleted
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
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    stringResource(
                                        if (isSubscriptionActive) R.string.subscription_active
                                        else R.string.subscription_expired
                                    )
                                )
                            },
                            leadingIcon = {
                                androidx.compose.material3.Icon(
                                    imageVector = if (isSubscriptionActive) {
                                        Icons.Filled.CheckCircle
                                    } else {
                                        Icons.Filled.Error
                                    },
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    ClubButton(
                        text = stringResource(R.string.edit_profile),
                        onClick = { showEditSheet = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ClubOutlinedButton(
                        text = stringResource(R.string.logout),
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (showEditSheet) {
                EditProfileSheet(
                    user = user,
                    onDismiss = { showEditSheet = false },
                    onSave = { name, weight, goal, goalVisits ->
                        viewModel.updateProfile(name, weight, goal, goalVisits)
                        showEditSheet = false
                    }
                )
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text(stringResource(R.string.logout_confirm_title)) },
                    text = { Text(stringResource(R.string.logout_confirm_message)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutDialog = false
                                viewModel.logout(onLoggedOut)
                            }
                        ) {
                            Text(stringResource(R.string.logout))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text(stringResource(R.string.keep_session))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileProgressAvatar(
    user: User,
    completed: Int
) {
    val progress = (completed.toFloat() / user.goalVisits.coerceAtLeast(1))
        .coerceIn(0f, 1f)
    var imageLoaded by remember(user.email) { mutableStateOf(false) }
    val avatarColor = UserInitials.colorFromEmail(user.email)

    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.size(148.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color(0xFF3B3B43),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
            drawArc(
                color = primary,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        Box(
            modifier = Modifier
                .size(118.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = UserInitials.initials(user.name),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            AsyncImage(
                model = "https://i.pravatar.cc/300?u=${user.email}",
                contentDescription = stringResource(R.string.profile_avatar),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                onSuccess = { imageLoaded = true },
                onError = { imageLoaded = false },
                alpha = if (imageLoaded) 1f else 0f
            )
        }
    }
}

@Composable
private fun AchievementGrid(
    user: User,
    totalCompleted: Int
) {
    val now = System.currentTimeMillis()
    val monthClub = now - user.joinedAt >= 30L * 24 * 60 * 60 * 1000

    val achievements = listOf(
        R.string.achievement_first to (totalCompleted >= 1),
        R.string.achievement_five to (totalCompleted >= 5),
        R.string.achievement_ten to (totalCompleted >= 10),
        R.string.achievement_twenty_five to (totalCompleted >= 25),
        R.string.achievement_month_club to monthClub
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        achievements.chunked(2).forEach { rowAchievements ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowAchievements.forEach { (labelId, unlocked) ->
                    AssistChip(
                        modifier = Modifier.weight(1f),
                        onClick = {},
                        label = {
                            Text(
                                text = stringResource(labelId),
                                maxLines = 2
                            )
                        },
                        leadingIcon = {
                            androidx.compose.material3.Icon(
                                imageVector = if (unlocked) {
                                    Icons.Filled.EmojiEvents
                                } else {
                                    Icons.Filled.Lock
                                },
                                contentDescription = null,
                                tint = if (unlocked) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        enabled = unlocked
                    )
                }
                if (rowAchievements.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    user: User,
    onDismiss: () -> Unit,
    onSave: (name: String, weight: String, goal: String, goalVisits: Int) -> Unit
) {
    var name by remember(user.id) { mutableStateOf(user.name) }
    var weight by remember(user.id) { mutableStateOf(user.weight?.toString().orEmpty()) }
    var goal by remember(user.id) { mutableStateOf(user.goal.orEmpty()) }
    var goalVisits by remember(user.id) {
        mutableFloatStateOf(user.goalVisits.coerceIn(5, 30).toFloat())
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.edit_profile_title),
                style = MaterialTheme.typography.titleLarge
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(stringResource(R.string.weight)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                label = { Text(stringResource(R.string.goal)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(
                    R.string.monthly_goal_visits,
                    goalVisits.toInt()
                )
            )
            Slider(
                value = goalVisits,
                onValueChange = { goalVisits = it },
                valueRange = 5f..30f,
                steps = 24
            )
            ClubButton(
                text = stringResource(R.string.save),
                onClick = {
                    onSave(name, weight, goal, goalVisits.toInt())
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}