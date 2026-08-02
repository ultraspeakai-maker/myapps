package com.example.aistockmarketnews.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import com.example.aistockmarketnews.data.model.AIAlert
import com.example.aistockmarketnews.data.model.NewsItem
import com.example.aistockmarketnews.data.model.Stock
import com.example.aistockmarketnews.data.model.StockDetail
import com.example.aistockmarketnews.theme.ColorDown
import com.example.aistockmarketnews.theme.ColorNeutral
import com.example.aistockmarketnews.theme.ColorUp
import com.example.aistockmarketnews.ui.components.BannerAdView

import com.example.aistockmarketnews.ui.viewmodel.StockViewModel
import java.util.Locale
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    symbol: String,
    viewModel: StockViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Select stock to load historical data and calculations
    LaunchedEffect(symbol) {
        viewModel.selectStock(symbol)
    }

    val stock by viewModel.selectedStock.collectAsState()
    val detail by viewModel.selectedStockDetail.collectAsState()
    val history by viewModel.historicalData.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val news by viewModel.news.collectAsState()
    val priceFlashes by viewModel.priceFlashes.collectAsState()

    var visibleRangeStart by remember { mutableStateOf(0) }
    var visibleCount by remember { mutableStateOf(40) }

    var lastKey by remember { mutableStateOf("") }
    val currentKey = "${symbol}_${selectedTimeframe}"

    LaunchedEffect(history, currentKey) {
        if (history.isNotEmpty() && (lastKey != currentKey || visibleCount == 0 || visibleCount > history.size)) {
            lastKey = currentKey
            visibleCount = kotlin.math.min(40, history.size)
            visibleRangeStart = kotlin.math.max(0, history.size - visibleCount)
        }
    }

    var showCreateAlertDialog by remember { mutableStateOf(false) }

    val isWatched = remember(symbol, viewModel) {
        viewModel.isWatched(symbol)
    }

    val stockNews = remember(symbol, news) {
        news.filter { it.title.contains(symbol, ignoreCase = true) }
    }

    if (stock == null || detail == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentStock = stock!!
    val currentDetail = detail!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentStock.symbol, fontWeight = FontWeight.Bold)
                        Text(
                            currentStock.name,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Watchlist Toggle
                    IconButton(onClick = { viewModel.toggleWatchlist(symbol) }) {
                        Icon(
                            imageVector = if (viewModel.watchlist.collectAsState().value.any { it.symbol == symbol }) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Watchlist",
                            tint = if (viewModel.watchlist.collectAsState().value.any { it.symbol == symbol }) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Create Alert Button
                    IconButton(onClick = { showCreateAlertDialog = true }) {
                        Icon(Icons.Default.AddAlert, contentDescription = "Create Alert")
                    }
                }
            )
        },
        modifier = modifier,
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                com.example.aistockmarketnews.ui.components.BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Live Price Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", currentStock.currentPrice)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black
                        )
                        val (displayChange, displayChangePct) = remember(selectedTimeframe, currentStock.currentPrice, history) {
                            if (selectedTimeframe == "1D" || history.isEmpty()) {
                                Pair(currentStock.change, currentStock.changePercent)
                            } else {
                                val startPrice = history.first().close
                                val diff = currentStock.currentPrice - startPrice
                                val pct = if (startPrice > 0) (diff / startPrice) * 100.0 else 0.0
                                Pair(diff, pct)
                            }
                        }
                        val changeSign = if (displayChange >= 0) "+" else ""
                        val tfTag = if (selectedTimeframe == "1D") "" else " • $selectedTimeframe"
                        Text(
                            text = "$changeSign${String.format(Locale.US, "%.2f", displayChange)} (${String.format(Locale.US, "%.2f", displayChangePct)}%)$tfTag",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (displayChange >= 0) ColorUp else ColorDown
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Day's High: ₹${String.format(Locale.US, "%.2f", currentDetail.high)}", fontSize = 11.sp, color = ColorUp, fontWeight = FontWeight.Bold)
                        Text("Day's Low: ₹${String.format(Locale.US, "%.2f", currentDetail.low)}", fontSize = 11.sp, color = ColorDown, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Market Closed Info Banner
            item {
                val isMarketOpen = com.example.aistockmarketnews.data.repository.StockSimulationEngine.isMarketOpen()
                val isHoliday = com.example.aistockmarketnews.data.repository.StockSimulationEngine.isHoliday()
                if (!isMarketOpen) {
                    val closedText = if (isHoliday) {
                        "Market is CLOSED today for an NSE/BSE Trading Holiday. Prices are currently static."
                    } else {
                        "Market is CLOSED (Hours: 9:15 AM - 3:30 PM IST Mon-Fri). Prices are currently static."
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Market Closed",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = closedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }



            // Key Support & Resistance List (S/R Levels)
            item {
                Text(
                    text = "S/R Levels",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Support List
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Support Levels", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorUp)
                            Spacer(modifier = Modifier.height(6.dp))
                            currentDetail.supportList.forEachIndexed { i, s ->
                                Text("S${i+1}: ₹${String.format(Locale.US, "%.1f", s)}", fontSize = 12.sp)
                            }
                        }
                    }

                    // Resistance List
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Resistance Levels", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorDown)
                            Spacer(modifier = Modifier.height(6.dp))
                            currentDetail.resistanceList.forEachIndexed { i, r ->
                                Text("R${i+1}: ₹${String.format(Locale.US, "%.1f", r)}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Fundamentals & Corporate Details
            item {
                Text(
                    text = "Fundamentals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Card(shape = MaterialTheme.shapes.large) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FundamentalRow("Open", "₹${String.format(Locale.US, "%.2f", currentDetail.open)}")
                        FundamentalRow("Prev. Close", "₹${String.format(Locale.US, "%.2f", currentStock.previousClose)}")
                        FundamentalRow("Volume", String.format(Locale.US, "%,d", currentDetail.volume))
                        FundamentalRow("Market Cap", "₹${currentDetail.marketCap} Cr")
                        FundamentalRow("P/E Ratio", "${currentDetail.pe}")
                        FundamentalRow("EPS", "₹${currentDetail.eps}")
                        FundamentalRow("Div. Yield", "${currentDetail.dividendYield}%")
                        FundamentalRow("Sector", currentStock.sector)
                        FundamentalRow("Industry", currentStock.industry)
                    }
                }
            }

            // Shareholding Pattern
            item {
                Text(
                    text = "Shareholding Pattern",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Card(shape = MaterialTheme.shapes.large) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FundamentalRow("Promoters Holding", "${String.format(Locale.US, "%.2f", currentDetail.promoterHolding)}%")
                        FundamentalRow("Mutual Funds", "${String.format(Locale.US, "%.2f", currentDetail.mutualFundHolding)}%")
                        FundamentalRow("Foreign & Dom. Institutions", "${String.format(Locale.US, "%.2f", currentDetail.institutionHolding)}%")
                    }
                }
            }

            // Bulk Deals Analysis Section
            item {
                Text(
                    text = "Bulk Deals Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                BulkDealsCard(
                    symbol = currentStock.symbol,
                    currentPrice = currentStock.currentPrice,
                    isPositive = currentStock.isPositive
                )
            }

            // Company Info Description
            item {
                Text(
                    text = "Company Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Card(shape = MaterialTheme.shapes.large) {
                    Text(
                        text = currentDetail.description,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Stock News Segment
            if (stockNews.isNotEmpty()) {
                item {
                    Text(
                        text = "Related News",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(stockNews) { item ->
                    NewsRowItem(item)
                }
            }

            // Bottom space for scrolling comfort
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    }

    // Create Alert Dialog
    if (showCreateAlertDialog) {
        var targetValueText by remember { mutableStateOf(currentStock.currentPrice.toString()) }
        var selectedType by remember { mutableStateOf("Price Crosses") }
        val alertTypes = listOf("Price Crosses", "RSI Above", "RSI Below", "Volume Spike")

        AlertDialog(
            onDismissRequest = { showCreateAlertDialog = false },
            title = { Text("Set AI Alerts") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Trigger an alert locally on your device for ${currentStock.symbol}.", fontSize = 12.sp)
                    
                    // Alert type dropdown selection (simulated radio row)
                    Text("Alert Trigger:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        alertTypes.forEach { type ->
                            val active = type == selectedType
                            InputChip(
                                selected = active,
                                onClick = { selectedType = type },
                                label = { Text(type, fontSize = 9.sp) }
                            )
                        }
                    }

                    // Target value input
                    OutlinedTextField(
                        value = targetValueText,
                        onValueChange = { targetValueText = it },
                        label = { Text("Target Threshold Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetValueText.toDoubleOrNull() ?: currentStock.currentPrice
                        viewModel.createAlert(symbol, selectedType, target)
                        showCreateAlertDialog = false
                    }
                ) {
                    Text("Create Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAlertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FundamentalRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NewsRowItem(item: NewsItem) {
    val context = LocalContext.current
    val badgeColor = when (item.sentiment) {
        "Positive" -> ColorUp
        "Negative" -> ColorDown
        else -> ColorNeutral
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Ignore or fallback
                }
            }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.source} • ${item.timeAgo}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Surface(
                    shape = CircleShape,
                    color = badgeColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${item.sentiment} (${Math.round(item.sentimentScore)}%)",
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.summary,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}



data class SimulatedBulkDeal(
    val investor: String,
    val isBuy: Boolean,
    val shares: Int,
    val price: Double,
    val date: String
)

@Composable
fun BulkDealsCard(
    symbol: String,
    currentPrice: Double,
    isPositive: Boolean
) {
    val rand = remember(symbol) { java.util.Random(symbol.hashCode().toLong()) }

    // Dynamic Bulk Deals for this specific stock
    val investors = listOf(
        "Vanguard Group", "Morgan Stanley", "Societe Generale", 
        "HDFC Mutual Fund", "SBI Mutual Fund", "Life Insurance Corporation (LIC)",
        "Nippon India MF", "BNP Paribas", "JP Morgan Securities"
    )
    
    val bulkDeals = remember(symbol) {
        val dates = listOf("13 Jul 2026", "10 Jul 2026", "08 Jul 2026", "03 Jul 2026", "29 Jun 2026")
        List(3) { i ->
            val investor = investors[rand.nextInt(investors.size)]
            val isBuy = rand.nextBoolean()
            val shares = (100000 + rand.nextInt(900000))
            val price = currentPrice * (0.99 + rand.nextDouble() * 0.02)
            val date = dates.getOrNull(i % dates.size) ?: "13 Jul 2026"
            SimulatedBulkDeal(investor, isBuy, shares, price, date)
        }
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Large Institutional Transactions (NSE/BSE)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bulkDeals.forEach { deal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(deal.investor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Qty: ${String.format(Locale.US, "%,d", deal.shares)} shares • ${deal.date}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val text = if (deal.isBuy) "BUY" else "SELL"
                            val color = if (deal.isBuy) ColorUp else ColorDown
                            Text(
                                text = text,
                                color = color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("Price: ₹${String.format(Locale.US, "%.2f", deal.price)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
