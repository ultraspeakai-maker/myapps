package com.camera.secretvideorecorder.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camera.secretvideorecorder.AdManager
import com.camera.secretvideorecorder.CameraService
import com.camera.secretvideorecorder.SettingsManager
import com.camera.secretvideorecorder.theme.BrightPink
import com.camera.secretvideorecorder.theme.NeonCyan
import com.camera.secretvideorecorder.theme.NeonPurple
import com.camera.secretvideorecorder.theme.TextPrimary
import com.camera.secretvideorecorder.theme.TextSecondary

@Composable
fun RecordScreen(
    settingsManager: SettingsManager,
    onRequestPermissions: (onGranted: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val isRecording by CameraService.isRecording.collectAsState()
    val durationSeconds by CameraService.durationSeconds.collectAsState()

    var isBackCamera by remember { mutableStateOf(settingsManager.isBackCamera) }
    var isAudioEnabled by remember { mutableStateOf(settingsManager.isAudioEnabled) }

    // Pulsing animation for the recording button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App header / Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "V SPY CAMERA",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Text(
                text = "Unlimited Free Recordings",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        // Recording Status & Timer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.height(100.dp)
        ) {
            if (isRecording) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BrightPink)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECORDING",
                        color = BrightPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatDuration(durationSeconds),
                    color = TextPrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Stealth Mode Ready",
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Press button below. Screen can be locked after starting.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // Central Start/Stop Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .scale(pulseScale)
        ) {
            // Glowing border
            val gradientBrush = Brush.sweepGradient(
                colors = if (isRecording) {
                    listOf(BrightPink, Color.Red, BrightPink)
                } else {
                    listOf(NeonPurple, NeonCyan, NeonPurple)
                }
            )

            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(gradientBrush)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .clickable {
                        onRequestPermissions {
                            toggleRecording(context, isRecording, settingsManager)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Inner button body
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isRecording) {
                                    listOf(BrightPink.copy(alpha = 0.15f), Color.Transparent)
                                } else {
                                    listOf(NeonPurple.copy(alpha = 0.15f), Color.Transparent)
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        contentDescription = "Record Trigger",
                        modifier = Modifier.size(64.dp),
                        tint = if (isRecording) BrightPink else NeonPurple
                    )
                }
            }
        }

        // Toggles & Configurations
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Camera source selector
                QuickConfigCard(
                    title = "Camera Source",
                    value = if (isBackCamera) "Back" else "Front",
                    icon = Icons.Default.Videocam,
                    isEnabled = !isRecording,
                    onClick = {
                        isBackCamera = !isBackCamera
                        settingsManager.isBackCamera = isBackCamera
                    }
                )

                // Audio recording toggle
                QuickConfigCard(
                    title = "Audio Mic",
                    value = if (isAudioEnabled) "Enabled" else "Muted",
                    icon = if (isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                    isEnabled = !isRecording,
                    onClick = {
                        isAudioEnabled = !isAudioEnabled
                        settingsManager.isAudioEnabled = isAudioEnabled
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun QuickConfigCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .width(140.dp)
            .clickable(enabled = isEnabled) { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isEnabled) NeonCyan else TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = if (isEnabled) TextPrimary else TextSecondary.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun toggleRecording(context: Context, isRecording: Boolean, settingsManager: SettingsManager) {
    val serviceIntent = Intent(context, CameraService::class.java)
    if (isRecording) {
        serviceIntent.action = CameraService.ACTION_STOP_RECORDING
        context.startService(serviceIntent)
    } else {
        serviceIntent.action = CameraService.ACTION_START_RECORDING
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
