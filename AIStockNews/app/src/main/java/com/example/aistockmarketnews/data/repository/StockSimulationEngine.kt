package com.example.aistockmarketnews.data.repository

import com.example.aistockmarketnews.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

object StockSimulationEngine {

    val INDICES = listOf(
        Stock("NIFTY 50", "NIFTY 50 Index", "Index", "Market Index", 24156.00, -50.90, -0.21, false, 24206.90),
        Stock("SENSEX", "BSE SENSEX Index", "Index", "Market Index", 79929.37, 2359.98, 3.04, true, 77569.39),
        Stock("BANK NIFTY", "Nifty Bank Index", "Index", "Market Index", 57813.45, -232.45, -0.40, false, 58045.90),
        Stock("FINNIFTY", "Nifty Financial Services Index", "Index", "Market Index", 24100.00, -20.00, -0.08, false, 24120.00)
    )

    val STOCKS = Top500Stocks.STOCKS_500

    private val stockDetailsCache = mutableMapOf<String, StockDetail>()
    private val activePrices = mutableMapOf<String, Double>()

    private var fetchedHolidays = setOf<String>()
    private var isFetchingHolidays = false

    init {
        STOCKS.forEach { activePrices[it.symbol] = it.currentPrice }
        INDICES.forEach { activePrices[it.symbol] = it.currentPrice }
        fetchHolidaysFromNetwork()
    }

    fun fetchHolidaysFromNetwork() {
        if (isFetchingHolidays) return
        isFetchingHolidays = true
        Thread {
            try {
                val url = java.net.URL("https://api.upstox.com/v2/market/holidays")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("Accept", "application/json")
                
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(text)
                    if (json.optString("status") == "success") {
                        val dataArray = json.optJSONArray("data")
                        val dates = mutableSetOf<String>()
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val item = dataArray.getJSONObject(i)
                                val type = item.optString("holiday_type")
                                val date = item.optString("date")
                                val closedExchanges = item.optJSONArray("closed_exchanges")
                                var isNseClosed = false
                                if (closedExchanges != null) {
                                    for (j in 0 until closedExchanges.length()) {
                                        val ex = closedExchanges.optString(j)
                                        if (ex == "NSE" || ex == "BSE" || ex == "NFO" || ex == "BFO") {
                                            isNseClosed = true
                                        }
                                    }
                                }
                                if (type == "TRADING_HOLIDAY" && isNseClosed && date.isNotEmpty()) {
                                    dates.add(date)
                                }
                            }
                        }
                        if (dates.isNotEmpty()) {
                            fetchedHolidays = dates
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isFetchingHolidays = false
            }
        }.start()
    }

    fun updatePriceFromYahoo(
        symbol: String,
        currentPrice: Double,
        previousClose: Double,
        open: Double,
        high: Double,
        low: Double,
        volume: Long
    ) {
        activePrices[symbol] = currentPrice
        val cached = stockDetailsCache[symbol]
        val updatedDetail = if (cached != null) {
            cached.copy(
                open = open,
                high = high,
                low = low,
                previousClose = previousClose,
                volume = volume
            )
        } else {
            val newDetail = getStockDetail(symbol)
            newDetail.copy(
                open = open,
                high = high,
                low = low,
                previousClose = previousClose,
                volume = volume
            )
        }
        stockDetailsCache[symbol] = updatedDetail
    }

    fun isHoliday(time: java.util.Calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"))): Boolean {
        // Formatter for yyyy-MM-dd
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
        val dateString = sdf.format(time.time)

        // If we successfully loaded live holidays from network, check the set
        if (fetchedHolidays.isNotEmpty()) {
            return fetchedHolidays.contains(dateString)
        }

        // Offline Fallback Calendar for 2026
        val year = time.get(java.util.Calendar.YEAR)
        val month = time.get(java.util.Calendar.MONTH) + 1 // 1-indexed
        val day = time.get(java.util.Calendar.DAY_OF_MONTH)
        
        if (year == 2026) {
            when (month) {
                1 -> if (day == 26) return true // Republic Day
                3 -> if (day == 4) return true  // Holi
                4 -> if (day == 3 || day == 14) return true // Good Friday, Ambedkar Jayanti
                5 -> if (day == 1 || day == 27) return true // Maharashtra Day, Bakri Id
                6 -> if (day == 26) return true // Muharram
                9 -> if (day == 4) return true  // Eid-e-Milad
                10 -> if (day == 2 || day == 21) return true // Gandhi Jayanti, Dussehra
                11 -> if (day == 24) return true // Gurunanak Jayanti
                12 -> if (day == 25) return true // Christmas
            }
        }
        return false
    }

