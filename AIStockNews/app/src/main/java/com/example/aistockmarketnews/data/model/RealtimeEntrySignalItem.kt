package com.example.aistockmarketnews.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeEntrySignalItem(
    val symbol: String,
    val name: String,
    val signalType: String, // "BUY" or "SELL"
    val entryPrice: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val riskReward: Double,
    val triggerReason: String,
    val timeframe: String, // "15m / 30m / 60m"
    val confidenceScore: Int,
    val expectedReturnPct: Double = 2.5
)
