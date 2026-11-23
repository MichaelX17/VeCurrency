package com.example.vecurrency.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vecurrency.data.local.SettingsPreferences
import com.example.vecurrency.data.repository.CurrencyRepository
import com.example.vecurrency.scheduler.ForegroundRefreshCoordinator
import com.example.vecurrency.ui.about.AboutScreen
import com.example.vecurrency.ui.calculator.CalculatorScreen
import com.example.vecurrency.ui.components.FloatingNavigationBar
import com.example.vecurrency.ui.components.SettingsModal
import com.example.vecurrency.ui.dashboard.DashboardScreen
import com.example.vecurrency.ui.dashboard.DashboardViewModel
import com.example.vecurrency.ui.export.DashboardExporter
import com.example.vecurrency.ui.navigation.Screen
import com.example.vecurrency.ui.theme.VeCurrencyTheme
import com.example.vecurrency.ui.theme.getAppStrings
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen(
    repository: CurrencyRepository,
    coordinator: ForegroundRefreshCoordinator? = null,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { SettingsPreferences(context) }

    var isDarkTheme by remember { mutableStateOf(prefs.isDarkTheme) }
    var language by remember { mutableStateOf(prefs.language) }
    var showSettingsModal by remember { mutableStateOf(false) }

    val strings = remember(language) { getAppStrings(language) }

    val scope = rememberCoroutineScope()

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(repository, coordinator)
    )
    val uiState by viewModel.uiState.collectAsState()

    val items = remember {
        listOf(
            Screen.Dashboard,
            Screen.Calculator,
            Screen.About
        )
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { items.size })

    VeCurrencyTheme(darkTheme = isDarkTheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                FloatingNavigationBar(
                    screens = items,
                    selectedIndex = pagerState.currentPage,
                    strings = strings,
                    onSelectPage = { pageIndex ->
                        scope.launch {
                            pagerState.animateScrollToPage(pageIndex)
                        }
                    }
                )
            },
            modifier = modifier
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(innerPadding)
            ) { page ->
                when (page) {
                    0 -> DashboardScreen(
                        rates = uiState.rates,
                        isRefreshing = uiState.isRefreshing,
                        strings = strings,
                        onRefresh = {
                            onRefresh?.invoke() ?: viewModel.refresh()
                        },
                        onShareSnapshot = {
                            DashboardExporter.generateAndShareSnapshot(context, uiState.rates)
                        },
                        userMessage = uiState.userMessage,
                        onUserMessageShown = { viewModel.userMessageShown() },
                        onOpenSettings = {
                            showSettingsModal = true
                        }
                    )
                    1 -> CalculatorScreen(
                        rates = uiState.rates,
                        strings = strings
                    )
                    2 -> AboutScreen(
                        rates = uiState.rates,
                        repository = repository,
                        strings = strings
                    )
                }
            }

            SettingsModal(
                isVisible = showSettingsModal,
                isDarkTheme = isDarkTheme,
                language = language,
                strings = strings,
                onThemeToggle = { newDark ->
                    isDarkTheme = newDark
                    prefs.isDarkTheme = newDark
                },
                onLanguageChange = { newLang ->
                    language = newLang
                    prefs.language = newLang
                },
                onDismiss = { showSettingsModal = false }
            )
        }
    }
}
