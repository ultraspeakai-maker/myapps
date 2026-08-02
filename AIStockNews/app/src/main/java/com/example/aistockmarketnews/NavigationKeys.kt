package com.example.aistockmarketnews

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Main : NavKey

@Serializable
data class StockDetailKey(val symbol: String) : NavKey

@Serializable
data object WatchlistKey : NavKey

@Serializable
data object SmartMoneyKey : NavKey

@Serializable
data object AlertsKey : NavKey

@Serializable
data object NewsKey : NavKey

