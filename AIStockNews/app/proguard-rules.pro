# ProGuard rules for AI Stock News

# Keep Google Mobile Ads SDK classes
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Keep kotlinx.serialization models
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep Jetpack Compose & ViewModel components
-keep class androidx.compose.** { *; }
-keep class com.example.aistockmarketnews.data.model.** { *; }
