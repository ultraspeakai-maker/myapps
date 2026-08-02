package com.example.aistockmarketnews.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.example.aistockmarketnews.data.model.HistoricalDataPoint

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

object YahooFinanceService {
    private const val TAG = "YahooFinanceService"

    private fun getYahooSymbol(symbol: String): String {
        val cleanSymbol = symbol.substringBefore("_").trim()
        return when (cleanSymbol) {
            "NIFTY 50" -> "^NSEI"
            "SENSEX" -> "^BSESN"
            "BANK NIFTY" -> "^NSEBANK"
            "FINNIFTY" -> "^CNXFIN"
            "M&M" -> "M%26M.NS"
            "BOB" -> "BANKBARODA.NS"
            "HPCL" -> "HINDPETRO.NS"
            "PIDILITE" -> "PIDILITIND.NS"
            "BLUESTAR" -> "BLUESTARCO.NS"
            "ROUTEMOBILE" -> "ROUTE.NS"
            "BAJAJ-AUTO" -> "BAJAJ-AUTO.NS"
            "TATAMOTORS" -> "TATAMOTORS.NS"
            "LTIM" -> "LTIM.NS"
            "NESTLEIND" -> "NESTLEIND.NS"
            "MARICO" -> "MARICO.NS"
            "EICHERMOT" -> "EICHERMOT.NS"
            else -> if (cleanSymbol.contains(".")) cleanSymbol else "${cleanSymbol.replace("&", "%26")}.NS"
        }
    }

    suspend fun fetchBatchStockPrices(symbols: List<String>): Map<String, StockPriceResult> = coroutineScope {
        val results = java.util.concurrent.ConcurrentHashMap<String, StockPriceResult>()
        symbols.map { sym ->
            async(Dispatchers.IO) {
                val priceRes = fetchStockPrice(sym)
                if (priceRes != null) {
                    results[sym] = priceRes
                }
            }
        }.awaitAll()
        results
    }

    suspend fun fetchStockPrice(symbol: String): StockPriceResult? = withContext(Dispatchers.IO) {
        val yahooSymbol = getYahooSymbol(symbol)
        val urlStr = "https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol?interval=1d&range=1d"
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            connection.setRequestProperty("Accept", "*/*")
            connection.setRequestProperty("Connection", "keep-alive")
            connection.connectTimeout = 4000
            connection.readTimeout = 4000

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val rootJson = JSONObject(responseText)
                val chart = rootJson.getJSONObject("chart")
                val resultArr = chart.getJSONArray("result")
                if (resultArr.length() > 0) {
                    val result = resultArr.getJSONObject(0)
                    val meta = result.getJSONObject("meta")
                    val price = if (meta.has("regularMarketPrice") && !meta.isNull("regularMarketPrice")) {
                        meta.getDouble("regularMarketPrice")
                    } else if (meta.has("chartPreviousClose") && !meta.isNull("chartPreviousClose")) {
                        meta.getDouble("chartPreviousClose")
                    } else if (meta.has("previousClose") && !meta.isNull("previousClose")) {
                        meta.getDouble("previousClose")
                    } else return@withContext null

                    val prevClose = if (meta.has("chartPreviousClose") && !meta.isNull("chartPreviousClose")) {
                        meta.getDouble("chartPreviousClose")
                    } else if (meta.has("previousClose") && !meta.isNull("previousClose")) {
                        meta.getDouble("previousClose")
                    } else price

                    val high = meta.optDouble("regularMarketDayHigh", price)
                    val low = meta.optDouble("regularMarketDayLow", price)
                    val open = meta.optDouble("regularMarketOpen", price)
                    val volume = meta.optLong("regularMarketVolume", 0L)

                    return@withContext StockPriceResult(
                        symbol = symbol,
                        currentPrice = price,
                        previousClose = prevClose,
                        open = open,
                        high = high,
                        low = low,
                        volume = volume
                    )
                }
            }
        } catch (e: Exception) {
            // Silently swallow network errors during rapid polling
        }
        return@withContext null
    }

    suspend fun fetchHistoricalData(
        symbol: String,
        range: String
    ): List<HistoricalDataPoint> = withContext(Dispatchers.IO) {
        delay(150)
        val yahooSymbol = getYahooSymbol(symbol)
        
        val (interval, apiRange) = when (range) {
            "1D" -> Pair("5m", "1d")
            "1W" -> Pair("15m", "5d")
            "1M" -> Pair("1h", "1mo")
            "1Y" -> Pair("1d", "1y")
            "5Y" -> Pair("1wk", "5y")
            "ALL" -> Pair("1mo", "max")
            else -> Pair("1d", "1y")
        }

        val urlStr = "https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol?interval=$interval&range=$apiRange"
        val points = mutableListOf<HistoricalDataPoint>()
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val rootJson = JSONObject(responseText)
                val chart = rootJson.getJSONObject("chart")
                val resultArr = chart.getJSONArray("result")
                if (resultArr.length() > 0) {
                    val result = resultArr.getJSONObject(0)
                    val timestamps = result.getJSONArray("timestamp")
                    val indicators = result.getJSONObject("indicators")
                    val quoteArr = indicators.getJSONArray("quote")
                    if (quoteArr.length() > 0) {
                        val quote = quoteArr.getJSONObject(0)
                        val opens = quote.getJSONArray("open")
                        val highs = quote.getJSONArray("high")
                        val lows = quote.getJSONArray("low")
                        val closes = quote.getJSONArray("close")
                        val volumes = quote.getJSONArray("volume")

                        for (i in 0 until timestamps.length()) {
                            if (closes.isNull(i) || opens.isNull(i) || highs.isNull(i) || lows.isNull(i)) continue
                            val timeSec = timestamps.getLong(i)
                            points.add(
                                HistoricalDataPoint(
                                    timestamp = timeSec * 1000L,
                                    open = opens.getDouble(i),
                                    high = highs.getDouble(i),
                                    low = lows.getDouble(i),
                                    close = closes.getDouble(i),
                                    volume = volumes.optDouble(i, 0.0)
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching historical data for $symbol: " + Log.getStackTraceString(e))
        }
        return@withContext points
    }
}

data class StockPriceResult(
    val symbol: String,
    val currentPrice: Double,
    val previousClose: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val volume: Long
)
