package com.example.aistockmarketnews.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MorningOpeningTrackerItem(
    val symbol: String,
    val name: String,
    val openPrice: Double, // 9:15 AM Opening Price
    val forecastTargetPrice: Double, // AI 9:15 AM Forecast Target Price
    val currentPrice: Double, // Real-time streaming or 3:30 PM Closing Price
    val expectedReturnPct: Double, // AI forecasted return %
    val actualReturnPct: Double, // Live actual return %
    val achievementPct: Double, // % Target Achieved (e.g. 104.2%)
    val status: String, // "TARGET MET 🎯", "IN PROGRESS ⏳", "STOP LOSS HIT ⚠️"
    val isTargetMet: Boolean,
    val recommendation: String, // "BUY" / "SELL"
    val confidenceScore: Int,
    val primaryReason: String
)
