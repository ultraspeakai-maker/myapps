package com.example.aistockmarketnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.aistockmarketnews.data.ads.AppOpenAdManager
import com.example.aistockmarketnews.theme.AIStockIntelligenceTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

class MainActivity : ComponentActivity() {

    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) { status ->
            val configuration = RequestConfiguration.Builder().build()
            MobileAds.setRequestConfiguration(configuration)
        }

        // Initialize App Open Ad Manager & load/show ad on app startup
        appOpenAdManager = AppOpenAdManager(this)
        appOpenAdManager.loadAndShowAd(this)

        enableEdgeToEdge()
        setContent {
            AIStockIntelligenceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appOpenAdManager.showAdIfAvailable(this)
    }
}
