package com.camera.secretvideorecorder.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camera.secretvideorecorder.AlarmReceiver
import com.camera.secretvideorecorder.ScheduledRecordTask
import com.camera.secretvideorecorder.SettingsManager
import com.camera.secretvideorecorder.theme.BrightPink
import com.camera.secretvideorecorder.theme.NeonCyan
import com.camera.secretvideorecorder.theme.NeonPurple
import com.camera.secretvideorecorder.theme.TextPrimary
import com.camera.secretvideorecorder.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerScreen(settingsManager: SettingsManager) {
    val context = LocalContext.current
    var tasksList by remember { mutableStateOf(settingsManager.getScheduledTasks()) }

    // Date & Time selection states
    val calendar = remember { Calendar.getInstance() }
    var selectedDateText by remember { mutableStateOf("") }
    var selectedTimeText by remember { mutableStateOf("") }
    var hasDateSelected by remember { mutableStateOf(false) }
    var hasTimeSelected by remember { mutableStateOf(false) }

    // Task settings
    var durationSeconds by remember { mutableStateOf(300) } // Default 5 mins
    var isBackCamera by remember { mutableStateOf(true) }
    var enableAudio by remember { mutableStateOf(true) }

    // Duration options dropdown
    val durationOptions = listOf(
        Pair("1 Minute", 60),
        Pair("2 Minutes", 120),
        Pair("5 Minutes", 300),
        Pair("10 Minutes", 600),
        Pair("30 Minutes", 1800),
        Pair("1 Hour", 3600)
    )
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedDurationText by remember { mutableStateOf("5 Minutes") }

    // Show date picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDateText = format.format(calendar.time)
            hasDateSelected = true
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }

    // Show time picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            selectedTimeText = format.format(calendar.time)
            hasTimeSelected = true
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Schedule Recording",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Config Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date & Time pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedDateText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        placeholder = { Text("Select Date") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, "Date selector", tint = NeonCyan)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerDialog.show() },
                        enabled = false, // Clicking is handled by parent modifier
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = NeonPurple.copy(alpha = 0.5f),
                            disabledLabelColor = TextSecondary,
                            disabledTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = selectedTimeText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Time") },
                        placeholder = { Text("Select Time") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, "Time selector", tint = NeonCyan)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { timePickerDialog.show() },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = NeonPurple.copy(alpha = 0.5f),
                            disabledLabelColor = TextSecondary,
                            disabledTextColor = TextPrimary
                        )
                    )
                }

                // Duration Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDurationText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Recording Duration") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = NeonPurple.copy(alpha = 0.5f),
                            focusedBorderColor = NeonPurple,
                            unfocusedLabelColor = TextSecondary,
                            focusedLabelColor = NeonPurple
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        durationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.first) },
                                onClick = {
                                    selectedDurationText = option.first
                                    durationSeconds = option.second
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Switch for back camera vs front
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, "Camera Selector", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Back Camera", color = TextPrimary, fontSize = 14.sp)
                    }
                    Switch(
                        checked = isBackCamera,
                        onCheckedChange = { isBackCamera = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonPurple
                        )
                    )
                }

                // Switch for audio enabled
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (enableAudio) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Audio Switch",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Audio", color = TextPrimary, fontSize = 14.sp)
                    }
                    Switch(
                        checked = enableAudio,
                        onCheckedChange = { enableAudio = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonPurple
                        )
                    )
                }

                // Schedule Button
                Button(
                    onClick = {
                        if (!hasDateSelected || !hasTimeSelected) {
                            Toast.makeText(context, "Please select both date and time", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val selectedMillis = calendar.timeInMillis
                        if (selectedMillis <= System.currentTimeMillis()) {
                            Toast.makeText(context, "Please select a future time", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val task = ScheduledRecordTask(
                            id = UUID.randomUUID().toString(),
                            timeMillis = selectedMillis,
                            durationSeconds = durationSeconds,
                            isBackCamera = isBackCamera,
                            enableAudio = enableAudio
                        )

                        // Save task & schedule Alarm
                        settingsManager.addScheduledTask(task)
                        AlarmReceiver.scheduleAlarm(context, task)

                        // Refresh list
                        tasksList = settingsManager.getScheduledTasks()

                        // Reset Date/Time fields
                        selectedDateText = ""
                        selectedTimeText = ""
                        hasDateSelected = false
                        hasTimeSelected = false

                        Toast.makeText(context, "Recording scheduled successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple
                    )
                ) {
                    Icon(Icons.Default.Schedule, "Schedule Now")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Schedule Auto-Record", fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(color = NeonPurple.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Active Tasks Header
        Text(
            text = "Active Schedules",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Active Tasks List
        if (tasksList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No scheduled tasks active.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasksList) { task ->
                    ScheduledTaskItem(
                        task = task,
                        onCancel = {
                            AlarmReceiver.cancelAlarm(context, task)
                            settingsManager.removeScheduledTask(task.id)
                            tasksList = settingsManager.getScheduledTasks()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduledTaskItem(task: ScheduledRecordTask, onCancel: () -> Unit) {
    val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    val formattedDateTime = dateTimeFormat.format(Date(task.timeMillis))

    val durationText = when (task.durationSeconds) {
        60 -> "1 Min"
        120 -> "2 Mins"
        300 -> "5 Mins"
        600 -> "10 Mins"
        1800 -> "30 Mins"
        3600 -> "1 Hour"
        else -> "${task.durationSeconds / 60} Mins"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDateTime,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Duration: $durationText",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (task.isBackCamera) "Camera: Back" else "Camera: Front",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = if (task.enableAudio) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Audio option status",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Cancel task",
                    tint = BrightPink,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
