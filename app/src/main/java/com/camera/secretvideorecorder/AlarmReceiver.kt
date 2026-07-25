package com.camera.secretvideorecorder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"

        fun scheduleAlarm(context: Context, task: ScheduledRecordTask) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("TASK_ID", task.id)
                putExtra("DURATION", task.durationSeconds)
                putExtra("IS_BACK_CAMERA", task.isBackCamera)
                putExtra("ENABLE_AUDIO", task.enableAudio)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        task.timeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        task.timeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.timeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for task ${task.id} at ${task.timeMillis}")
        }

        fun cancelAlarm(context: Context, task: ScheduledRecordTask) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled alarm for task ${task.id}")
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "onReceive: action=$action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all active alarms on boot
            val settingsManager = SettingsManager(context)
            val tasks = settingsManager.getScheduledTasks()
            val now = System.currentTimeMillis()
            
            val validTasks = mutableListOf<ScheduledRecordTask>()
            for (task in tasks) {
                if (task.timeMillis > now) {
                    scheduleAlarm(context, task)
                    validTasks.add(task)
                }
            }
            // Save cleaned up list (remove expired tasks)
            settingsManager.saveScheduledTasks(validTasks)
            return
        }

        // Trigger recording
        val taskId = intent?.getStringExtra("TASK_ID") ?: return
        val duration = intent.getIntExtra("DURATION", 0)
        val isBackCamera = intent.getBooleanExtra("IS_BACK_CAMERA", true)
        val enableAudio = intent.getBooleanExtra("ENABLE_AUDIO", true)

        Log.d(TAG, "Alarm triggered! Starting CameraService for taskId=$taskId, duration=$duration")

        // First update settings temporarily so service reads these specific values
        val settingsManager = SettingsManager(context)
        settingsManager.isBackCamera = isBackCamera
        settingsManager.isAudioEnabled = enableAudio
        // Remove task from list since it has fired
        settingsManager.removeScheduledTask(taskId)

        val serviceIntent = Intent(context, CameraService::class.java).apply {
            this.action = CameraService.ACTION_START_RECORDING
            putExtra("DURATION_LIMIT", duration)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service directly, attempting transparent activity workaround: ${e.message}")
            
            // If background starts fail due to Android 14 restrictions, we launch our MainActivity as transparent 
            // or trigger it to handle starting the service.
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("START_RECORDING_FROM_BACKGROUND", true)
                putExtra("DURATION_LIMIT", duration)
            }
            context.startActivity(activityIntent)
        }
    }
}