    fun isTomorrowHoliday(): Boolean {
        val tz = java.util.TimeZone.getTimeZone("Asia/Kolkata")
        val cal = java.util.Calendar.getInstance(tz)
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
        return isHoliday(cal)
    }

    fun isMarketOpen(): Boolean {
        val tz = java.util.TimeZone.getTimeZone("Asia/Kolkata")
        val cal = java.util.Calendar.getInstance(tz)
        if (isHoliday(cal)) {
            return false
        }
        val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
        if (dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY) {
            return false
        }
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = cal.get(java.util.Calendar.MINUTE)
        val timeInMinutes = hour * 60 + minute
        val marketOpenMinutes = 9 * 60 + 15
        val marketCloseMinutes = 15 * 60 + 30
        return timeInMinutes in marketOpenMinutes..marketCloseMinutes
    }

    // High fidelity real-time tick stream using Kotlin Flow
    fun getRealtimeTickStream(): Flow<Map<String, Double>> = flow {
        // Emit initial snapshot immediately for instant startup live price render
        emit(activePrices.toMap())
        while (true) {
            delay(5000)
            emit(activePrices.toMap())
        }
    }

    fun getCurrentPrice(symbol: String): Double {
        return activePrices[symbol] ?: 100.0
    }

    fun getStockDetail(symbol: String): StockDetail {
        return stockDetailsCache.getOrPut(symbol) {
            val stock = (STOCKS + INDICES).find { it.symbol == symbol } ?: STOCKS[0]
            val rand = Random(symbol.hashCode())
            
            val prevClose = stock.currentPrice - stock.change
            val open = prevClose + (rand.nextDouble() * 10 - 5)
            val high = Math.max(open, stock.currentPrice) + rand.nextDouble() * 15
            val low = Math.min(open, stock.currentPrice) - rand.nextDouble() * 15

            val vol = 1000000L + rand.nextLong(15000000L)
            val mCap = 25000.0 + rand.nextDouble() * 1800000.0 // in Crores
            val pe = 12.0 + rand.nextDouble() * 70.0
            val eps = stock.currentPrice / pe * (0.8 + rand.nextDouble() * 0.4)
            val divYield = rand.nextDouble() * 3.5

            val supportList = listOf(
                Math.round((stock.currentPrice * 0.96) * 10.0) / 10.0,
                Math.round((stock.currentPrice * 0.93) * 10.0) / 10.0,
                Math.round((stock.currentPrice * 0.90) * 10.0) / 10.0
            ).sortedDescending()

            val resistanceList = listOf(
                Math.round((stock.currentPrice * 1.03) * 10.0) / 10.0,
                Math.round((stock.currentPrice * 1.06) * 10.0) / 10.0,
                Math.round((stock.currentPrice * 1.09) * 10.0) / 10.0
            ).sorted()

            StockDetail(
                symbol = symbol,
                open = Math.round(open * 100.0) / 100.0,
                high = Math.round(high * 100.0) / 100.0,
                low = Math.round(low * 100.0) / 100.0,
                previousClose = Math.round(prevClose * 100.0) / 100.0,
                volume = vol,
                marketCap = Math.round(mCap * 10.0) / 10.0,
                pe = Math.round(pe * 100.0) / 100.0,
                eps = Math.round(eps * 100.0) / 100.0,
                dividendYield = Math.round(divYield * 100.0) / 100.0,
                description = generateCompanyProfile(stock),
                promoterHolding = 40.0 + rand.nextDouble() * 35.0,
                mutualFundHolding = 5.0 + rand.nextDouble() * 15.0,
                institutionHolding = 10.0 + rand.nextDouble() * 25.0,
                riskMeterValue = rand.nextInt(1, 6), // 1 to 5
                supportList = supportList,
                resistanceList = resistanceList,
                entryZoneMin = Math.round((stock.currentPrice * 0.97) * 100.0) / 100.0,
                entryZoneMax = Math.round((stock.currentPrice * 0.995) * 100.0) / 100.0,
                targetPrice = Math.round((stock.currentPrice * 1.12) * 100.0) / 100.0,
                stopLoss = Math.round((stock.currentPrice * 0.94) * 100.0) / 100.0,
                expectedTimeline = "${rand.nextInt(3, 16)} Days"
            )
        }
    }

