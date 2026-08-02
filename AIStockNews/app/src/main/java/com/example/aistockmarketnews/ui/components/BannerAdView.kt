package com.example.aistockmarketnews.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    primaryAdUnitId: String = "ca-app-pub-5621401237476154/5979838530"
) {
    val testAdUnitId = "ca-app-pub-3940256099942544/6300978111"
    var activeAdUnitId by remember { mutableStateOf(primaryAdUnitId) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        key(activeAdUnitId) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = activeAdUnitId

                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Log.w("BannerAdView", "Banner failed ($activeAdUnitId): ${error.message}.")
                                if (activeAdUnitId != testAdUnitId) {
                                    activeAdUnitId = testAdUnitId
                                }
                            }

                            override fun onAdLoaded() {
                                Log.d("BannerAdView", "Banner ad loaded successfully ($activeAdUnitId).")
                            }
                        }

                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}
