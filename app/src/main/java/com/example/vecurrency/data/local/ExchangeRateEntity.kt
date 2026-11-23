package com.example.vecurrency.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.domain.model.RateMetadata
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import java.math.BigDecimal
import java.time.Instant

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val currencyCode: String,
    val vesRate: String,
    val timestampEpochMs: Long,
    val fetchTimestampEpochMs: Long,
    val status: String,
    val source: String,
    val valueDateIso: String? = null,
    val askMean: String? = null,
    val bidMean: String? = null,
    val levelsUsed: Int? = null
) {
    fun toDomain(): ExchangeRate? {
        val currency = Currency.fromCode(currencyCode) ?: return null
        val bdRate = try { BigDecimal(vesRate) } catch (_: NumberFormatException) { return null }
        val rateStatus = try { RateStatus.valueOf(status) } catch (_: IllegalArgumentException) { RateStatus.STALE }
        val rateSource = try { RateSource.valueOf(source) } catch (_: IllegalArgumentException) { RateSource.BCV }

        val metadata = if (valueDateIso != null || askMean != null || bidMean != null || levelsUsed != null) {
            RateMetadata(
                askMean = askMean?.let { try { BigDecimal(it) } catch (_: NumberFormatException) { null } },
                bidMean = bidMean?.let { try { BigDecimal(it) } catch (_: NumberFormatException) { null } },
                levelsUsed = levelsUsed,
                valueDateIso = valueDateIso
            )
        } else {
            null
        }

        return ExchangeRate(
            currency = currency,
            vesRate = bdRate,
            timestamp = Instant.ofEpochMilli(timestampEpochMs),
            fetchTimestamp = Instant.ofEpochMilli(fetchTimestampEpochMs),
            status = rateStatus,
            source = rateSource,
            metadata = metadata
        )
    }

    companion object {
        fun fromDomain(rate: ExchangeRate): ExchangeRateEntity {
            return ExchangeRateEntity(
                currencyCode = rate.currency.code,
                vesRate = rate.vesRate.toPlainString(),
                timestampEpochMs = rate.timestamp.toEpochMilli(),
                fetchTimestampEpochMs = rate.fetchTimestamp.toEpochMilli(),
                status = rate.status.name,
                source = rate.source.name,
                valueDateIso = rate.metadata?.valueDateIso,
                askMean = rate.metadata?.askMean?.toPlainString(),
                bidMean = rate.metadata?.bidMean?.toPlainString(),
                levelsUsed = rate.metadata?.levelsUsed
            )
        }
    }
}
