package com.example.aistockmarketnews.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aistockmarketnews.data.model.Stock
import com.example.aistockmarketnews.theme.ColorDown
import com.example.aistockmarketnews.theme.ColorUp
import com.example.aistockmarketnews.theme.ColorNeutral
import com.example.aistockmarketnews.ui.viewmodel.StockViewModel
import com.example.aistockmarketnews.data.repository.StockSimulationEngine
import java.util.Locale
import com.example.aistockmarketnews.ui.components.BannerAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StockViewModel,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    val stocks by viewModel.stocks.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val priceFlashes by viewModel.priceFlashes.collectAsState()
    val smartMoneyRecords by viewModel.smartMoney.collectAsState()
    val news by viewModel.news.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val focusManager = LocalFocusManager.current

    // Main 3-Tab state
    var selectedMainTab by rememberSaveable { mutableStateOf(0) }
    val mainTabs = listOf("Smart Money", "Market News", "Search Stocks")

    // Tab 0 Filter: Smart Money
    var selectedSmartMoneyFilter by rememberSaveable { mutableStateOf("All") }
    var smartMoneySearchQuery by rememberSaveable { mutableStateOf("") }
    val smartMoneyFilters = listOf("All", "Mutual Fund", "FII", "DII", "Promoter", "Large Investor")
    val filteredSmartMoneyRecords = remember(smartMoneyRecords, selectedSmartMoneyFilter, smartMoneySearchQuery) {
        smartMoneyRecords.filter { record ->
            val matchesFilter = selectedSmartMoneyFilter == "All" || record.type == selectedSmartMoneyFilter
            val matchesQuery = smartMoneySearchQuery.isBlank() ||
                record.stockSymbol.contains(smartMoneySearchQuery, ignoreCase = true) ||
                record.stockName.contains(smartMoneySearchQuery, ignoreCase = true) ||
                record.investorName.contains(smartMoneySearchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    // Tab 1 Filter: Market News
    var selectedNewsSentimentFilter by rememberSaveable { mutableStateOf("All") }
    var newsSearchQuery by rememberSaveable { mutableStateOf("") }
    val newsSentimentFilters = listOf("All", "Positive", "Neutral", "Negative")
    val filteredNews = remember(news, selectedNewsSentimentFilter, newsSearchQuery) {
        news.filter { item ->
            val matchesFilter = selectedNewsSentimentFilter == "All" || item.sentiment == selectedNewsSentimentFilter
            val matchesQuery = newsSearchQuery.isBlank() ||
                item.title.contains(newsSearchQuery, ignoreCase = true) ||
                item.summary.contains(newsSearchQuery, ignoreCase = true) ||
                item.source.contains(newsSearchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                BannerAdView(
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
        // Top Refreshing Indicator Banner
        if (isRefreshing) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Refreshing live stock market data...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // App Header Bar
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Stock News",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Indian Markets Intelligence",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        val isMarketOpen = StockSimulationEngine.isMarketOpen()
                        val isHoliday = StockSimulationEngine.isHoliday()
                        val statusText = when {
                            isMarketOpen -> "MARKET LIVE"
                            isHoliday -> "MARKET CLOSED (HOLIDAY)"
                            else -> "MARKET CLOSED"
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isMarketOpen) Color(0xFF10B981) else Color(0xFFEF4444),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Text(
                            text = statusText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMarketOpen) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }

                // Action buttons: Refresh & AI Icon
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.refreshLiveData() },
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Live Data",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Market Holiday Tomorrow Alert Banner (If applicable)
            if (StockSimulationEngine.isTomorrowHoliday()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Holiday",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Market Holiday Tomorrow: NSE/BSE trading will remain closed.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Main Navigation TabRow (3 Tabs: Smart Money, Market News, Search Stocks)
        TabRow(
            selectedTabIndex = selectedMainTab,
            containerColor = Color.Transparent,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            mainTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedMainTab == index,
                    onClick = { selectedMainTab = index },
                    icon = {
                        Icon(
                            imageVector = when (index) {
                                0 -> Icons.Default.TrendingUp
                                1 -> Icons.Default.Newspaper
                                else -> Icons.Default.Search
                            },
                            contentDescription = title
                        )
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedMainTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TAB CONTENT
        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            when (selectedMainTab) {
                // TAB 0: SMART MONEY
                0 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Smart Money Search Bar
                        OutlinedTextField(
                            value = smartMoneySearchQuery,
                            onValueChange = { smartMoneySearchQuery = it },
                            placeholder = { Text("Search stock (e.g. RELIANCE, TATA)...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Smart Money") },
                            trailingIcon = {
                                if (smartMoneySearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { smartMoneySearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                        // Info Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tracks official disclosures of holdings, bulk & block deals, and promoter actions. AI scores confidence metrics and historical institutional trends.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Filter Chips Row
                        ScrollableTabRow(
                            selectedTabIndex = smartMoneyFilters.indexOf(selectedSmartMoneyFilter).coerceAtLeast(0),
                            containerColor = Color.Transparent,
                            edgePadding = 0.dp,
                            divider = {}
                        ) {
                            smartMoneyFilters.forEach { filter ->
                                Tab(
                                    selected = selectedSmartMoneyFilter == filter,
                                    onClick = { selectedSmartMoneyFilter = filter },
                                    text = {
                                        Text(
                                            text = filter,
                                            fontWeight = if (selectedSmartMoneyFilter == filter) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }

                        // Smart Money Records List
                        if (filteredSmartMoneyRecords.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No $selectedSmartMoneyFilter records available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredSmartMoneyRecords) { record ->
                                    SmartMoneyCard(
                                        record = record,
                                        onClick = { onNavigate(record.stockSymbol) }
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 1: MARKET NEWS
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Market News Search Bar
                        OutlinedTextField(
                            value = newsSearchQuery,
                            onValueChange = { newsSearchQuery = it },
                            placeholder = { Text("Search news by stock or keyword...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Market News") },
                            trailingIcon = {
                                if (newsSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { newsSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                        // News Sentiment Info Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Sentiment Analysis",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "AI News Sentiment calculates positive/negative weight based on corporate updates and market trends.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Sentiment Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            newsSentimentFilters.forEach { filter ->
                                val active = selectedNewsSentimentFilter == filter
                                FilterChip(
                                    selected = active,
                                    onClick = { selectedNewsSentimentFilter = filter },
                                    label = { Text(filter, fontSize = 11.sp) }
                                )
                            }
                        }

                        // News List
                        if (filteredNews.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No news items available for $selectedNewsSentimentFilter sentiment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredNews) { item ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        shape = MaterialTheme.shapes.large
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(item.source, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                val (badgeBg, badgeFg) = when (item.sentiment) {
                                                    "Positive" -> ColorUp.copy(alpha = 0.15f) to ColorUp
                                                    "Negative" -> ColorDown.copy(alpha = 0.15f) to ColorDown
                                                    else -> ColorNeutral.copy(alpha = 0.15f) to ColorNeutral
                                                }
                                                Surface(
                                                    color = badgeBg,
                                                    shape = MaterialTheme.shapes.small
                                                ) {
                                                    Text(
                                                        text = item.sentiment,
                                                        color = badgeFg,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(item.summary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(item.timeAgo, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: SEARCH STOCKS & STOCK DIRECTORY
                2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Search Input TextField
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search stocks by name, symbol, or sector...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )

                        // Stock Directory List
                        val listToDisplay = if (searchQuery.isNotEmpty()) searchResults else stocks

                        if (listToDisplay.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No stocks matched \"$searchQuery\"" else "No stocks available",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(listToDisplay) { stock ->
                                    StockRowItem(
                                        stock = stock,
                                        flashDirection = priceFlashes[stock.symbol],
                                        onClick = { onNavigate(stock.symbol) }
                                    )
                            }
                        }
                    }
                }
            }
        }

        }
    }
}
}


@Composable
fun StockRowItem(
    stock: Stock,
    flashDirection: Boolean?,
    onClick: () -> Unit
) {
    val flashColor by animateColorAsState(
        targetValue = when (flashDirection) {
            true -> ColorUp.copy(alpha = 0.25f)
            false -> ColorDown.copy(alpha = 0.25f)
            else -> Color.Transparent
        },
        label = "stockFlash"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(flashColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stock.symbol,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stock.name,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "₹${String.format(Locale.US, "%.2f", stock.currentPrice)}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            val changeSign = if (stock.change >= 0) "+" else ""
            Text(
                text = "$changeSign${String.format(Locale.US, "%.2f", stock.changePercent)}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (stock.change >= 0) ColorUp else ColorDown
            )
        }
    }
}

@Composable
fun AnalyticsShortcutCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = modifier
            .clickable(onClick = onClick)
            .height(90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
