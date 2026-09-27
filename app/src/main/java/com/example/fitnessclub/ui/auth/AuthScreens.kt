package com.example.fitnessclub.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fitnessclub.R
import com.example.fitnessclub.ui.components.ClubButton
import com.example.fitnessclub.ui.components.ClubOutlinedButton
import com.example.fitnessclub.ui.components.LoadingIndicator

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (Long) -> Unit,
    onRegisterClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val current = state) {
            is LoginUiState.Success -> current.signedInUserId?.let(onLoginSuccess)
            is LoginUiState.Error -> snackbarHostState.showSnackbar(current.message)
            LoginUiState.Loading -> Unit
        }
    }

    if (state is LoginUiState.Loading) {
        LoadingIndicator()
        return
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            ClubButton(
                text = stringResource(R.string.login_button),
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            ClubOutlinedButton(
                text = stringResource(R.string.register_button),
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth()
            )
            if (state is LoginUiState.Error) {
                Spacer(Modifier.height(12.dp))
                ClubOutlinedButton(
                    text = stringResource(R.string.retry),
                    onClick = viewModel::retry,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onRegistrationSuccess: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (val current = state) {
            is RegistrationUiState.Success ->
                current.signedInUserId?.let(onRegistrationSuccess)
            is RegistrationUiState.Error -> snackbarHostState.showSnackbar(current.message)
            RegistrationUiState.Loading -> Unit
        }
    }

    if (state is RegistrationUiState.Loading) {
        LoadingIndicator()
        return
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            ClubButton(
                text = stringResource(R.string.register_submit),
                onClick = { viewModel.register(email, password, name) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            ClubOutlinedButton(
                text = stringResource(R.string.back),
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
            if (state is RegistrationUiState.Error) {
                Spacer(Modifier.height(12.dp))
                ClubOutlinedButton(
                    text = stringResource(R.string.retry),
                    onClick = viewModel::retry,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}