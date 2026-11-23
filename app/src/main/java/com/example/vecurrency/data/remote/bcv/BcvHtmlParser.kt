package com.example.vecurrency.data.remote.bcv

import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.domain.model.RateMetadata
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import org.jsoup.Jsoup
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class BcvParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

object BcvHtmlParser {

    private val MIN_PLAUSIBLE_RATE = BigDecimal("1.00")
    private val MAX_PLAUSIBLE_RATE = BigDecimal("100000.00")

    fun parse(html: String, fetchTime: Instant = Instant.now()): List<ExchangeRate> {
        if (html.isBlank()) {
            throw BcvParseException("BCV HTML response is empty")
        }

        val doc = try {
            Jsoup.parse(html)
        } catch (e: Exception) {
            throw BcvParseException("Failed to parse HTML document", e)
        }

        // Parse Fecha Valor
        val valueDateElement = doc.selectFirst("span[property='dc:date']")
        val valueDateIso = valueDateElement?.attr("content")?.ifBlank { null }
            ?: valueDateElement?.text()?.ifBlank { null }

        val observationInstant = parseObservationTime(valueDateIso) ?: fetchTime

        val usdRate = parseCurrencyRate(
            doc = doc,
            containerId = "dolar",
            currency = Currency.USD,
            fetchTime = fetchTime,
            observationInstant = observationInstant,
            valueDateIso = valueDateIso
        )

        val eurRate = parseCurrencyRate(
            doc = doc,
            containerId = "euro",
            currency = Currency.EUR,
            fetchTime = fetchTime,
            observationInstant = observationInstant,
            valueDateIso = valueDateIso
        )

        return listOf(usdRate, eurRate)
    }

    private fun parseCurrencyRate(
        doc: org.jsoup.nodes.Document,
        containerId: String,
        currency: Currency,
        fetchTime: Instant,
        observationInstant: Instant,
        valueDateIso: String?
    ): ExchangeRate {
        val container = doc.getElementById(containerId)
            ?: throw BcvParseException("Missing container id='$containerId' for ${currency.code}")

        val rateElement = container.selectFirst("strong.strong-tb")
            ?: container.selectFirst("div.textp")
            ?: throw BcvParseException("Missing rate text element inside container id='$containerId'")

        val rawText = rateElement.text().trim()
        if (rawText.isBlank()) {
            throw BcvParseException("Rate text is empty for ${currency.code} in container id='$containerId'")
        }

        val cleanNumberStr = sanitizeNumberString(rawText)
        val bdRate = try {
            BigDecimal(cleanNumberStr)
        } catch (e: Exception) {
            throw BcvParseException("Malformed rate number '$rawText' (cleaned: '$cleanNumberStr') for ${currency.code}", e)
        }

        if (bdRate < MIN_PLAUSIBLE_RATE || bdRate > MAX_PLAUSIBLE_RATE) {
            throw BcvParseException("Implausible rate value '$bdRate' for ${currency.code} (must be between $MIN_PLAUSIBLE_RATE and $MAX_PLAUSIBLE_RATE)")
        }

        return ExchangeRate(
            currency = currency,
            vesRate = bdRate,
            timestamp = observationInstant,
            fetchTimestamp = fetchTime,
            status = RateStatus.FRESH,
            source = RateSource.BCV,
            metadata = RateMetadata(valueDateIso = valueDateIso)
        )
    }

    private fun sanitizeNumberString(raw: String): String {
        var s = raw.replace("\u00A0", "").replace(" ", "").trim()
        if (s.contains(".") && s.contains(",")) {
            s = s.replace(".", "").replace(",", ".")
        } else if (s.contains(",")) {
            s = s.replace(",", ".")
        }
        return s
    }

    private fun parseObservationTime(isoString: String?): Instant? {
        if (isoString == null) return null
        return try {
            OffsetDateTime.parse(isoString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
        } catch (_: Exception) {
            null
        }
    }
}
