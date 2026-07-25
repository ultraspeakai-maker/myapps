package com.camera.secretvideorecorder

import android.app.Application
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SecretVideoRecorderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the Mobile Ads SDK asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@SecretVideoRecorderApp) {}
        }
    }
}
