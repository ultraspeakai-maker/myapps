package com.camera.secretvideorecorder

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ScheduledRecordTask(
    val id: String,
    val timeMillis: Long,
    val durationSeconds: Int,
    val isBackCamera: Boolean,
    val enableAudio: Boolean
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("svr_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CAMERA_BACK = "camera_back"
        private const val KEY_VIDEO_QUALITY = "video_quality"
        private const val KEY_NOTIF_TITLE = "notif_title"
        private const val KEY_NOTIF_CONTENT = "notif_content"
        private const val KEY_RECORD_AUDIO = "record_audio"
        private const val KEY_VIBRATE = "vibrate"
        private const val KEY_SHUTTER_SOUND = "shutter_sound"
        private const val KEY_SCHEDULED_TASKS = "scheduled_tasks"
        
        // Quality values: "highest", "1080p", "720p", "480p"
        const val QUALITY_HIGHEST = "highest"
        const val QUALITY_1080P = "1080p"
        const val QUALITY_720P = "720p"
        const val QUALITY_480P = "480p"
    }

    var isBackCamera: Boolean
        get() = prefs.getBoolean(KEY_CAMERA_BACK, true)
        set(value) = prefs.edit().putBoolean(KEY_CAMERA_BACK, value).apply()

    var videoQuality: String
        get() = prefs.getString(KEY_VIDEO_QUALITY, QUALITY_HIGHEST) ?: QUALITY_HIGHEST
        set(value) = prefs.edit().putString(KEY_VIDEO_QUALITY, value).apply()

    var notificationTitle: String
        get() = prefs.getString(KEY_NOTIF_TITLE, "System Update") ?: "System Update"
        set(value) = prefs.edit().putString(KEY_NOTIF_TITLE, value).apply()

    var notificationContent: String
        get() = prefs.getString(KEY_NOTIF_CONTENT, "Syncing system resources...") ?: "Syncing system resources..."
        set(value) = prefs.edit().putString(KEY_NOTIF_CONTENT, value).apply()

    var isAudioEnabled: Boolean
        get() = prefs.getBoolean(KEY_RECORD_AUDIO, true)
        set(value) = prefs.edit().putBoolean(KEY_RECORD_AUDIO, value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE, value).apply()

    var isShutterSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHUTTER_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_SHUTTER_SOUND, value).apply()

    fun getScheduledTasks(): List<ScheduledRecordTask> {
        val jsonStr = prefs.getString(KEY_SCHEDULED_TASKS, null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<ScheduledRecordTask>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveScheduledTasks(tasks: List<ScheduledRecordTask>) {
        val jsonStr = Json.encodeToString(tasks)
        prefs.edit().putString(KEY_SCHEDULED_TASKS, jsonStr).apply()
    }

    fun addScheduledTask(task: ScheduledRecordTask) {
        val tasks = getScheduledTasks().toMutableList()
        tasks.add(task)
        saveScheduledTasks(tasks)
    }

    fun removeScheduledTask(taskId: String) {
        val tasks = getScheduledTasks().filter { it.id != taskId }
        saveScheduledTasks(tasks)
    }
}
