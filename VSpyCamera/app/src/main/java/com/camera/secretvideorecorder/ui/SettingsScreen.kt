package com.camera.secretvideorecorder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camera.secretvideorecorder.SettingsManager
import com.camera.secretvideorecorder.theme.BrightPink
import com.camera.secretvideorecorder.theme.GlassySurface
import com.camera.secretvideorecorder.theme.NeonCyan
import com.camera.secretvideorecorder.theme.NeonPurple
import com.camera.secretvideorecorder.theme.TextPrimary
import com.camera.secretvideorecorder.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsManager: SettingsManager) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Preferences states
    var isBackCamera by remember { mutableStateOf(settingsManager.isBackCamera) }
    var videoQuality by remember { mutableStateOf(settingsManager.videoQuality) }
    var notificationTitle by remember { mutableStateOf(settingsManager.notificationTitle) }
    var notificationContent by remember { mutableStateOf(settingsManager.notificationContent) }
    var isAudioEnabled by remember { mutableStateOf(settingsManager.isAudioEnabled) }
    var isVibrationEnabled by remember { mutableStateOf(settingsManager.isVibrationEnabled) }
    var isShutterSoundEnabled by remember { mutableStateOf(settingsManager.isShutterSoundEnabled) }

    // Quality Selection Dropdown
    val qualityOptions = listOf(
        Pair("Highest (Default)", SettingsManager.QUALITY_HIGHEST),
        Pair("Full HD (1080p)", SettingsManager.QUALITY_1080P),
        Pair("HD (720p)", SettingsManager.QUALITY_720P),
        Pair("SD (480p)", SettingsManager.QUALITY_480P)
    )
    var isQualityDropdownExpanded by remember { mutableStateOf(false) }
    var selectedQualityText by remember {
        mutableStateOf(
            qualityOptions.firstOrNull { it.second == videoQuality }?.first ?: "Highest"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Settings",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- Camera & Video Config ---
        SettingsCategoryHeader(title = "Camera & Quality", icon = Icons.Default.Camera)
        SettingsCard {
            // Camera Source
            SettingsSwitchRow(
                title = "Use Back Camera",
                subtitle = "Turn off to record using front camera",
                checked = isBackCamera,
                onCheckedChange = {
                    isBackCamera = it
                    settingsManager.isBackCamera = it
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quality Select
            ExposedDropdownMenuBox(
                expanded = isQualityDropdownExpanded,
                onExpandedChange = { isQualityDropdownExpanded = !isQualityDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedQualityText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Video Quality") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isQualityDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                        focusedBorderColor = NeonPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedLabelColor = NeonPurple,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isQualityDropdownExpanded,
                    onDismissRequest = { isQualityDropdownExpanded = false }
                ) {
                    qualityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.first) },
                            onClick = {
                                selectedQualityText = option.first
                                videoQuality = option.second
                                settingsManager.videoQuality = option.second
                                isQualityDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Audio Record
            SettingsSwitchRow(
                title = "Record Audio",
                subtitle = "Include microphone sound in recordings",
                checked = isAudioEnabled,
                onCheckedChange = {
                    isAudioEnabled = it
                    settingsManager.isAudioEnabled = it
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Privacy & Stealth ---
        SettingsCategoryHeader(title = "Privacy & Notifications", icon = Icons.Default.NotificationsActive)
        SettingsCard {
            Text(
                text = "Customize the persistent notification to disguise the recording service.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Notification Title Input
            OutlinedTextField(
                value = notificationTitle,
                onValueChange = {
                    notificationTitle = it
                    settingsManager.notificationTitle = it
                },
                label = { Text("Fake Notification Title") },
                placeholder = { Text("e.g. System Update") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                    focusedBorderColor = NeonPurple,
                    unfocusedLabelColor = TextSecondary,
                    focusedLabelColor = NeonPurple,
                    unfocusedTextColor = TextPrimary,
                    focusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notification Content Input
            OutlinedTextField(
                value = notificationContent,
                onValueChange = {
                    notificationContent = it
                    settingsManager.notificationContent = it
                },
                label = { Text("Fake Notification Content") },
                placeholder = { Text("e.g. Syncing system resources...") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                    focusedBorderColor = NeonPurple,
                    unfocusedLabelColor = TextSecondary,
                    focusedLabelColor = NeonPurple,
                    unfocusedTextColor = TextPrimary,
                    focusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Feedback and Sounds ---
        SettingsCategoryHeader(title = "Sounds & Feedback", icon = Icons.Default.VolumeUp)
        SettingsCard {
            // Vibration Feedback
            SettingsSwitchRow(
                title = "Vibration Feedback",
                subtitle = "Vibrate when recording starts or stops",
                checked = isVibrationEnabled,
                onCheckedChange = {
                    isVibrationEnabled = it
                    settingsManager.isVibrationEnabled = it
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Shutter Sound
            SettingsSwitchRow(
                title = "Shutter / Alert Sound",
                subtitle = "Play start/stop beep (highly unrecommended for stealth)",
                checked = isShutterSoundEnabled,
                onCheckedChange = {
                    isShutterSoundEnabled = it
                    settingsManager.isShutterSoundEnabled = it
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- System Settings & Battery Exemption ---
        SettingsCategoryHeader(title = "Battery & Permissions", icon = Icons.Default.BatteryAlert)
        SettingsCard {
            Text(
                text = "Android may terminate background services to save battery. " +
                        "Exempting this app ensures recording doesn't cut off.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    val packageName = context.packageName
                    if (pm.isIgnoringBatteryOptimizations(packageName)) {
                        Toast.makeText(context, "Battery optimization is already disabled!", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Unable to open battery settings directly.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.BatteryAlert, "Battery Alert")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request Battery Exemption", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Privacy Policy & Play Store Compliance ---
        var showPrivacyDialog by remember { mutableStateOf(false) }

        SettingsCategoryHeader(title = "Privacy & Legal", icon = Icons.Default.Info)
        SettingsCard {
            Text(
                text = "V Spy Camera records video and audio locally on your device. " +
                        "No video files or personal data are collected, shared, or uploaded to any cloud server.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showPrivacyDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Read Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ultraspeakai-maker/myapps"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassySurface),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View GitHub Repo", fontSize = 12.sp, color = NeonCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Info Section ---
        SettingsCategoryHeader(title = "App Info", icon = Icons.Default.Info)
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("App Name", color = TextSecondary, fontSize = 14.sp)
                Text("V Spy Camera", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Version", color = TextSecondary, fontSize = 14.sp)
                Text("1.0.0 (PlayStore Build)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Support Email", color = TextSecondary, fontSize = 14.sp)
                Text("ultraspeakai@gmail.com", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Recording limit", color = TextSecondary, fontSize = 14.sp)
                Text("Unlimited (Free)", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Avoid getting cut off by bottom nav

        if (showPrivacyDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Privacy Policy for V Spy Camera - Unlimited Free Recordings\n\n" +
                                    "1. Local Storage Only:\n" +
                                    "V Spy Camera is designed with user privacy as top priority. All videos and audio recordings captured by this app are saved directly to your device local MediaStore gallery (DCIM/SecretVideoRecorder). No media is uploaded, transmitted, or accessible to us or third parties.\n\n" +
                                    "2. Permissions Usage:\n" +
                                    "- Camera: Required to capture video.\n" +
                                    "- Microphone: Required to record audio with video.\n" +
                                    "- Foreground Service: Used to display a persistent notification while background recording is active.\n" +
                                    "- Notifications: Used to keep you informed of active recordings.\n\n" +
                                    "3. Advertising:\n" +
                                    "This free app uses Google AdMob to display banner, app open, and interstitial ads. AdMob may collect standard non-personally identifiable advertising IDs and diagnostic data strictly for ad serving and analytics.\n\n" +
                                    "4. Contact & Support:\n" +
                                    "Email: ultraspeakai@gmail.com\n" +
                                    "Repository: https://github.com/ultraspeakai-maker/myapps",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("Close", color = NeonCyan)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = NeonCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonPurple,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.background
            )
        )
    }
}
