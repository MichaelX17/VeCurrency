package com.example.vecurrency.domain.model

enum class Currency(
    val code: String,
    val displayName: String,
    val symbol: String,
    val defaultDecimals: Int
) {
    USD(code = "USD", displayName = "Dólar estadounidense", symbol = "$", defaultDecimals = 2),
    EUR(code = "EUR", displayName = "Euro", symbol = "€", defaultDecimals = 2),
    USDT(code = "USDT", displayName = "Tether (Binance)", symbol = "USDT", defaultDecimals = 2),
    VES(code = "VES", displayName = "Bolívar venezolano", symbol = "Bs.", defaultDecimals = 2);

    companion object {
        fun fromCode(code: String): Currency? = entries.find { it.code.equals(code, ignoreCase = true) }
    }
}
