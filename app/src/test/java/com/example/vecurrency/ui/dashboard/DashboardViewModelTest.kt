package com.example.vecurrency.ui.dashboard

import com.example.vecurrency.data.repository.CurrencyRepository
import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class TestRepository : CurrencyRepository {
        val ratesFlow = MutableStateFlow<List<ExchangeRate>>(emptyList())
        var bcvRefreshed = false
        var usdtRefreshed = false

        override fun getRatesFlow(): Flow<List<ExchangeRate>> = ratesFlow
        override suspend fun refreshBcv(): Result<Unit> {
            bcvRefreshed = true
            return Result.success(Unit)
        }
        override suspend fun refreshUsdt(): Result<Unit> {
            usdtRefreshed = true
            return Result.success(Unit)
        }
        override suspend fun getFiveDayPrices(currencyCode: String, fallbackRate: BigDecimal?): List<BigDecimal> {
            val base = fallbackRate ?: BigDecimal("100.00")
            return listOf(base, base, base, base, base)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_subscribesToRatesFlow_returnsAllThreePrimaryCurrencies() = runTest {
        val testRepo = TestRepository()
        val viewModel = DashboardViewModel(repository = testRepo)

        val sampleRate = ExchangeRate(
            currency = Currency.USD,
            vesRate = BigDecimal("900.00"),
            timestamp = Instant.now(),
            fetchTimestamp = Instant.now(),
            status = RateStatus.FRESH,
            source = RateSource.BCV
        )

        testRepo.ratesFlow.value = listOf(sampleRate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Rates list always contains USD, EUR, and USDT
        assertEquals(3, viewModel.uiState.value.rates.size)
        assertEquals(Currency.USD, viewModel.uiState.value.rates[0].currency)
        assertEquals(Currency.EUR, viewModel.uiState.value.rates[1].currency)
        assertEquals(Currency.USDT, viewModel.uiState.value.rates[2].currency)
        assertEquals(BigDecimal("900.00"), viewModel.uiState.value.rates[0].vesRate)
        assertEquals(RateStatus.UNAVAILABLE, viewModel.uiState.value.rates[1].status)
        assertEquals(RateStatus.UNAVAILABLE, viewModel.uiState.value.rates[2].status)
    }

    @Test
    fun refresh_triggersRepositoryRefresh() = runTest {
        val testRepo = TestRepository()
        val viewModel = DashboardViewModel(repository = testRepo)

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(testRepo.bcvRefreshed)
        assertTrue(testRepo.usdtRefreshed)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals("Tasas actualizadas", viewModel.uiState.value.userMessage)
    }

    @Test
    fun userMessageShown_clearsUserMessage() = runTest {
        val testRepo = TestRepository()
        val viewModel = DashboardViewModel(repository = testRepo)

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.userMessageShown()
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun init_failedFetchDoesNotTriggerInfiniteRefreshLoop() = runTest {
        var refreshCount = 0
        val testRepo = object : CurrencyRepository {
            val ratesFlow = MutableStateFlow<List<ExchangeRate>>(emptyList())
            override fun getRatesFlow(): Flow<List<ExchangeRate>> = ratesFlow
            override suspend fun refreshBcv(): Result<Unit> {
                refreshCount++
                return Result.failure(Exception("Fetch failed"))
            }
            override suspend fun refreshUsdt(): Result<Unit> {
                return Result.failure(Exception("Fetch failed"))
            }
            override suspend fun getFiveDayPrices(currencyCode: String, fallbackRate: BigDecimal?): List<BigDecimal> {
                val base = fallbackRate ?: BigDecimal("100.00")
                return listOf(base, base, base, base, base)
            }
        }

        val viewModel = DashboardViewModel(repository = testRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        // Emitting new values on DB flow must NOT trigger another refresh
        testRepo.ratesFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.rates.size)
        assertEquals(1, refreshCount)
    }
}
