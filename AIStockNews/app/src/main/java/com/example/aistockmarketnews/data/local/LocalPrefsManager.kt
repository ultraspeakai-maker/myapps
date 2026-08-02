package com.example.aistockmarketnews.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.aistockmarketnews.data.model.AIAlert
import com.example.aistockmarketnews.data.model.IntradayItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalPrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ai_stock_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
        private const val KEY_WATCHLIST = "watchlist"
        private const val KEY_ALERTS = "alerts"
    }

    var isDisclaimerAccepted: Boolean
        get() = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, value).apply()

    fun getWatchlist(): Set<String> {
        return prefs.getStringSet(KEY_WATCHLIST, emptySet()) ?: emptySet()
    }

    fun addToWatchlist(symbol: String) {
        val current = getWatchlist().toMutableSet()
        current.add(symbol)
        prefs.edit().putStringSet(KEY_WATCHLIST, current).apply()
    }

    fun removeFromWatchlist(symbol: String) {
        val current = getWatchlist().toMutableSet()
        current.remove(symbol)
        prefs.edit().putStringSet(KEY_WATCHLIST, current).apply()
    }

    fun getAlerts(): List<AIAlert> {
        val jsonStr = prefs.getString(KEY_ALERTS, null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<AIAlert>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAlerts(alerts: List<AIAlert>) {
        val jsonStr = Json.encodeToString(alerts)
        prefs.edit().putString(KEY_ALERTS, jsonStr).apply()
    }

    fun addAlert(alert: AIAlert) {
        val current = getAlerts().toMutableList()
        current.add(alert)
        saveAlerts(current)
    }

    fun removeAlert(alertId: String) {
        val current = getAlerts().filter { it.id != alertId }
        saveAlerts(current)
    }

    fun toggleAlert(alertId: String, isActive: Boolean) {
        val current = getAlerts().map {
            if (it.id == alertId) it.copy(isActive = isActive) else it
        }
        saveAlerts(current)
    }

    fun getTodayIntraday(): List<IntradayItem> {
        val jsonStr = prefs.getString("today_intraday", null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<IntradayItem>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveTodayIntraday(items: List<IntradayItem>) {
        val jsonStr = Json.encodeToString(items)
        prefs.edit().putString("today_intraday", jsonStr).apply()
    }

    fun getTomorrowIntraday(): List<IntradayItem> {
        val jsonStr = prefs.getString("tomorrow_intraday", null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<IntradayItem>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveTomorrowIntraday(items: List<IntradayItem>) {
        val jsonStr = Json.encodeToString(items)
        prefs.edit().putString("tomorrow_intraday", jsonStr).apply()
    }

    var lastIntradayShiftDate: String
        get() = prefs.getString("last_intraday_shift_date", "") ?: ""
        set(value) = prefs.edit().putString("last_intraday_shift_date", value).apply()

    fun getModelLeaderboard(): Map<String, Double> {
        val jsonStr = prefs.getString("model_leaderboard", null) ?: return emptyMap()
        return try {
            Json.decodeFromString<Map<String, Double>>(jsonStr)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveModelLeaderboard(leaderboard: Map<String, Double>) {
        val jsonStr = Json.encodeToString(leaderboard)
        prefs.edit().putString("model_leaderboard", jsonStr).apply()
    }
}
