package com.example.fitnessclub.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.fitnessclub.R

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    val loadingDescription = stringResource(R.string.loading)

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = loadingDescription
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}