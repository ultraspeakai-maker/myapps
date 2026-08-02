package com.example.aistockmarketnews.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Stock(
    val symbol: String,
    val name: String,
    val sector: String,
    val industry: String,
    val currentPrice: Double,
    val change: Double,
    val changePercent: Double,
    val isPositive: Boolean,
    val previousClose: Double
)

@Serializable
data class StockDetail(
    val symbol: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val previousClose: Double,
    val volume: Long,
    val marketCap: Double, // in Crores
    val pe: Double,
    val eps: Double,
    val dividendYield: Double,
    val description: String,
    val promoterHolding: Double, // percentage
    val mutualFundHolding: Double, // percentage
    val institutionHolding: Double, // percentage
    val riskMeterValue: Int, // 1 to 5 (Low to High Risk)
    val supportList: List<Double>,
    val resistanceList: List<Double>,
    val entryZoneMin: Double,
    val entryZoneMax: Double,
    val targetPrice: Double,
    val stopLoss: Double,
    val expectedTimeline: String = "5-10 Days"
)

@Serializable
data class HistoricalDataPoint(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

@Serializable
data class NewsItem(
    val id: String,
    val title: String,
    val source: String,
    val timeAgo: String,
    val summary: String,
    val sentiment: String, // "Positive", "Neutral", "Negative"
    val sentimentScore: Double, // 0.0 to 100.0
    val url: String = "https://news.google.com"
)

@Serializable
data class SmartMoneyRecord(
    val id: String,
    val stockSymbol: String,
    val stockName: String,
    val investorName: String,
    val holdingChangePercent: Double, // positive or negative
    val dateString: String,
    val confidenceScore: Int, // 0 to 100
    val type: String, // "Mutual Fund", "FII", "DII", "Promoter", "Large Investor"
    val action: String // "Buying" or "Selling"
)

@Serializable
data class AIAlert(
    val id: String,
    val symbol: String,
    val name: String,
    val type: String, // "Price Crosses", "RSI Above", "RSI Below", "Volume Spike"
    val targetValue: Double,
    val isActive: Boolean,
    val createdAt: Long
)

@Serializable
data class IntradayItem(
    val symbol: String,
    val price: Double,
    val changePercent: Double,
    val shortTermMovePct: Double,
    val trend15mText: String,
    val trend15mDesc: String,
    val prob15m: Int,
    val trend30mText: String,
    val trend30mDesc: String,
    val prob30m: Int,
    val trend60mText: String,
    val trend60mDesc: String,
    val prob60m: Int,
    val trendFullDayText: String = "Bullish",
    val trendFullDayDesc: String = "Full Day Trend",
    val probFullDay: Int = 80
)

@Serializable
data class DividendRecord(
    val id: String,
    val stockSymbol: String,
    val stockName: String,
    val exDate: String,
    val exDateMillis: Long,
    val recordDate: String,
    val dividendAmount: Double,
    val dividendType: String,
    val currentPrice: Double,
    val changePercent: Double,
    val aiInsight: String
)
