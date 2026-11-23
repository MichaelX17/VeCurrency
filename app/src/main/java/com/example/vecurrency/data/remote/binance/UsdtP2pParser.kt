package com.example.vecurrency.data.remote.binance

import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.domain.model.RateMetadata
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

class UsdtParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

object UsdtP2pParser {

    private val MIN_PLAUSIBLE_RATE = BigDecimal("1.00")
    private val MAX_PLAUSIBLE_RATE = BigDecimal("100000.00")
    private const val DISPLAY_SCALE = 4

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parseDolarApiResponse(jsonString: String): DolarApiResponse {
        if (jsonString.isBlank()) throw UsdtParseException("Empty DolarApi response payload")
        return try {
            json.decodeFromString<DolarApiResponse>(jsonString)
        } catch (e: Exception) {
            throw UsdtParseException("Failed to parse DolarApi JSON payload: ${e.message}", e)
        }
    }

    fun parseYadioResponse(jsonString: String): YadioResponse {
        if (jsonString.isBlank()) throw UsdtParseException("Empty Yadio response payload")
        return try {
            json.decodeFromString<YadioResponse>(jsonString)
        } catch (e: Exception) {
            throw UsdtParseException("Failed to parse Yadio JSON payload: ${e.message}", e)
        }
    }

    fun computeRateFromDolarApi(
        response: DolarApiResponse,
        fetchTime: Instant = Instant.now()
    ): ExchangeRate {
        val priceVal = extractBigDecimal(response.promedio)
            ?: throw UsdtParseException("No valid price found in DolarApi response")

        val midpoint = priceVal.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)

        if (midpoint < MIN_PLAUSIBLE_RATE || midpoint > MAX_PLAUSIBLE_RATE) {
            throw UsdtParseException("Implausible USDT rate calculated: $midpoint (must be between $MIN_PLAUSIBLE_RATE and $MAX_PLAUSIBLE_RATE)")
        }

        val rateTimestamp = response.fechaActualizacion?.let {
            try {
                Instant.parse(it)
            } catch (_: Exception) {
                fetchTime
            }
        } ?: fetchTime

        return ExchangeRate(
            currency = Currency.USDT,
            vesRate = midpoint,
            timestamp = rateTimestamp,
            fetchTimestamp = fetchTime,
            status = RateStatus.FRESH,
            source = RateSource.BINANCE_P2P,
            metadata = RateMetadata(
                askMean = midpoint,
                bidMean = midpoint,
                levelsUsed = 1
            )
        )
    }

    fun computeRateFromYadio(
        response: YadioResponse,
        fetchTime: Instant = Instant.now()
    ): ExchangeRate {
        val priceVal = extractBigDecimal(response.USD?.rate)
            ?: throw UsdtParseException("No valid price found in Yadio response")

        val midpoint = priceVal.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)

        if (midpoint < MIN_PLAUSIBLE_RATE || midpoint > MAX_PLAUSIBLE_RATE) {
            throw UsdtParseException("Implausible USDT rate calculated: $midpoint (must be between $MIN_PLAUSIBLE_RATE and $MAX_PLAUSIBLE_RATE)")
        }

        return ExchangeRate(
            currency = Currency.USDT,
            vesRate = midpoint,
            timestamp = fetchTime,
            fetchTimestamp = fetchTime,
            status = RateStatus.FRESH,
            source = RateSource.BINANCE_P2P,
            metadata = RateMetadata(
                askMean = midpoint,
                bidMean = midpoint,
                levelsUsed = 1
            )
        )
    }

    fun parseDolarApiOfficialRate(
        jsonString: String,
        targetCurrency: Currency,
        fetchTime: Instant = Instant.now()
    ): ExchangeRate {
        val dto = parseDolarApiResponse(jsonString)
        val priceVal = extractBigDecimal(dto.promedio)
            ?: throw UsdtParseException("No valid price found in official DolarApi response for ${targetCurrency.code}")

        val midpoint = priceVal.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
        val rateTimestamp = dto.fechaActualizacion?.let {
            try { Instant.parse(it) } catch (_: Exception) { fetchTime }
        } ?: fetchTime

        return ExchangeRate(
            currency = targetCurrency,
            vesRate = midpoint,
            timestamp = rateTimestamp,
            fetchTimestamp = fetchTime,
            status = RateStatus.FRESH,
            source = RateSource.BCV,
            metadata = RateMetadata(valueDateIso = dto.fechaActualizacion)
        )
    }

    private fun extractBigDecimal(primitive: JsonPrimitive?): BigDecimal? {
        if (primitive == null) return null
        val content = primitive.content.trim()
        if (content.isBlank() || content.equals("null", ignoreCase = true)) return null
        return try {
            val bd = BigDecimal(content)
            if (bd > BigDecimal.ZERO) bd else null
        } catch (_: Exception) {
            null
        }
    }
}