    private fun generateCompanyProfile(stock: Stock): String {
        val name = stock.name
        val sector = stock.sector
        val symbol = stock.symbol

        return when {
            sector.equals("IT", ignoreCase = true) || sector.contains("Tech", ignoreCase = true) || sector.contains("Information", ignoreCase = true) ->
                "$name ($symbol) is a major technology services and software solution provider headquartered in India. The enterprise specializes in digital transformation, cloud computing, artificial intelligence, and enterprise IT operations for corporate clients worldwide."
            sector.equals("Banking", ignoreCase = true) || sector.contains("Finance", ignoreCase = true) || sector.contains("Financial", ignoreCase = true) ->
                "$name ($symbol) is a premier Indian financial services and banking institution. It offers comprehensive retail banking, commercial credit, treasury services, digital payments, and wealth management solutions across India."
            sector.contains("Energy", ignoreCase = true) || sector.contains("Oil", ignoreCase = true) || sector.contains("Power", ignoreCase = true) ->
                "$name ($symbol) is a leading energy and power industrial corporate in India, operating across fuel refining, thermal & renewable power generation, and natural resource distribution."
            sector.contains("Auto", ignoreCase = true) || sector.contains("Vehicle", ignoreCase = true) ->
                "$name ($symbol) is an established Indian automotive manufacturer producing passenger vehicles, commercial fleets, two-wheelers, and next-generation electric mobility solutions."
            sector.contains("Pharma", ignoreCase = true) || sector.contains("Health", ignoreCase = true) ->
                "$name ($symbol) is a major pharmaceutical firm in India engaged in generic drug formulations, active pharmaceutical ingredients (APIs), biological research, and healthcare exports."
            sector.contains("FMCG", ignoreCase = true) || sector.contains("Consumer", ignoreCase = true) ->
                "$name ($symbol) is a prominent fast-moving consumer goods enterprise in India, manufacturing and distributing household essentials, packaged foods, and personal care products."
            sector.contains("Metals", ignoreCase = true) || sector.contains("Steel", ignoreCase = true) || sector.contains("Mining", ignoreCase = true) ->
                "$name ($symbol) is a foundational metal and mining company in India, producing high-grade steel, industrial alloys, and processed minerals for construction and manufacturing industries."
            sector.contains("Infra", ignoreCase = true) || sector.contains("Construct", ignoreCase = true) || sector.contains("Cement", ignoreCase = true) ->
                "$name ($symbol) is a core infrastructure and building materials player executing large-scale engineering, highway construction, real estate, and industrial projects."
            sector.contains("Telecom", ignoreCase = true) ->
                "$name ($symbol) is a key telecommunications provider in India, offering nationwide 5G/4G wireless network coverage, fiber broadband, and digital enterprise infrastructure."
            else ->
                "$name ($symbol) is a well-established commercial enterprise operating within the $sector sector in India. The company delivers strong market fundamentals, product innovation, and significant industry footprint."
        }
    }

    // Seed-based reproducible historical data generator
    fun generateHistoricalData(symbol: String, timeframe: String): List<HistoricalDataPoint> {
        val rand = java.util.Random((symbol.hashCode() + timeframe.hashCode()).toLong())
        val points = mutableListOf<HistoricalDataPoint>()
        
        val count = when (timeframe) {
            "1D" -> 78 // 5m intervals in 6.5 hours trading session
            "1W" -> 100
            "1M" -> 120
            "1Y" -> 250
            "5Y" -> 500
            "ALL" -> 800
            else -> 100
        }

        val scale = when (timeframe) {
            "1D" -> 0.002
            "1W" -> 0.01
            "1M" -> 0.04
            "1Y" -> 0.09
            "5Y" -> 0.2
            "ALL" -> 0.4
            else -> 0.02
        }

        var price = activePrices[symbol] ?: 100.0
        // Move backward in time
        var currentPrice = price
        val intervalMs = when (timeframe) {
            "1D" -> 300000L      // 5 Minutes
            "1W" -> 900000L      // 15 Minutes
            "1M" -> 3600000L     // 1 Hour
            "1Y" -> 86400000L    // 1 Day
            "5Y" -> 604800000L   // 1 Week
            "ALL" -> 2592000000L // 1 Month
            else -> 86400000L
        }

        var currentTime = System.currentTimeMillis()

        for (i in 0 until count) {
            val drift = -0.0002 // slight positive drift going forward (negative going backward)
            val volatility = scale * (0.8 + rand.nextDouble() * 0.4)
            val percentChange = drift + (rand.nextGaussian() * volatility)
            
            val close = currentPrice
            val open = currentPrice / (1.0 + percentChange)
            val low = Math.min(open, close) * (1.0 - rand.nextDouble() * scale * 0.3)
            val high = Math.max(open, close) * (1.0 + rand.nextDouble() * scale * 0.3)
            val volume = 10000.0 + rand.nextDouble() * 500000.0

            points.add(HistoricalDataPoint(
                timestamp = currentTime,
                open = Math.round(open * 100.0) / 100.0,
                high = Math.round(high * 100.0) / 100.0,
                low = Math.round(low * 100.0) / 100.0,
                close = Math.round(close * 100.0) / 100.0,
                volume = Math.round(volume * 10.0) / 10.0
            ))

            currentPrice = open
            currentTime -= intervalMs
        }

        return points.reversed()
    }

