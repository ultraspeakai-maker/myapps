package com.example.aistockmarketnews.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(private val context: Context) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false

    private val primaryAdUnitId = "ca-app-pub-5621401237476154/9943134271"
    private val testAdUnitId = "ca-app-pub-3940256099942544/9257395921"

    fun loadAndShowAd(activity: Activity) {
        if (isLoadingAd || isShowingAd) return
        if (isAdAvailable()) {
            showAdIfAvailable(activity)
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            primaryAdUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    showAdIfAvailable(activity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w("AppOpenAdManager", "Primary App Open failed: ${loadAdError.message}. Trying test fallback...")
                    AppOpenAd.load(
                        context,
                        testAdUnitId,
                        request,
                        object : AppOpenAd.AppOpenAdLoadCallback() {
                            override fun onAdLoaded(ad: AppOpenAd) {
                                appOpenAd = ad
                                isLoadingAd = false
                                showAdIfAvailable(activity)
                            }

                            override fun onAdFailedToLoad(fallbackError: LoadAdError) {
                                isLoadingAd = false
                            }
                        }
                    )
                }
            }
        )
    }

    fun isAdAvailable(): Boolean = appOpenAd != null

    fun showAdIfAvailable(activity: Activity) {
        if (!isShowingAd && isAdAvailable() && !activity.isFinishing && !activity.isDestroyed) {
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
            appOpenAd?.show(activity)
        }
    }
}
