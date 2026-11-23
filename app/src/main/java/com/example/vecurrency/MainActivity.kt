package com.example.vecurrency

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.example.vecurrency.data.local.AppDatabase
import com.example.vecurrency.data.repository.DefaultCurrencyRepository
import com.example.vecurrency.scheduler.ForegroundRefreshCoordinator
import com.example.vecurrency.ui.MainAppScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repository: DefaultCurrencyRepository by lazy {
        val db = AppDatabase.getInstance(applicationContext)
        DefaultCurrencyRepository(
            exchangeRateDao = db.exchangeRateDao(),
            bcvSlotLogDao = db.bcvSlotLogDao(),
            lastFetchDao = db.lastFetchDao(),
            dailyRateHistoryDao = db.dailyRateHistoryDao()
        )
    }

    private val coordinator: ForegroundRefreshCoordinator by lazy {
        ForegroundRefreshCoordinator(repository = repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val repo = remember { repository }
            val coord = remember { coordinator }
            MainAppScreen(
                repository = repo,
                coordinator = coord,
                onRefresh = {
                    lifecycleScope.launch {
                        coord.triggerManualRefresh()
                    }
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        coordinator.onAppForegrounded(lifecycleScope)
    }

    override fun onStop() {
        super.onStop()
        coordinator.onAppBackgrounded()
    }
}