    fun getSmartMoneyRecords(): List<SmartMoneyRecord> {
        return generateSmartMoneyRecords()
    }

    fun getNews(): List<NewsItem> {
        return generateNews()
    }

    fun getUpcomingDividends(): List<DividendRecord> {
        return generateUpcomingDividends()
    }

    private fun generateUpcomingDividends(): List<DividendRecord> {
        val list = mutableListOf<DividendRecord>()
        val types = listOf("Final Dividend", "Interim Dividend", "Special Dividend")
        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val baseCal = java.util.Calendar.getInstance()

        val selectedStocks = STOCKS.take(40)
        selectedStocks.forEachIndexed { index, stock ->
            val rand = Random(stock.symbol.hashCode() + (System.currentTimeMillis() / 60000L).toInt())
            val daysAhead = (index % 18) + 1
            
            val exCal = baseCal.clone() as java.util.Calendar
            exCal.add(java.util.Calendar.DAY_OF_MONTH, daysAhead)
            val exDateStr = dateFormat.format(exCal.time)
            
            val recordCal = exCal.clone() as java.util.Calendar
            recordCal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            val recordDateStr = dateFormat.format(recordCal.time)
            
            val divType = types[rand.nextInt(types.size)]
            val divAmount = when {
                stock.currentPrice > 3000 -> Math.round((rand.nextDouble() * 35.0 + 15.0) * 100.0) / 100.0
                stock.currentPrice > 1000 -> Math.round((rand.nextDouble() * 18.0 + 8.0) * 100.0) / 100.0
                else -> Math.round((rand.nextDouble() * 8.0 + 2.0) * 100.0) / 100.0
            }
            val yieldPct = String.format(java.util.Locale.US, "%.1f", (divAmount / stock.currentPrice) * 100)
            
            val aiInsight = when (divType) {
                "Final Dividend" -> "FY26 Final Dividend payout • Yield ~${yieldPct}%"
                "Interim Dividend" -> "Q2 Interim Dividend payout • Cash rich balance sheet"
                else -> "Special Dividend disclosure • Robust operational cashflows"
            }

            val curPrice = activePrices[stock.symbol] ?: stock.currentPrice

            list.add(DividendRecord(
                id = "${stock.symbol}_div_$index",
                stockSymbol = stock.symbol,
                stockName = stock.name,
                exDate = exDateStr,
                exDateMillis = exCal.timeInMillis,
                recordDate = recordDateStr,
                dividendAmount = divAmount,
                dividendType = divType,
                currentPrice = curPrice,
                changePercent = stock.changePercent,
                aiInsight = aiInsight
            ))
        }

        return list.sortedBy { it.exDateMillis }
    }

