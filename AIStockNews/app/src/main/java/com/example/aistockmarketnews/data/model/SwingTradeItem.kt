package com.example.aistockmarketnews.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SwingTradeItem(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val entryMin: Double,
    val entryMax: Double,
    val target1: Double,
    val target2: Double,
    val stopLoss: Double,
    val approxDays: String, // e.g. "5 - 12 Trading Days"
    val forecastedTargetDate: String, // e.g. "05 Aug 2026"
    val achievedPctTillDate: Double, // e.g. +2.4%
    val expectedReturnPct: Double,
    val riskReward: Double,
    val patternTrigger: String,
    val confidenceScore: Int
)
