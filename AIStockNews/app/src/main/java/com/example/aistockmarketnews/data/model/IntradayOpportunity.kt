package com.example.aistockmarketnews.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IntradayOpportunity(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val aiScore: Int, // 0 to 100
    val confidencePct: Int, // e.g. 88%
    val tradeDirection: String, // "BUY" or "SELL"
    val entryZoneMin: Double, // Conservative entry
    val entryZoneMax: Double, // Aggressive entry
    val suggestedStopLoss: Double,
    val trailingStopLoss: Double,
    val target1: Double,
    val target2: Double,
    val target3: Double,
    val riskRewardRatio: Double, // e.g. 2.4
    val expectedMovePct: Double, // e.g. 2.8%
    val expectedProfitPerShare: Double,
    val probabilityTarget1Hit: Int, // e.g. 85%
    val probabilityStopLossHit: Int, // e.g. 12%
    val currentPattern: String, // "Bull Flag", "Ascending Triangle", "Order Block Breakout", etc.
    val patternCompletionPct: Int, // e.g. 91%
    val trendStrength: String, // "Strong Bullish", "Extreme Momentum", etc.
    val volumeStrength: String, // "180% Above Avg", "Institutional Accumulation"
    val marketRegime: String, // "Bull Market", "Trending Bullish", "High Volatility", etc.
    val lastUpdatedText: String, // "Live (200ms ago)"
    val reasonForTrade: List<String>, // Bullet points for AI explanation
    val timeframe: String = "5m / 15m",
    val sector: String = "F&O / NSE",
    val isHighConviction: Boolean = true
)
