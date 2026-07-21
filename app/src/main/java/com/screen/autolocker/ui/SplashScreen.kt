package com.screen.autolocker.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.screen.autolocker.ui.theme.AppPalette
import com.screen.autolocker.ui.theme.backgroundBrush
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    palette: AppPalette,
    onFinish: () -> Unit
) {
    var progressTarget by remember { mutableStateOf(0f) }
    var logicDone by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(700),
        label = "splashProgress"
    )

    LaunchedEffect(Unit) {
        progressTarget = 0.35f
        delay(180)
        progressTarget = 0.72f
        delay(180)
        progressTarget = 1f
        logicDone = true
    }

    LaunchedEffect(animatedProgress, logicDone) {
        if (logicDone && animatedProgress >= 0.99f) {
            delay(150)
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                backgroundBrush(palette)
            )
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    translationX = 80f
                    translationY = -80f
                }
                .clip(CircleShape)
                .background(palette.secondary.copy(alpha = 0.72f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(92.dp),
                    shape = CircleShape,
                    color = palette.surface,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("AL", color = palette.accent, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(26.dp))

                Text(
                    "Auto Lock",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.text
                )

                Text(
                    "Quiet, focused screen security",
                    color = palette.muted
                )

                Spacer(Modifier.height(42.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = palette.accent,
                    trackColor = palette.surface.copy(alpha = 0.82f)
                )
            }

            Text(
                "Akshat Vhora",
                color = palette.muted
            )
        }
    }
}
