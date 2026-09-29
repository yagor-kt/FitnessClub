package com.example.fitnessclub.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitnessclub.ui.theme.ClubSkeleton

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp
) {
    val transition = rememberInfiniteTransition(label = "skeleton_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Box(
        modifier = modifier
            .height(height)
            .alpha(alpha)
            .background(
                color = ClubSkeleton,
                shape = RoundedCornerShape(10.dp)
            )
    )
}

@Composable
fun ScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBox(
            modifier = Modifier.fillMaxWidth(0.58f),
            height = 30.dp
        )

        repeat(3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = ClubSkeleton.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth(0.46f),
                    height = 20.dp
                )
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth(0.82f),
                    height = 14.dp
                )
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth(0.64f),
                    height = 14.dp
                )
                Spacer(Modifier.height(2.dp))
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth(),
                    height = 38.dp
                )
            }
        }
    }
}