package com.example.aistockmarketnews.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aistockmarketnews.data.local.LocalPrefsManager
import com.example.aistockmarketnews.data.model.*
import com.example.aistockmarketnews.data.repository.StockSimulationEngine
import com.example.aistockmarketnews.data.repository.YahooFinanceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random
import com.example.aistockmarketnews.domain.TechnicalAnalysis


class StockViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = LocalPrefsManager(application.applicationContext)
    private val connectivityManager = application.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Network Connectivity State
    private val _isConnected = MutableStateFlow(isNetworkAvailable())
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // UI States (Disclaimer opens on every app launch)
    private val _isDisclaimerAccepted = MutableStateFlow(false)
    val isDisclaimerAccepted: StateFlow<Boolean> = _isDisclaimerAccepted.asStateFlow()

    private val _indices = MutableStateFlow<List<Stock>>(StockSimulationEngine.INDICES)
    val indices: StateFlow<List<Stock>> = _indices.asStateFlow()

    private val _stocks = MutableStateFlow<List<Stock>>(StockSimulationEngine.STOCKS)
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    private val _watchlist = MutableStateFlow<List<Stock>>(emptyList())
    val watchlist: StateFlow<List<Stock>> = _watchlist.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Stock>>(emptyList())
    val searchResults: StateFlow<List<Stock>> = _searchResults.asStateFlow()

    private val _selectedStock = MutableStateFlow<Stock?>(null)
    val selectedStock: StateFlow<Stock?> = _selectedStock.asStateFlow()

    private val _selectedStockDetail = MutableStateFlow<StockDetail?>(null)
    val selectedStockDetail: StateFlow<StockDetail?> = _selectedStockDetail.asStateFlow()

    private val _historicalData = MutableStateFlow<List<HistoricalDataPoint>>(emptyList())
    val historicalData: StateFlow<List<HistoricalDataPoint>> = _historicalData.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("1D")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _chartType = MutableStateFlow("Candlestick") // "Line", "Candlestick", "Area"
    val chartType: StateFlow<String> = _chartType.asStateFlow()

    private val _alerts = MutableStateFlow<List<AIAlert>>(emptyList())
    val alerts: StateFlow<List<AIAlert>> = _alerts.asStateFlow()

    private val _news = MutableStateFlow<List<NewsItem>>(StockSimulationEngine.getNews())
    val news: StateFlow<List<NewsItem>> = _news.asStateFlow()

    private val _smartMoney = MutableStateFlow<List<SmartMoneyRecord>>(StockSimulationEngine.getSmartMoneyRecords())
    val smartMoney: StateFlow<List<SmartMoneyRecord>> = _smartMoney.asStateFlow()

    // Flash states: maps symbol -> positive (true) or negative (false) price flash
    private val _priceFlashes = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val priceFlashes: StateFlow<Map<String, Boolean>> = _priceFlashes.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _todayIntradayStocks = MutableStateFlow<List<IntradayItem>>(emptyList())
    val todayIntradayStocks: StateFlow<List<IntradayItem>> = _todayIntradayStocks.asStateFlow()

    private val _tomorrowIntradayStocks = MutableStateFlow<List<IntradayItem>>(emptyList())
    val tomorrowIntradayStocks: StateFlow<List<IntradayItem>> = _tomorrowIntradayStocks.asStateFlow()

    private var lastIntradayTriggerMinute = -1
    private var tickRetrainCounter = 0

    init {
        registerNetworkCallback()
        loadWatchlist()
        loadAlerts()
        startRealtimeTicks()
        startYahooFinanceSync()
        startOneMinuteAutoRefreshLoop()
        initIntradayPicksInBackground()
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true
        }
    }

    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isConnected.value = true
                }

                override fun onLost(network: Network) {
                    _isConnected.value = isNetworkAvailable()
                }
            })
        } catch (e: Exception) {
            _isConnected.value = isNetworkAvailable()
        }
    }

    fun checkNetworkConnectivity() {
        val available = isNetworkAvailable()
        _isConnected.value = available
        if (available) {
            refreshLiveData()
        }
    }

    private fun startOneMinuteAutoRefreshLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(60_000L) // Refresh every 1 minute (60 seconds)
                if (_isConnected.value) {
                    syncAllPricesWithYahooFinance()
                    _news.value = StockSimulationEngine.getNews()
                }
            }
        }
    }

    private fun initIntradayPicksInBackground() {
        viewModelScope.launch(Dispatchers.Default) {
            val tz = java.util.TimeZone.getTimeZone("Asia/Kolkata")
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply { timeZone = tz }
            val currentDateStr = sdf.format(java.util.Date())

            val storedToday = prefsManager.getTodayIntraday()
            val storedTomorrow = prefsManager.getTomorrowIntraday()

            if (storedToday.isEmpty()) {
                val initialToday = generateFreshIntradayPicks()
                prefsManager.saveTodayIntraday(initialToday)
                _todayIntradayStocks.value = initialToday
            } else {
                _todayIntradayStocks.value = storedToday
            }
            _tomorrowIntradayStocks.value = storedTomorrow

            checkAndShiftIntraday(currentDateStr)
        }
    }

    fun refreshLiveData() {
        viewModelScope.launch(Dispatchers.Default) {
            _isRefreshing.value = true
            syncAllPricesWithYahooFinance()
            delay(400)
            _isRefreshing.value = false
        }
    }

    private suspend fun syncAllPricesWithYahooFinance() {
        val allSymbols = (_indices.value.map { it.symbol } + _stocks.value.map { it.symbol }).distinct()
        val fetchedMap = YahooFinanceService.fetchBatchStockPrices(allSymbols)

        if (fetchedMap.isNotEmpty()) {
            val updatedFlashes = mutableMapOf<String, Boolean>()

            _indices.value = _indices.value.map { stock ->
                val res = fetchedMap[stock.symbol]
                if (res != null) {
                    StockSimulationEngine.updatePriceFromYahoo(
                        symbol = stock.symbol,
                        currentPrice = res.currentPrice,
                        previousClose = res.previousClose,
                        open = res.open,
                        high = res.high,
                        low = res.low,
                        volume = res.volume
                    )
                    val change = res.currentPrice - res.previousClose
                    val changePercent = change / res.previousClose * 100.0
                    if (res.currentPrice != stock.currentPrice) {
                        updatedFlashes[stock.symbol] = res.currentPrice > stock.currentPrice
                    }
                    stock.copy(
                        currentPrice = res.currentPrice,
                        previousClose = res.previousClose,
                        change = Math.round(change * 100.0) / 100.0,
                        changePercent = Math.round(changePercent * 100.0) / 100.0,
                        isPositive = change >= 0
                    )
                } else stock
            }

            _stocks.value = _stocks.value.map { stock ->
                val res = fetchedMap[stock.symbol]
                if (res != null) {
                    StockSimulationEngine.updatePriceFromYahoo(
                        symbol = stock.symbol,
                        currentPrice = res.currentPrice,
                        previousClose = res.previousClose,
                        open = res.open,
                        high = res.high,
                        low = res.low,
                        volume = res.volume
                    )
                    val change = res.currentPrice - res.previousClose
                    val changePercent = change / res.previousClose * 100.0
                    if (res.currentPrice != stock.currentPrice) {
                        updatedFlashes[stock.symbol] = res.currentPrice > stock.currentPrice
                    }
                    stock.copy(
                        currentPrice = res.currentPrice,
                        previousClose = res.previousClose,
                        change = Math.round(change * 100.0) / 100.0,
                        changePercent = Math.round(changePercent * 100.0) / 100.0,
                        isPositive = change >= 0
                    )
                } else stock
            }

            if (updatedFlashes.isNotEmpty()) {
                _priceFlashes.value = updatedFlashes
            }

            // Sync selected stock details panel
            val selected = _selectedStock.value
            if (selected != null) {
                val matching = (_stocks.value + _indices.value).find { it.symbol == selected.symbol }
                if (matching != null) {
                    _selectedStock.value = matching
                    val currentDetail = _selectedStockDetail.value
                    if (currentDetail != null && currentDetail.symbol == matching.symbol) {
                        _selectedStockDetail.value = currentDetail.copy(
                            open = matching.currentPrice * 0.998,
                            high = Math.max(currentDetail.high, matching.currentPrice),
                            low = Math.min(currentDetail.low, matching.currentPrice)
                        )
                    }
                }
            }

            loadWatchlist()
            if (_searchQuery.value.isNotBlank()) {
                onSearchQueryChanged(_searchQuery.value)
            }
        }
    }


    // Disclaimer
    fun acceptDisclaimer() {
        prefsManager.isDisclaimerAccepted = true
        _isDisclaimerAccepted.value = true
    }

    // Watchlist Management
    private fun loadWatchlist() {
        val watchedSymbols = prefsManager.getWatchlist()
        val all = _stocks.value
        val list = all.filter { watchedSymbols.contains(it.symbol) }
        _watchlist.value = list
    }

    fun toggleWatchlist(symbol: String) {
        val current = prefsManager.getWatchlist()
        if (current.contains(symbol)) {
            prefsManager.removeFromWatchlist(symbol)
        } else {
            prefsManager.addToWatchlist(symbol)
        }
        loadWatchlist()
    }

    fun isWatched(symbol: String): Boolean {
        return prefsManager.getWatchlist().contains(symbol)
    }

    // Search Management
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            _searchResults.value = _stocks.value.filter {
                it.symbol.contains(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true) ||
                it.sector.contains(query, ignoreCase = true)
            }
        }
    }

    // Select Stock and Details
    fun selectStock(symbol: String) {
        val stock = (_stocks.value + _indices.value).find { it.symbol == symbol } ?: return
        _selectedStock.value = stock
        val detail = StockSimulationEngine.getStockDetail(symbol)
        
        // Feed updated prices to stock detail
        val livePrice = StockSimulationEngine.getCurrentPrice(symbol)
        val updatedDetail = detail.copy(
            open = if (livePrice > detail.previousClose) livePrice * 0.995 else livePrice * 1.005,
            high = Math.max(detail.high, livePrice),
            low = Math.min(detail.low, livePrice)
        )
        _selectedStockDetail.value = updatedDetail

        loadHistoricalData(symbol, _selectedTimeframe.value)
    }

    fun setTimeframe(timeframe: String) {
        _selectedTimeframe.value = timeframe
        _selectedStock.value?.let {
            loadHistoricalData(it.symbol, timeframe)
        }
    }

    fun setChartType(type: String) {
        _chartType.value = type
    }

    private fun loadHistoricalData(symbol: String, timeframe: String) {
        viewModelScope.launch {
            val history = YahooFinanceService.fetchHistoricalData(symbol, timeframe)
            val finalHistory = if (history.isNotEmpty()) {
                history
            } else {
                StockSimulationEngine.generateHistoricalData(symbol, timeframe)
            }
            _historicalData.value = finalHistory
        }
    }

    // Alerts Management
    private fun loadAlerts() {
        _alerts.value = prefsManager.getAlerts()
    }

    fun createAlert(symbol: String, type: String, value: Double) {
        val stockName = (_stocks.value + _indices.value).find { it.symbol == symbol }?.name ?: symbol
        val alert = AIAlert(
            id = UUID.randomUUID().toString(),
            symbol = symbol,
            name = stockName,
            type = type,
            targetValue = value,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        prefsManager.addAlert(alert)
        loadAlerts()
    }

    fun deleteAlert(id: String) {
        prefsManager.removeAlert(id)
        loadAlerts()
    }

    fun toggleAlert(id: String, isActive: Boolean) {
        prefsManager.toggleAlert(id, isActive)
        loadAlerts()
    }

    // Realtime ticks collector
    private fun startRealtimeTicks() {
        viewModelScope.launch {
            StockSimulationEngine.getRealtimeTickStream().collectLatest { updates ->
                val tz = java.util.TimeZone.getTimeZone("Asia/Kolkata")
                val cal = java.util.Calendar.getInstance(tz)
                val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                val minute = cal.get(java.util.Calendar.MINUTE)
                val timeInMinutes = hour * 60 + minute
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply { timeZone = tz }
                val currentDateStr = sdf.format(java.util.Date())

                checkAndShiftIntraday(currentDateStr)

                // Trigger tomorrow forecast updates at 2:30 PM and 3:00 PM when live
                val isMarketLive = StockSimulationEngine.isMarketOpen()
                if (isMarketLive) {
                    if (timeInMinutes == 14 * 60 + 30 || timeInMinutes == 15 * 60 + 0) {
                        if (lastIntradayTriggerMinute != timeInMinutes) {
                            lastIntradayTriggerMinute = timeInMinutes
                            updateTomorrowIntradayPicks()
                        }
                    }
                }
                // Update Indices
                _indices.value = _indices.value.map { stock ->
                    val newPrice = updates[stock.symbol]
                    if (newPrice != null) {
                        val isUp = newPrice > stock.currentPrice
                        val change = newPrice - stock.previousClose
                        val changePercent = change / stock.previousClose * 100.0
                        
                        triggerFlash(stock.symbol, isUp)

                        stock.copy(
                            currentPrice = newPrice,
                            change = Math.round(change * 100.0) / 100.0,
                            changePercent = Math.round(changePercent * 100.0) / 100.0,
                            isPositive = change >= 0
                        )
                    } else stock
                }

                // Update Stocks
                _stocks.value = _stocks.value.map { stock ->
                    val newPrice = updates[stock.symbol]
                    if (newPrice != null) {
                        val isUp = newPrice > stock.currentPrice
                        val change = newPrice - stock.previousClose
                        val changePercent = change / stock.previousClose * 100.0

                        triggerFlash(stock.symbol, isUp)

                        stock.copy(
                            currentPrice = newPrice,
                            change = Math.round(change * 100.0) / 100.0,
                            changePercent = Math.round(changePercent * 100.0) / 100.0,
                            isPositive = change >= 0
                        )
                    } else stock
                }

                // Sync Watchlist
                loadWatchlist()

                // Sync Search results if active
                if (_searchQuery.value.isNotBlank()) {
                    _searchResults.value = _stocks.value.filter {
                        it.symbol.contains(_searchQuery.value, ignoreCase = true) ||
                        it.name.contains(_searchQuery.value, ignoreCase = true)
                    }
                }

                // Check Alerts Trigger
                checkAlerts(updates)

                // Update Selected Stock if matching
                val selected = _selectedStock.value
                if (selected != null && updates.containsKey(selected.symbol)) {
                    val updatedPrice = updates[selected.symbol]!!
                    val isUp = updatedPrice > selected.currentPrice
                    val change = updatedPrice - selected.previousClose
                    val changePercent = change / selected.previousClose * 100.0

                    _selectedStock.value = selected.copy(
                        currentPrice = updatedPrice,
                        change = Math.round(change * 100.0) / 100.0,
                        changePercent = Math.round(changePercent * 100.0) / 100.0,
                        isPositive = change >= 0
                    )

                    // Also adjust detail
                    _selectedStockDetail.value?.let { detail ->
                        _selectedStockDetail.value = detail.copy(
                            high = Math.max(detail.high, updatedPrice),
                            low = Math.min(detail.low, updatedPrice)
                        )
                    }

                    // Update the last data point in the historical data list in real-time!
                    val currentHistory = _historicalData.value.toMutableList()
                    if (currentHistory.isNotEmpty()) {
                        val lastPoint = currentHistory.last()
                        val updatedPoint = lastPoint.copy(
                            close = updatedPrice,
                            high = Math.max(lastPoint.high, updatedPrice),
                            low = Math.min(lastPoint.low, updatedPrice)
                        )
                        currentHistory[currentHistory.lastIndex] = updatedPoint
                        _historicalData.value = currentHistory
                    }
                }
            }
        }
    }

    private fun triggerFlash(symbol: String, isUp: Boolean) {
        val currentFlashes = _priceFlashes.value.toMutableMap()
        currentFlashes[symbol] = isUp
        _priceFlashes.value = currentFlashes
        
        // Remove flash after a delay to clear animation highlight
        viewModelScope.launch {
            delay(800)
            val updated = _priceFlashes.value.toMutableMap()
            updated.remove(symbol)
            _priceFlashes.value = updated
        }
    }

    private fun checkAlerts(updates: Map<String, Double>) {
        val currentAlerts = prefsManager.getAlerts()
        var changed = false
        currentAlerts.forEach { alert ->
            if (alert.isActive) {
                val newPrice = updates[alert.symbol]
                if (newPrice != null) {
                    val triggered = when (alert.type) {
                        "Price Crosses" -> {
                            // Simple alert check: if price crosses target
                            // In simulation, we check if it is very close or crossed
                            val currentStock = (_stocks.value + _indices.value).find { it.symbol == alert.symbol }
                            val oldPrice = currentStock?.currentPrice ?: newPrice
                            (oldPrice <= alert.targetValue && newPrice >= alert.targetValue) ||
                            (oldPrice >= alert.targetValue && newPrice <= alert.targetValue)
                        }
                        else -> false
                    }

                    if (triggered) {
                        // Deactivate alert
                        prefsManager.toggleAlert(alert.id, false)
                        changed = true
                    }
                }
            }
        }
        if (changed) {
            loadAlerts()
        }
    }

    private fun startYahooFinanceSync() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                syncAllPricesWithYahooFinance()

                val isLive = StockSimulationEngine.isMarketOpen()
                val sleepInterval = if (isLive) 5000L else 30000L
                delay(sleepInterval)
            }
        }
    }

    fun checkAndShiftIntraday(currentDateStr: String) {
        val lastShift = prefsManager.lastIntradayShiftDate
        if (currentDateStr != lastShift) {
            val tomorrowList = prefsManager.getTomorrowIntraday()
            if (tomorrowList.isNotEmpty()) {
                prefsManager.saveTodayIntraday(tomorrowList)
            } else {
                val initialList = generateFreshIntradayPicks()
                prefsManager.saveTodayIntraday(initialList)
            }
            prefsManager.saveTomorrowIntraday(emptyList())
            prefsManager.lastIntradayShiftDate = currentDateStr
            
            _todayIntradayStocks.value = prefsManager.getTodayIntraday()
            _tomorrowIntradayStocks.value = emptyList()
        }
    }

    private fun generateFreshIntradayPicks(): List<IntradayItem> {
        val allList = _stocks.value + _indices.value
        return allList.mapNotNull { stock ->
            val sym = stock.symbol
            val rand = Random(sym.hashCode() + 777)
            val isBullish = stock.changePercent >= 0.0
            val shortTermMovePct = if (isBullish) {
                3.0 + (rand.nextDouble() * 4.0)
            } else {
                -(3.0 + (rand.nextDouble() * 4.0))
            }
            
            val history = StockSimulationEngine.generateHistoricalData(sym, "1D")
            val (patternName, _) = TechnicalAnalysis.detectPatternAndProbability(history)
            
            IntradayItem(
                symbol = sym,
                price = stock.currentPrice,
                changePercent = stock.changePercent,
                shortTermMovePct = shortTermMovePct,
                trend15mText = if (isBullish) "Bullish" else "Bearish",
                trend15mDesc = if (patternName != "No Pattern") patternName else (if (isBullish) "Bull Flag" else "Bear Flag"),
                prob15m = (65 + abs(stock.changePercent) * 8).toInt().coerceIn(55, 95),
                trend30mText = if (isBullish) "Bullish" else "Bearish",
                trend30mDesc = if (patternName != "No Pattern") patternName else (if (isBullish) "Ascending Tri" else "Descending Tri"),
                prob30m = (60 + abs(stock.changePercent) * 7).toInt().coerceIn(55, 95),
                trend60mText = if (isBullish) "Bullish" else "Bearish",
                trend60mDesc = if (patternName != "No Pattern") patternName else (if (isBullish) "Cup & Handle" else "Triple Top"),
                prob60m = (62 + abs(stock.changePercent) * 6).toInt().coerceIn(55, 95),
                trendFullDayText = if (isBullish) "Bullish" else "Bearish",
                trendFullDayDesc = if (patternName != "No Pattern") patternName else (if (isBullish) "Channel Breakout" else "Channel Breakdown"),
                probFullDay = (64 + abs(stock.changePercent) * 7).toInt().coerceIn(55, 95)
            )
        }
        .sortedByDescending { it.prob30m }
        .take(20)
    }

    private fun updateTomorrowIntradayPicks() {
        viewModelScope.launch(Dispatchers.Default) {
            val allList = _stocks.value + _indices.value
            val tomorrowPicks = allList.mapNotNull { stock ->
                val sym = stock.symbol
                val rand = Random(sym.hashCode() + 999)
                val isBullish = stock.changePercent >= 0.0
                
                val shortTermMovePct = if (isBullish) {
                    3.0 + (rand.nextDouble() * 5.0)
                } else {
                    -(3.0 + (rand.nextDouble() * 5.0))
                }
                
                val history = StockSimulationEngine.generateHistoricalData(sym, "1D")
                val (patternName, _) = TechnicalAnalysis.detectPatternAndProbability(history)
                
                IntradayItem(
                    symbol = sym,
                    price = stock.currentPrice,
                    changePercent = stock.changePercent,
                    shortTermMovePct = shortTermMovePct,
                    trend15mText = if (isBullish) "Bullish" else "Bearish",
                    trend15mDesc = if (patternName != "No Pattern") patternName else (if (isBullish) "Bull Flag" else "Bear Flag"),
                    prob15m = (65 + abs(stock.changePercent) * 8).toInt().coerceIn(55, 95),
                    trend30mText = if (isBullish) "Bullish" else "Bearish",
                    trend30mDesc = if (patternName != "No Pattern") patternName else (if (isBullish) "Ascending Tri" else "Descending Tri"),
                    prob30m = (60 + abs(stock.changePercent) * 7).toInt().coerceIn(55, 95),
                    trend60mText = if (isBullish) "Bullish" else "Bearish",
                    trend60mDesc = if (patternName != "No Pattern") patternName else (if (isBullish) "Cup & Handle" else "Triple Top"),
                    prob60m = (62 + abs(stock.changePercent) * 6).toInt().coerceIn(55, 95),
                    trendFullDayText = if (isBullish) "Bullish" else "Bearish",
                    trendFullDayDesc = if (patternName != "No Pattern") patternName else (if (isBullish) "Channel Breakout" else "Channel Breakdown"),
                    probFullDay = (64 + abs(stock.changePercent) * 7).toInt().coerceIn(55, 95)
                )
            }
            .sortedByDescending { it.prob30m }
            .take(20)
            
            prefsManager.saveTomorrowIntraday(tomorrowPicks)
            _tomorrowIntradayStocks.value = tomorrowPicks
        }
    }
}

