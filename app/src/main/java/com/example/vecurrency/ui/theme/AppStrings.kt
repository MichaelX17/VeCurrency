package com.example.vecurrency.ui.theme

data class AppStrings(
    val appTitle: String = "VeCurrency",
    val appSubtitle: String = "Venezuela • Tipos de cambio",
    val tabInicio: String = "Inicio",
    val tabCalculadora: String = "Calculadora",
    val tabHistorial: String = "Historial",
    val updated: String = "Actualizado",
    val unavailable: String = "No disponible",
    val copyRate: String = "Copiar tasa",
    val lastUpdate: String = "Última actualización",
    val nextBcvUpdate: String = "Próxima actualización BCV:",
    val nextUsdtUpdate: String = "Próxima actualización USDT:",
    val bcvScheduleToday: String = "Hoy",
    val usdtInterval: String = "Cada 4 horas",
    val usdtInTime: String = "En",
    val usdtAvailableNow: String = "Disponible ahora",

    // Calculator
    val calculatorTitle: String = "Calculadora",
    val amountToConvert: String = "Monto a convertir",
    val swapCurrencies: String = "Intercambiar",
    val equivalencies: String = "Equivalencias",
    val clear: String = "Limpiar",
    val copy: String = "Copiar",
    val resultCopied: String = "Resultado copiado",

    // Insights
    val insightsTitle: String = "Detalles e Insights",
    val rateLevel: String = "Nivel de cambio",
    val priceTrend: String = "Tendencia de precios",
    val positive: String = "Positivo",
    val neutral: String = "Neutro",
    val negative: String = "Negativo",
    val verified: String = "Verificado",
    val methodologyTitle: String = "Metodología y Transparencia",
    val bcvSourceDesc: String = "Las tasas USD y EUR provienen directamente del sitio web oficial del BCV. Se consultan exclusivamente en primer plano en los horarios 09:30, 15:00 y 21:30 (Hora de Caracas).",
    val usdtSourceDesc: String = "La tasa USDT es el promedio de las ofertas del mercado P2P de Binance para Venezuela. Se actualiza como máximo cada 4 horas en primer plano.",
    val selectedDateLabel: String = "Fecha punto seleccionado",
    val selectedPriceLabel: String = "Precio",

    // Settings Modal
    val settingsTitle: String = "Configuración",
    val themeLabel: String = "Tema",
    val themeDark: String = "Oscuro",
    val themeLight: String = "Claro",
    val languageLabel: String = "Idioma"
)

fun getAppStrings(language: String): AppStrings {
    return if (language.equals("EN", ignoreCase = true)) {
        AppStrings(
            appSubtitle = "Venezuela • Exchange Rates",
            tabInicio = "Home",
            tabCalculadora = "Calculator",
            tabHistorial = "History",
            updated = "Updated",
            unavailable = "Unavailable",
            copyRate = "Copy rate",
            lastUpdate = "Last update",
            nextBcvUpdate = "Next BCV update:",
            nextUsdtUpdate = "Next USDT update:",
            bcvScheduleToday = "Today",
            usdtInterval = "Every 4 hours",
            usdtInTime = "In",
            usdtAvailableNow = "Available now",

            calculatorTitle = "Calculator",
            amountToConvert = "Amount to convert",
            swapCurrencies = "Swap",
            equivalencies = "Equivalencies",
            clear = "Clear",
            copy = "Copy",
            resultCopied = "Result copied",

            insightsTitle = "Details & Insights",
            rateLevel = "Rate Level",
            priceTrend = "Price Trend",
            positive = "Positive",
            neutral = "Neutral",
            negative = "Negative",
            verified = "Verified",
            methodologyTitle = "Methodology & Transparency",
            bcvSourceDesc = "USD and EUR rates come directly from the official BCV website. They are queried in the foreground at 09:30, 15:00, and 21:30 (Caracas Time).",
            usdtSourceDesc = "The USDT rate is the average of Binance P2P market offers for Venezuela. It updates every 4 hours max in the foreground.",
            selectedDateLabel = "Selected point date",
            selectedPriceLabel = "Price",

            settingsTitle = "Settings",
            themeLabel = "Theme",
            themeDark = "Dark",
            themeLight = "Light",
            languageLabel = "Language"
        )
    } else {
        AppStrings()
    }
}
