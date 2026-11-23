package com.example.vecurrency.data.remote.bcv

import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.RateSource
import com.example.vecurrency.domain.model.RateStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class BcvHtmlParserTest {

    private fun loadFixture(filename: String): String {
        val stream = javaClass.classLoader?.getResourceAsStream("fixtures/$filename")
            ?: error("Fixture fixtures/$filename not found")
        return stream.bufferedReader().readText()
    }

    @Test
    fun parse_normalHtml_returnsUsdAndEurRates() {
        val html = loadFixture("bcv_normal.html")
        val fetchTime = Instant.parse("2026-11-22T10:00:00Z")

        val rates = BcvHtmlParser.parse(html, fetchTime)

        assertEquals(2, rates.size)

        val usd = rates.first { it.currency == Currency.USD }
        assertEquals(BigDecimal("919.82270000"), usd.vesRate)
        assertEquals(RateSource.BCV, usd.source)
        assertEquals(RateStatus.FRESH, usd.status)
        assertEquals("2026-11-24T00:00:00-04:00", usd.metadata?.valueDateIso)

        val eur = rates.first { it.currency == Currency.EUR }
        assertEquals(BigDecimal("1011.17949056"), eur.vesRate)
        assertEquals(RateSource.BCV, eur.source)
        assertEquals(RateStatus.FRESH, eur.status)
    }

    @Test(expected = BcvParseException::class)
    fun parse_missingUsd_throwsBcvParseException() {
        val html = loadFixture("bcv_missing_usd.html")
        BcvHtmlParser.parse(html)
    }

    @Test(expected = BcvParseException::class)
    fun parse_missingEuro_throwsBcvParseException() {
        val html = loadFixture("bcv_missing_euro.html")
        BcvHtmlParser.parse(html)
    }

    @Test(expected = BcvParseException::class)
    fun parse_malformedNumber_throwsBcvParseException() {
        val html = loadFixture("bcv_malformed_number.html")
        BcvHtmlParser.parse(html)
    }

    @Test(expected = BcvParseException::class)
    fun parse_changedSelectors_throwsBcvParseException() {
        val html = loadFixture("bcv_changed_selectors.html")
        BcvHtmlParser.parse(html)
    }

    @Test(expected = BcvParseException::class)
    fun parse_emptyHtml_throwsBcvParseException() {
        BcvHtmlParser.parse("")
    }
}
