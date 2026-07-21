package com.screen.autolocker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.screen.autolocker.ui.theme.AppPalette

@Composable
fun PrivacyPolicyScreen(
    palette: AppPalette,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                "Privacy Policy",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = palette.text
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Your privacy is important to us. This app is designed with your security in mind.",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.muted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            PolicySection(
                title = "No Data Collection",
                description = "This app does not collect, store, or transmit any personal information. All data stays on your device.",
                palette = palette
            )

            PolicySection(
                title = "Accessibility Service",
                description = "Used only to lock your screen. No data is read or transmitted. The service only performs screen lock actions.",
                palette = palette
            )

            PolicySection(
                title = "Local Storage",
                description = "Your settings and lock history are stored locally on your device and are never uploaded anywhere.",
                palette = palette
            )

            PolicySection(
                title = "No Analytics",
                description = "We do not use any analytics, tracking, or third-party services that collect user data.",
                palette = palette
            )

            PolicySection(
                title = "Permissions",
                description = "We only request permissions necessary for locking your screen: Accessibility and Device Admin (optional).",
                palette = palette
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "By tapping 'Accept', you confirm that you understand and agree to this privacy policy.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = palette.buttonText
                )
            ) {
                Text(
                    "Accept & Continue",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onDecline) {
                Text(
                    "Decline",
                    color = palette.muted
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    description: String,
    palette: AppPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = palette.text
        )
        Spacer(Modifier.height(4.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )
    }
}