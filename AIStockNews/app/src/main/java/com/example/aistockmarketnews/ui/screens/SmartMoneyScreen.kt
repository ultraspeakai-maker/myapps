package com.example.aistockmarketnews.ui.screens

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aistockmarketnews.data.model.SmartMoneyRecord
import com.example.aistockmarketnews.theme.ColorDown
import com.example.aistockmarketnews.theme.ColorNeutral
import com.example.aistockmarketnews.theme.ColorUp
import com.example.aistockmarketnews.ui.components.BannerAdView
import com.example.aistockmarketnews.ui.viewmodel.StockViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMoneyScreen(
    viewModel: StockViewModel,
    onNavigateToStock: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val smartMoneyRecords by viewModel.smartMoney.collectAsState()
    var selectedTypeFilter by rememberSaveable { mutableStateOf("All") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val filters = listOf("All", "Mutual Fund", "FII", "DII", "Promoter", "Large Investor")

    val filteredRecords = remember(smartMoneyRecords, selectedTypeFilter, searchQuery) {
        smartMoneyRecords.filter { record ->
            val matchesFilter = selectedTypeFilter == "All" || record.type == selectedTypeFilter
            val matchesQuery = searchQuery.isBlank() ||
                record.stockSymbol.contains(searchQuery, ignoreCase = true) ||
                record.stockName.contains(searchQuery, ignoreCase = true) ||
                record.investorName.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Money Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search stock or investor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Smart Money") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
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
            // Main content block inside a weight(1f) Box/Column to leave space for Banner
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Description banner
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
                            text = "Tracks official disclosures of holdings, bulk & block deals, and promoter actions. AI scores confidence metrics and provides historical trends.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Horizontally scrollable chip filter row
                ScrollableTabRow(
                    selectedTabIndex = filters.indexOf(selectedTypeFilter).coerceAtLeast(0),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    filters.forEach { filter ->
                        Tab(
                            selected = selectedTypeFilter == filter,
                            onClick = { selectedTypeFilter = filter },
                            text = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                if (filteredRecords.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No records found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredRecords) { record ->
                            SmartMoneyCard(
                                record = record,
                                onClick = { onNavigateToStock(record.stockSymbol) }
                            )
                    }
                }
            }
}

        }
    }
}

@Composable
fun SmartMoneyCard(
    record: SmartMoneyRecord,
    onClick: () -> Unit
) {
    val isBuy = record.holdingChangePercent >= 0
    val flowColor = if (isBuy) ColorUp else ColorDown

    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = record.stockSymbol,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = record.stockName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = flowColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (isBuy) "BUYING" else "SELLING",
                        color = flowColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Investor / Institution", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = record.investorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Holding Change", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val changeSign = if (record.holdingChangePercent >= 0) "+" else ""
                    Text(
                        text = "$changeSign${record.holdingChangePercent}%",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = flowColor
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filing: ${record.dateString}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("AI Confidence:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${record.confidenceScore}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
