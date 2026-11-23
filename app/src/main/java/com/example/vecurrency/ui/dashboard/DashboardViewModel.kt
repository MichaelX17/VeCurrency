package com.example.vecurrency.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vecurrency.data.repository.CurrencyRepository
import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.domain.model.RateMetadata
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import com.example.vecurrency.scheduler.ForegroundRefreshCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant

data class DashboardUiState(
    val rates: List<ExchangeRate> = emptyList(),
    val isRefreshing: Boolean = false,
    val userMessage: String? = null
)

class DashboardViewModel(
    private val repository: CurrencyRepository,
    private val coordinator: ForegroundRefreshCoordinator? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRatesFlow().collect { ratesFromDb ->
                val fullRates = ensureAllPrimaryCurrenciesPresent(ratesFromDb)
                _uiState.update { it.copy(rates = fullRates) }
            }
        }
        viewModelScope.launch {
            if (coordinator != null) {
                coordinator.checkDueRefreshes()
            } else {
                refresh()
            }
        }
    }

    private fun ensureAllPrimaryCurrenciesPresent(ratesFromDb: List<ExchangeRate>): List<ExchangeRate> {
        val rateMap = ratesFromDb.associateBy { it.currency }
        val primaryCurrencies = listOf(Currency.USD, Currency.EUR, Currency.USDT)

        return primaryCurrencies.map { currency ->
            rateMap[currency] ?: ExchangeRate(
                currency = currency,
                vesRate = BigDecimal.ZERO,
                timestamp = Instant.now(),
                fetchTimestamp = Instant.now(),
                status = RateStatus.UNAVAILABLE,
                source = if (currency == Currency.USDT) RateSource.BINANCE_P2P else RateSource.BCV,
                metadata = RateMetadata(errorMessage = "Sin tasa disponible")
            )
        }
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val result = coordinator?.triggerManualRefresh()
                ?: run {
                    val r1 = repository.refreshBcv()
                    val r2 = repository.refreshUsdt()
                    if (r1.isSuccess || r2.isSuccess) Result.success(Unit)
                    else Result.failure(r1.exceptionOrNull() ?: r2.exceptionOrNull() ?: Exception("Error al actualizar"))
                }

            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    userMessage = if (result.isFailure) "Error al actualizar tasas: ${result.exceptionOrNull()?.localizedMessage}" else "Tasas actualizadas"
                )
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    class Factory(
        private val repository: CurrencyRepository,
        private val coordinator: ForegroundRefreshCoordinator?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository, coordinator) as T
        }
    }
}
