package com.camera.secretvideorecorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.camera.secretvideorecorder.theme.GlassySurface
import com.camera.secretvideorecorder.theme.NeonCyan
import com.camera.secretvideorecorder.theme.NeonPurple
import com.camera.secretvideorecorder.theme.SecretVideoRecorderTheme
import com.camera.secretvideorecorder.theme.TextPrimary
import com.camera.secretvideorecorder.theme.TextSecondary
import com.camera.secretvideorecorder.ui.GalleryScreen
import com.camera.secretvideorecorder.ui.RecordScreen
import com.camera.secretvideorecorder.ui.SchedulerScreen
import com.camera.secretvideorecorder.ui.SettingsScreen
import kotlinx.coroutines.delay

enum class AppScreen {
    Record,
    Scheduler,
    Gallery,
    Settings
}

class MainActivity : ComponentActivity() {

    private lateinit var settingsManager: SettingsManager
    private var isAppOpenAdShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)

        // Load Ads immediately
        AdManager.loadAppOpenAd(this)
        AdManager.loadInterstitialAd(this)

        enableEdgeToEdge()
        setContent {
            SecretVideoRecorderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppLayout(
                        activity = this,
                        settingsManager = settingsManager
                    )
                }
            }
        }

        // Show App Open Ad as soon as possible after startup
        showAppOpenAdWithDelay()
    }

    private fun showAppOpenAdWithDelay() {
        // Run a small delay to allow AdMob App Open ad to load on start
        window.decorView.postDelayed({
            if (!isAppOpenAdShown) {
                AdManager.showAppOpenAdIfAvailable(this) {
                    isAppOpenAdShown = true
                }
            }
        }, 1200)
    }

    override fun onResume() {
        super.onResume()
        // Reload interstitial if it was cleared
        AdManager.loadInterstitialAd(this)
    }
}

@Composable
fun MainAppLayout(
    activity: ComponentActivity,
    settingsManager: SettingsManager
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.Record) }
    var permissionsGranted by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    // List of required runtime permissions
    val requiredPermissions = remember {
        mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        permissionsGranted = cameraGranted && audioGranted
        
        if (!permissionsGranted) {
            Toast.makeText(context, "Camera and Audio permissions are required to record video.", Toast.LENGTH_LONG).show()
        }
    }

    // Check permissions on launch
    LaunchedEffect(Unit) {
        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        permissionsGranted = allGranted
        if (!allGranted) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    // Back Gesture Interception
    BackHandler(enabled = true) {
        // If we exit from the app, show interstitial and close
        AdManager.showInterstitialAd(activity) {
            activity.finish()
        }
    }

    Scaffold(
        bottomBar = {
            Column {
                // Banner Ad permanently placed above bottom nav
                AdManager.BannerAd(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                )
                
                // Bottom navigation bar
                NavigationBar(
                    containerColor = GlassySurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    val tabs = listOf(
                        Triple(AppScreen.Record, "Record", Icons.Default.Videocam),
                        Triple(AppScreen.Scheduler, "Schedule", Icons.Default.Schedule),
                        Triple(AppScreen.Gallery, "Videos", Icons.Default.Folder),
                        Triple(AppScreen.Settings, "Settings", Icons.Default.Settings)
                    )

                    tabs.forEach { (screen, label, icon) ->
                        val selected = currentScreen == screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonPurple.copy(alpha = 0.2f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Display active screen
            when (currentScreen) {
                AppScreen.Record -> {
                    RecordScreen(
                        settingsManager = settingsManager,
                        onRequestPermissions = { onGranted ->
                            val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            val audioGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            
                            if (cameraGranted && audioGranted) {
                                // Now check overlay permission (optional but recommended for starting from background)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                    showOverlayPermissionDialog = true
                                } else {
                                    onGranted()
                                }
                            } else {
                                permissionLauncher.launch(requiredPermissions)
                            }
                        }
                    )
                }
                AppScreen.Scheduler -> SchedulerScreen(settingsManager = settingsManager)
                AppScreen.Gallery -> GalleryScreen()
                AppScreen.Settings -> SettingsScreen(settingsManager = settingsManager)
            }
        }
    }

    // Overlay Permission Request Dialog
    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = { Text("Display Overlay Permission") },
            text = {
                Text("To support stealth recording starting from background schedules or quick widget triggers, " +
                        "please allow SVR to 'Draw over other apps' in the system settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverlayPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text("Grant Permission", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showOverlayPermissionDialog = false
                        Toast.makeText(context, "Overlay permission skipped. Some background features may fail.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Skip", color = TextSecondary)
                }
            }
        )
    }
}
