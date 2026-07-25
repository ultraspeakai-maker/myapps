package com.camera.secretvideorecorder

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Official Test Unit IDs
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"

    private var interstitialAd: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var isInterstitialLoading = false
    private var isAppOpenLoading = false
    
    // User ad IDs (can be configured later)
    private var userBannerId: String? = null
    private var userInterstitialId: String? = null
    private var userAppOpenId: String? = null

    fun configureAdIds(bannerId: String?, interstitialId: String?, appOpenId: String?) {
        userBannerId = bannerId?.takeIf { it.isNotBlank() }
        userInterstitialId = interstitialId?.takeIf { it.isNotBlank() }
        userAppOpenId = appOpenId?.takeIf { it.isNotBlank() }
    }

    private fun getBannerAdId(): String = userBannerId ?: TEST_BANNER_ID
    private fun getInterstitialAdId(): String = userInterstitialId ?: TEST_INTERSTITIAL_ID
    private fun getAppOpenAdId(): String = userAppOpenId ?: TEST_APP_OPEN_ID

    // --- Banner Ad ---
    @Composable
    fun BannerAd(modifier: Modifier = Modifier) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = getBannerAdId()
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    loadAd(AdRequest.Builder().build())
                }
            },
            update = { adView ->
                adView.loadAd(AdRequest.Builder().build())
            }
        )
    }

    // --- Interstitial Ad ---
    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true
        Log.d(TAG, "Loading Interstitial Ad...")

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            getInterstitialAdId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Interstitial failed to load: ${adError.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad Loaded successfully.")
                    interstitialAd = ad
                    isInterstitialLoading = false
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed.")
                    interstitialAd = null
                    loadInterstitialAd(activity) // Pre-load next
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "Interstitial ad failed to show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdClosed()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready.")
            loadInterstitialAd(activity)
            onAdClosed() // Fallback so user experience isn't blocked
        }
    }

    // --- App Open Ad ---
    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || isAppOpenLoading) return
        isAppOpenLoading = true
        Log.d(TAG, "Loading App Open Ad...")

        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            getAppOpenAdId(),
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App Open Ad Loaded.")
                    appOpenAd = ad
                    isAppOpenLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                    appOpenAd = null
                    isAppOpenLoading = false
                }
            }
        )
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onDismissed: () -> Unit) {
        val ad = appOpenAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "App open ad dismissed.")
                    appOpenAd = null
                    loadAppOpenAd(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "App open ad failed to show: ${adError.message}")
                    appOpenAd = null
                    loadAppOpenAd(activity)
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "App open ad not ready.")
            loadAppOpenAd(activity)
            onDismissed()
        }
    }
}
