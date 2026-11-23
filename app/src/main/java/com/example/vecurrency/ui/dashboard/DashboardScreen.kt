package com.example.vecurrency.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vecurrency.domain.model.Currency
import com.example.vecurrency.domain.model.ExchangeRate
import com.example.vecurrency.domain.model.RateStatus
import com.example.vecurrency.ui.components.BinanceIcon
import com.example.vecurrency.ui.components.EurFlagIcon
import com.example.vecurrency.ui.components.UsFlagIcon
import com.example.vecurrency.ui.theme.AppStrings
import com.example.vecurrency.ui.theme.EurPurple
import com.example.vecurrency.ui.theme.SuccessGreen
import com.example.vecurrency.ui.theme.TextMuted
import com.example.vecurrency.ui.theme.UsdBlue
import com.example.vecurrency.ui.theme.UsdtYellow
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    rates: List<ExchangeRate>,
    isRefreshing: Boolean,
    strings: AppStrings,
    onRefresh: () -> Unit,
    onShareSnapshot: () -> Unit,
    userMessage: String?,
    onUserMessageShown: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(userMessage) {
        if (userMessage != null) {
            snackbarHostState.showSnackbar(userMessage)
            onUserMessageShown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HeaderSection(
                        isRefreshing = isRefreshing,
                        strings = strings,
                        onRefresh = onRefresh,
                        onShareSnapshot = onShareSnapshot,
                        onOpenSettings = onOpenSettings
                    )
                }

                items(rates, key = { it.currency.code }) { rate ->
                    RedesignedRateCard(
                        rate = rate,
                        strings = strings,
                        onCopyRate = { rateText ->
                            clipboardManager.setText(AnnotatedString(rateText))
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    RefreshInfoPanels(rates = rates, strings = strings)
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HeaderSection(
    isRefreshing: Boolean,
    strings: AppStrings,
    onRefresh: () -> Unit,
    onShareSnapshot: () -> Unit,
    onOpenSettings: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strings.appTitle,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = strings.appSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onShareSnapshot) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir captura",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 4.dp),
                    color = UsdBlue,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar tasas",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { onOpenSettings?.invoke() }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = strings.settingsTitle,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RedesignedRateCard(
    rate: ExchangeRate,
    strings: AppStrings,
    onCopyRate: (String) -> Unit
) {
    val isAvailable = rate.status != RateStatus.UNAVAILABLE && rate.vesRate > BigDecimal.ZERO

    val accentColor = when (rate.currency) {
        Currency.USD -> UsdBlue
        Currency.EUR -> EurPurple
        Currency.USDT -> UsdtYellow
        Currency.VES -> MaterialTheme.colorScheme.onSurface
    }

    val formattedTime = remember(rate.fetchTimestamp) {
        val caracasZone = ZoneId.of("America/Caracas")
        val zdt = rate.fetchTimestamp.atZone(caracasZone)
        DateTimeFormatter.ofPattern("HH:mm '(VET)'").format(zdt)
    }

    val formattedPrice = remember(rate.vesRate, isAvailable) {
        if (!isAvailable) "Bs. --"
        else {
            val scaled = rate.vesRate.setScale(2, java.math.RoundingMode.HALF_UP)
            "Bs. ${scaled.toPlainString().replace('.', ',')}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                if (isAvailable) {
                    onCopyRate(rate.vesRate.toPlainString())
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp)
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
                        when (rate.currency) {
                            Currency.USD -> UsFlagIcon(size = 36.dp)
                            Currency.EUR -> EurFlagIcon(size = 36.dp)
                            Currency.USDT -> BinanceIcon(size = 36.dp)
                            Currency.VES -> {}
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = rate.currency.code,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = rate.source.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formattedPrice,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAvailable) "${strings.updated}: $formattedTime" else "Estado: ${strings.unavailable}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isAvailable) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = strings.copyRate,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RefreshInfoPanels(rates: List<ExchangeRate>, strings: AppStrings) {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L)
            now = Instant.now()
        }
    }

    val nextBcvSlot = remember(now) {
        val caracasZone = ZoneId.of("America/Caracas")
        val nowZdt = now.atZone(caracasZone)
        val today = nowZdt.toLocalDate()

        val slots = listOf(
            today.atTime(9, 30).atZone(caracasZone),
            today.atTime(15, 0).atZone(caracasZone),
            today.atTime(21, 30).atZone(caracasZone)
        )

        slots.firstOrNull { it.isAfter(nowZdt) }
            ?: today.plusDays(1).atTime(9, 30).atZone(caracasZone)
    }

    val bcvFormattedTime = remember(nextBcvSlot) {
        DateTimeFormatter.ofPattern("HH:mm '(VET)'").format(nextBcvSlot)
    }

    val nextUsdtSlot = remember(now) {
        val caracasZone = ZoneId.of("America/Caracas")
        val nowZdt = now.atZone(caracasZone)
        val today = nowZdt.toLocalDate()

        val slots = listOf(
            today.atTime(0, 0).atZone(caracasZone),
            today.atTime(4, 0).atZone(caracasZone),
            today.atTime(8, 0).atZone(caracasZone),
            today.atTime(12, 0).atZone(caracasZone),
            today.atTime(16, 0).atZone(caracasZone),
            today.atTime(20, 0).atZone(caracasZone)
        )

        slots.firstOrNull { it.isAfter(nowZdt) }
            ?: today.plusDays(1).atTime(0, 0).atZone(caracasZone)
    }

    val usdtFormattedTime = remember(nextUsdtSlot) {
        DateTimeFormatter.ofPattern("HH:mm '(VET)'").format(nextUsdtSlot)
    }

    val usdtCountdownText = remember(usdtFormattedTime, strings) {
        "${strings.bcvScheduleToday}, $usdtFormattedTime"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendario",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.nextBcvUpdate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${strings.bcvScheduleToday}, $bcvFormattedTime",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = UsdtYellow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        BinanceIcon(size = 24.dp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.nextUsdtUpdate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = usdtCountdownText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EurPurple,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
