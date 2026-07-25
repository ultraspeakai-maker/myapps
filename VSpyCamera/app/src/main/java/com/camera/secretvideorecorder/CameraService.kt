package com.camera.secretvideorecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaActionSound
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraService : LifecycleService() {

    companion object {
        private const val TAG = "CameraService"
        const val ACTION_START_RECORDING = "ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "ACTION_STOP_RECORDING"
        
        private const val NOTIFICATION_ID = 8888
        private const val CHANNEL_ID = "svr_channel"

        private val _isRecording = MutableStateFlow(false)
        val isRecording = _isRecording.asStateFlow()

        private val _durationSeconds = MutableStateFlow(0L)
        val durationSeconds = _durationSeconds.asStateFlow()
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var timerJob: Job? = null
    private lateinit var settingsManager: SettingsManager
    private val mediaActionSound = MediaActionSound()

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        val action = intent?.action
        Log.d(TAG, "onStartCommand action: $action")

        when (action) {
            ACTION_START_RECORDING -> {
                startForegroundServiceNotification()
                if (!_isRecording.value) {
                    val durationLimit = intent?.getIntExtra("DURATION_LIMIT", 0) ?: 0
                    startRecordingFlow(durationLimit)
                }
            }
            ACTION_STOP_RECORDING -> {
                stopRecordingFlow()
            }
        }

        return START_NOT_STICKY
    }

    private fun startForegroundServiceNotification() {
        val title = settingsManager.notificationTitle
        val content = settingsManager.notificationContent

        createNotificationChannel()

        // Create main app intent
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            this, 0, appIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create stop action intent
        val stopIntent = Intent(this, CameraService::class.java).apply {
            this.action = ACTION_STOP_RECORDING
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync) // Generic spinning sync icon
            .setContentIntent(appPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Recording",
                stopPendingIntent
            )
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Background Recorder Service"
            val descriptionText = "Handles recording status indicators"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startRecordingFlow(durationLimit: Int) {
        Log.d(TAG, "startRecordingFlow init...")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                // Read configuration
                val isBack = settingsManager.isBackCamera
                val qualityString = settingsManager.videoQuality
                val enableAudio = settingsManager.isAudioEnabled
                
                val cameraSelector = if (isBack) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

                // Map quality
                val quality = when (qualityString) {
                    SettingsManager.QUALITY_1080P -> Quality.FHD
                    SettingsManager.QUALITY_720P -> Quality.HD
                    SettingsManager.QUALITY_480P -> Quality.SD
                    else -> Quality.HIGHEST
                }

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(quality))
                    .build()
                
                videoCapture = VideoCapture.withOutput(recorder)

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, cameraSelector, videoCapture)

                // Configure output in MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, "SVR_${System.currentTimeMillis()}")
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/SecretVideoRecorder")
                }

                val outputOptions = MediaStoreOutputOptions.Builder(
                    contentResolver,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                ).setContentValues(contentValues).build()

                // Vibration feedback on start
                vibrateDevice(200)

                // Shutter sound
                if (settingsManager.isShutterSoundEnabled) {
                    mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING)
                }

                // Start recording
                activeRecording = videoCapture?.output
                    ?.prepareRecording(this, outputOptions)
                    ?.apply {
                        if (enableAudio) {
                            withAudioEnabled()
                        }
                    }
                    ?.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                        when (recordEvent) {
                            is VideoRecordEvent.Start -> {
                                Log.d(TAG, "CameraX Recording Started Successfully")
                                _isRecording.value = true
                                startTimer()
                                
                                // Auto stop after duration limit
                                if (durationLimit > 0) {
                                    lifecycleScope.launch {
                                        delay(durationLimit * 1000L)
                                        if (_isRecording.value) {
                                            stopRecordingFlow()
                                        }
                                    }
                                }
                            }
                            is VideoRecordEvent.Finalize -> {
                                Log.d(TAG, "CameraX Recording Finalized")
                                stopTimer()
                                _isRecording.value = false
                                
                                if (recordEvent.hasError()) {
                                    Log.e(TAG, "Recording finalized with error code: ${recordEvent.error}")
                                }
                                
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()
                            }
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera recording flow: ${e.message}", e)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopRecordingFlow() {
        Log.d(TAG, "Stopping recording flow...")
        // Vibration feedback on stop
        vibrateDevice(100)
        Thread.sleep(80)
        vibrateDevice(100)

        // Shutter sound
        if (settingsManager.isShutterSoundEnabled) {
            mediaActionSound.play(MediaActionSound.STOP_VIDEO_RECORDING)
        }

        activeRecording?.stop()
        activeRecording = null
        
        cameraProvider?.unbindAll()
    }

    private fun startTimer() {
        timerJob?.cancel()
        _durationSeconds.value = 0L
        timerJob = lifecycleScope.launch {
            while (true) {
                delay(1000)
                _durationSeconds.value += 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _durationSeconds.value = 0L
    }

    private fun vibrateDevice(duration: Long) {
        if (!settingsManager.isVibrationEnabled) return
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        mediaActionSound.release()
    }
}
