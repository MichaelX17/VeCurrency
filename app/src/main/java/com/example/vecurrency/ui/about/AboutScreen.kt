package com.example.vecurrency.ui.about

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.ui.components.BcvLogoIcon
import com.example.vecurrency.ui.components.BinanceIcon
import com.example.vecurrency.ui.components.EurFlagIcon
import com.example.vecurrency.ui.components.UsFlagIcon
import com.example.vecurrency.ui.theme.AppStrings
import com.example.vecurrency.ui.theme.EurPurple
import com.example.vecurrency.ui.theme.SuccessGreen
import com.example.vecurrency.ui.theme.TextMuted
import com.example.vecurrency.ui.theme.TextPrimary
import com.example.vecurrency.ui.theme.TextSecondary
import com.example.vecurrency.ui.theme.UsdBlue
import com.example.vecurrency.ui.theme.UsdtYellow
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.example.vecurrency.data.repository.CurrencyRepository
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AboutScreen(
    rates: List<ExchangeRate> = emptyList(),
    repository: CurrencyRepository? = null,
    strings: AppStrings,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCurrency by remember { mutableStateOf(Currency.USD) }
    var selectedPointIndex by remember { mutableStateOf(4) } // Default to latest point (today)

    val currentRate = remember(rates, selectedCurrency) {
        rates.firstOrNull { it.currency == selectedCurrency }
    }

    var fiveDayRates by remember { mutableStateOf<List<BigDecimal>>(emptyList()) }

    LaunchedEffect(selectedCurrency, currentRate, repository) {
        val base = currentRate?.vesRate
        val fetched = repository?.getFiveDayPrices(selectedCurrency.code, base)
            ?: run {
                val b = base ?: BigDecimal("100.00")
                listOf(b, b, b, b, b)
            }
        fiveDayRates = fetched
    }

    val caracasZone = remember { ZoneId.of("America/Caracas") }
    val fiveDayDates = remember {
        val today = LocalDate.now(caracasZone)
        (4 downTo 0).map { today.minusDays(it.toLong()) }
    }

    val currencyAccent = when (selectedCurrency) {
        Currency.USD -> UsdBlue
        Currency.EUR -> EurPurple
        Currency.USDT -> UsdtYellow
        Currency.VES -> UsdBlue
    }

    val selectedPrice = remember(fiveDayRates, selectedPointIndex) {
        if (fiveDayRates.isEmpty()) {
            "Bs. --"
        } else {
            val index = selectedPointIndex.coerceIn(0, fiveDayRates.lastIndex)
            val rateVal = fiveDayRates[index]
            "Bs. ${rateVal.toPlainString().replace('.', ',')}"
        }
    }

    val selectedDateText = remember(fiveDayDates, selectedPointIndex, strings) {
        if (fiveDayDates.isEmpty()) {
            "--"
        } else {
            val index = selectedPointIndex.coerceIn(0, fiveDayDates.lastIndex)
            val date = fiveDayDates[index]
            val formatted = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date)
            if (index == 4) "$formatted (${strings.bcvScheduleToday})" else formatted
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Text(
                    text = strings.insightsTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    textAlign = if (onBack != null) TextAlign.Center else TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Currency Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(Currency.USD, Currency.EUR, Currency.USDT).forEach { curr ->
                    val isSelected = selectedCurrency == curr
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                selectedCurrency = curr
                                selectedPointIndex = 4
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (curr) {
                                Currency.USD -> UsFlagIcon(size = 20.dp)
                                Currency.EUR -> EurFlagIcon(size = 20.dp)
                                Currency.USDT -> BinanceIcon(size = 20.dp)
                                Currency.VES -> {}
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = curr.code,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Nivel de cambio
            Text(
                text = strings.rateLevel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val ringSize = if (maxWidth < 340.dp) 100.dp else 130.dp
                    val trackColor = MaterialTheme.colorScheme.primaryContainer

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(ringSize),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = (ringSize.toPx() * 0.11f)
                                val radius = (size.minDimension - strokeWidth) / 2f
                                val center = Offset(size.width / 2f, size.height / 2f)

                                drawCircle(
                                    color = trackColor,
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = strokeWidth)
                                )

                                drawArc(
                                    color = currencyAccent,
                                    startAngle = -90f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val currentRateDisplay = currentRate?.vesRate?.let {
                                    "Bs. ${it.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',')}"
                                } ?: "--"
                                Text(
                                    text = currentRateDisplay,
                                    style = if (ringSize < 120.dp) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = selectedCurrency.code,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            LegendRow(color = currencyAccent, label = strings.positive)
                            LegendRow(color = MaterialTheme.colorScheme.onSurfaceVariant, label = strings.neutral)
                            LegendRow(color = MaterialTheme.colorScheme.outline, label = strings.negative)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Tendencia de precios (Price Trend)
            Text(
                text = strings.priceTrend,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp)
            ) {
                Column {
                    // Chart Canvas with 5 points
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val gridColor = MaterialTheme.colorScheme.primaryContainer
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val paddingX = 30f

                            for (i in 0..3) {
                                val y = h * (i / 3f)
                                drawLine(
                                    color = gridColor,
                                    start = Offset(paddingX, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            if (fiveDayRates.isNotEmpty()) {
                                val minRate = fiveDayRates.minOrNull()?.toFloat() ?: 0f
                                val maxRate = fiveDayRates.maxOrNull()?.toFloat() ?: 100f
                                val range = if (maxRate - minRate == 0f) 1f else (maxRate - minRate)
                                val divisor = if (fiveDayRates.size <= 1) 1f else (fiveDayRates.size - 1).toFloat()

                                val points = fiveDayRates.mapIndexed { index, rate ->
                                    val x = paddingX + (index.toFloat() / divisor) * (w - paddingX - 20f)
                                    val normalizedY = (rate.toFloat() - minRate) / range
                                    val y = h * 0.85f - (normalizedY * (h * 0.7f))
                                    Offset(x, y)
                                }

                                if (fiveDayRates.size >= 2) {
                                    val path = Path().apply {
                                        moveTo(points.first().x, points.first().y)
                                        for (i in 0 until points.size - 1) {
                                            val p1 = points[i]
                                            val p2 = points[i + 1]
                                            val cx = (p1.x + p2.x) / 2f
                                            cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                                        }
                                    }

                                    drawPath(
                                        path = path,
                                        color = currencyAccent,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                points.forEachIndexed { idx, pt ->
                                    val isSelectedPoint = idx == selectedPointIndex
                                    val radius = if (isSelectedPoint) 6.dp.toPx() else 4.dp.toPx()
                                    if (isSelectedPoint) {
                                        drawCircle(
                                            color = currencyAccent.copy(alpha = 0.35f),
                                            radius = 12.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                    drawCircle(color = currencyAccent, radius = radius, center = pt)
                                }
                            }
                        }

                        // Touch Row across 5 columns to tap points directly
                        Row(modifier = Modifier.fillMaxSize()) {
                            fiveDayRates.indices.forEach { idx ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clickable { selectedPointIndex = idx }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selected Point Info Label Panel (Replaces days/months selector)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Fecha",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedDateText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = selectedPrice,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = currencyAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 3: Última actualización
            Text(
                text = strings.lastUpdate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            BcvLogoIcon(size = 36.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BCV USD / EUR",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${strings.bcvScheduleToday}, 15:00 (VET)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = strings.verified,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            BinanceIcon(size = 36.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Binance USDT",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${strings.bcvScheduleToday}, 13:30 (VET)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = strings.verified,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.nextBcvUpdate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${strings.bcvScheduleToday}, 21:30 (VET)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = UsdtYellow,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = strings.methodologyTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. Banco Central de Venezuela (BCV)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = UsdBlue
                    )
                    Text(
                        text = strings.bcvSourceDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "2. Binance P2P USDT",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = UsdtYellow
                    )
                    Text(
                        text = strings.usdtSourceDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