    // Get institutional / Smart Money tracking information
    private fun generateSmartMoneyRecords(): List<SmartMoneyRecord> {
        val list = mutableListOf<SmartMoneyRecord>()
        val types = listOf("Mutual Fund", "FII", "DII", "Promoter", "Large Investor")
        val mutualFunds = listOf("SBI Bluechip Fund", "HDFC Top 100", "ICICI Prudential Bluechip", "Nippon India Growth", "Axis Bluechip")
        val fiiNames = listOf("Vanguard Emerging Markets", "BlackRock Global Funds", "Fidelity Investment Group", "JPMorgan India Fund")
        val largeInvestors = listOf("Rakesh Jhunjhunwala Portfolio Assoc.", "Radhakishan Damani Family Trust", "Mukul Agrawal Group", "Ashish Kacholia Holding")

        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val currentCal = java.util.Calendar.getInstance()

        STOCKS.forEachIndexed { index, stock ->
            val rand = Random(stock.symbol.hashCode() + (System.currentTimeMillis() / 60000L).toInt())
            val recordsCount = rand.nextInt(2, 4)
            for (i in 0 until recordsCount) {
                val type = types[rand.nextInt(types.size)]
                val name = when (type) {
                    "Mutual Fund" -> mutualFunds[rand.nextInt(mutualFunds.size)]
                    "FII" -> fiiNames[rand.nextInt(fiiNames.size)]
                    "DII" -> "LIC of India"
                    "Promoter" -> "${stock.name.substringBefore(" ")} Promoter Group"
                    else -> largeInvestors[rand.nextInt(largeInvestors.size)]
                }
                val isBuy = rand.nextBoolean()
                val change = (rand.nextDouble() * 4.5 + 0.1) * (if (isBuy) 1 else -1)

                val recordCal = currentCal.clone() as java.util.Calendar
                recordCal.add(java.util.Calendar.DAY_OF_MONTH, -i)
                val formattedDate = dateFormat.format(recordCal.time)

                list.add(SmartMoneyRecord(
                    id = "${stock.symbol}_sm_${i}",
                    stockSymbol = stock.symbol,
                    stockName = stock.name,
                    investorName = name,
                    holdingChangePercent = Math.round(change * 100.0) / 100.0,
                    dateString = formattedDate,
                    confidenceScore = 70 + rand.nextInt(26), // 70 to 95
                    type = type,
                    action = if (isBuy) "Buying" else "Selling"
                ))
            }
        }
        return list
    }

    // Simulated news with real-time AI timestamps
    private fun generateNews(): List<NewsItem> {
        val list = mutableListOf<NewsItem>()
        val headlines = listOf(
            "Announces strong quarterly financial results with profit expansion." to "Positive",
            "Expands AI & green energy strategic partnership in global markets." to "Positive",
            "Institutional FII & DII buying accelerates following positive forecast." to "Positive",
            "SEBI reviews corporate compliance; core operations remain strong." to "Neutral",
            "Maintains production targets despite global supply chain dynamics." to "Neutral",
            "Management unveils AI transformation roadmap during investor presentation." to "Positive",
            "Analyst coverage notes steady operating margins amidst raw material costs." to "Neutral",
            "Leading brokerages upgrade target price citing solid orderbook pipeline." to "Positive",
            "Announces multi-crore capital expenditure program for strategic expansion." to "Positive",
            "Consolidates near support levels following broader market movements." to "Neutral"
        )

        val currentMinute = ((System.currentTimeMillis() / 60000) % 60).toInt()

        STOCKS.forEachIndexed { index, stock ->
            val rand = Random(stock.symbol.hashCode() + (System.currentTimeMillis() / 60000L).toInt())
            val headlinePair = headlines[rand.nextInt(headlines.size)]
            val score = if (headlinePair.second == "Positive") 65.0 + rand.nextDouble() * 30 
                        else if (headlinePair.second == "Negative") 10.0 + rand.nextDouble() * 30 
                        else 40.0 + rand.nextDouble() * 20

            val timeAgoStr = when (index % 6) {
                0 -> "Just now"
                1 -> "${(currentMinute % 8) + 1} mins ago"
                2 -> "${(currentMinute % 20) + 9} mins ago"
                3 -> "${(currentMinute % 35) + 21} mins ago"
                4 -> "1 hour ago"
                else -> "${(index % 3) + 2} hours ago"
            }

            list.add(NewsItem(
                id = "${stock.symbol}_news_${System.currentTimeMillis()}_$index",
                title = "${stock.symbol} ${headlinePair.first}",
                source = if (index % 2 == 0) "Economic Times AI" else "LiveMint AI Wire",
                timeAgo = timeAgoStr,
                summary = "AI News Engine indicates active market participation in ${stock.name}. Recent updates on corporate actions and institutional flows are actively influencing trading sentiment on NSE and BSE.",
                sentiment = headlinePair.second,
                sentimentScore = Math.round(score * 10.0) / 10.0,
                url = "https://news.google.com/search?q=${stock.name.replace(" ", "+")}"
            ))
        }
        return list
    }
}
