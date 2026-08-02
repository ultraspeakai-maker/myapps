package com.example.aistockmarketnews

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.aistockmarketnews.ui.components.DisclaimerDialog
import com.example.aistockmarketnews.ui.components.NoInternetDialog
import com.example.aistockmarketnews.ui.screens.*
import com.example.aistockmarketnews.ui.viewmodel.StockViewModel

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val stockViewModel: StockViewModel = viewModel {
        StockViewModel(context.applicationContext as Application)
    }

    val isConnected by stockViewModel.isConnected.collectAsState()
    val isDisclaimerAccepted by stockViewModel.isDisclaimerAccepted.collectAsState()

    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                HomeScreen(
                    viewModel = stockViewModel,
                    onNavigate = { key ->
                        when (key) {
                            "smart_money" -> backStack.add(SmartMoneyKey)
                            "alerts" -> backStack.add(AlertsKey)
                            "news" -> backStack.add(NewsKey)
                            "watchlist" -> backStack.add(WatchlistKey)
                            is String -> backStack.add(StockDetailKey(key))
                        }
                    },
                    modifier = Modifier.safeDrawingPadding()
                )
            }

            entry<StockDetailKey> { key ->
                StockDetailScreen(
                    symbol = key.symbol,
                    viewModel = stockViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }

            entry<WatchlistKey> {
                WatchlistScreen(
                    viewModel = stockViewModel,
                    onNavigateToStock = { symbol -> backStack.add(StockDetailKey(symbol)) },
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }

            entry<SmartMoneyKey> {
                SmartMoneyScreen(
                    viewModel = stockViewModel,
                    onNavigateToStock = { symbol -> backStack.add(StockDetailKey(symbol)) },
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }

            entry<AlertsKey> {
                AlertsScreen(
                    viewModel = stockViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }

            entry<NewsKey> {
                NewsScreen(
                    viewModel = stockViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
        }
    )

    if (!isConnected) {
        NoInternetDialog(
            onRetry = { stockViewModel.checkNetworkConnectivity() }
        )
    } else if (!isDisclaimerAccepted) {
        DisclaimerDialog(
            onAccept = { stockViewModel.acceptDisclaimer() }
        )
    }
}
