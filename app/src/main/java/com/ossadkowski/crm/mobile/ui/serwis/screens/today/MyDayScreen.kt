package com.ossadkowski.crm.mobile.ui.serwis.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.serwis.model.MyOrder
import com.ossadkowski.crm.mobile.ui.serwis.components.HeroHeader
import com.ossadkowski.crm.mobile.ui.serwis.components.PhoneFrame
import com.ossadkowski.crm.mobile.ui.serwis.components.RowCard
import com.ossadkowski.crm.mobile.ui.serwis.components.SegmentedToggle
import com.ossadkowski.crm.mobile.ui.serwis.components.StatusPill
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.EmptyState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.ErrorState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.LoadingState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.label
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.statusToken
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import com.ossadkowski.crm.mobile.ui.serwis.theme.StatusToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDayScreen(
    vm: MyDayViewModel = hiltViewModel(),
    onMenuClick: () -> Unit,
    onOrderClick: (orderRegNum: String) -> Unit,
    onNavigatePlan: () -> Unit,
    onCreateOrder: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()

    PhoneFrame(
        floatingActionButton = {
            MyDayFab(onClick = onCreateOrder)
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state is MyDayUiState.Loading,
            onRefresh = { vm.refresh() },
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                MyDayUiState.Loading -> LoadingState()
                is MyDayUiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = { vm.refresh() },
                )
                is MyDayUiState.Success -> MyDayContent(
                    orders = s.orders,
                    filter = s.filter,
                    onMenuClick = onMenuClick,
                    onOrderClick = onOrderClick,
                    onNavigatePlan = onNavigatePlan,
                    onFilterClick = vm::setFilter,
                )
            }
        }
    }
}

/* ----------------------------- content ----------------------------- */

@Composable
private fun MyDayContent(
    orders: List<MyOrder>,
    filter: TaskFilter,
    onMenuClick: () -> Unit,
    onOrderClick: (String) -> Unit,
    onNavigatePlan: () -> Unit,
    onFilterClick: (TaskFilter) -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // 1. Hero
        HeroHeader(
            title = "Dziś masz ${orders.size} ${pluralOrders(orders.size)}",
            dayStamp = formatDayStamp(LocalDate.now()),
            topBar = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OnDarkIconBtn(icon = Icons.Outlined.Menu, contentDescription = "Menu", onClick = onMenuClick)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "SERWIS",
                        style = CrmTheme.type.label.copy(color = palette.onDarkMuted),
                    )
                    Spacer(Modifier.weight(1f))
                    OnDarkIconBtn(
                        icon = Icons.Outlined.Notifications,
                        contentDescription = "Powiadomienia",
                        onClick = {},
                    )
                }
            },
        ) {
            Spacer(Modifier.height(dimens.spacing16))
            // 2. Stat row
            StatRow(orders = orders)
        }

        Spacer(Modifier.height(dimens.spacing16))

        if (orders.isEmpty()) {
            EmptyState(
                text = "Brak zleceń na dziś. Sprawdź jutro.",
                action = {
                    PrimaryGhostBtn(
                        label = "Zobacz plan tygodnia",
                        onClick = onNavigatePlan,
                    )
                },
            )
            Spacer(Modifier.height(dimens.spacing24))
            return@Column
        }

        // 3. Next-order big card
        NextOrderCard(
            order = orders.first(),
            onOpen = { onOrderClick(orders.first().orderRegNum) },
        )

        Spacer(Modifier.height(dimens.spacing20))

        // 4. Plan dnia label + filter chips
        Column(modifier = Modifier.padding(horizontal = dimens.spacing16)) {
            Text(
                text = "Plan dnia",
                style = CrmTheme.type.headline.copy(color = palette.ink),
            )
            Spacer(Modifier.height(dimens.spacing8))
            SegmentedToggle(
                options = listOf("Dziś", "Pilne", "SLA", "Recall"),
                selectedIndex = filter.ordinal,
                onSelect = { idx -> onFilterClick(TaskFilter.values()[idx]) },
            )
        }

        Spacer(Modifier.height(dimens.spacing12))

        // 5. List rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing8),
        ) {
            orders.forEach { o ->
                RowCard(
                    title = o.custName ?: "—",
                    meta = "${o.orderRegNum}${o.orderDate?.let { " · $it" } ?: ""}",
                    accent = o.status.statusToken(),
                    iconStatus = o.status.statusToken(),
                    leading = {
                        Icon(
                            imageVector = Icons.Outlined.Build,
                            contentDescription = null,
                            tint = CrmTheme.colors.brand.text,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingValue = null,
                    trailingLabel = o.status.label().uppercase(),
                    onClick = { onOrderClick(o.orderRegNum) },
                )
            }
        }

        Spacer(Modifier.height(dimens.spacing32 + dimens.spacing32))
    }
}

/* ----------------------------- pieces ----------------------------- */

@Composable
private fun StatRow(orders: List<MyOrder>) {
    val total = orders.size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CrmTheme.dimens.spacing4),
        horizontalArrangement = Arrangement.spacedBy(CrmTheme.dimens.spacing8),
    ) {
        StatTile(value = total.toString(), label = "Pilne SLA", token = StatusToken.BAD, modifier = Modifier.weight(1f))
        StatTile(value = "0", label = "W trakcie", token = StatusToken.WARN, modifier = Modifier.weight(1f))
        StatTile(value = "0", label = "Zakończ.", token = StatusToken.OK, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    token: StatusToken,
    modifier: Modifier = Modifier,
) {
    val palette = CrmTheme.colors
    val tone = when (token) {
        StatusToken.BAD -> palette.bad.text
        StatusToken.WARN -> palette.warn.text
        StatusToken.OK -> palette.ok.text
        StatusToken.INFO -> palette.info.text
        StatusToken.BRAND -> palette.brand.text
        StatusToken.NEUTRAL -> palette.neutral.text
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CrmTheme.dimens.radius14))
            .background(palette.onDarkGlass)
            .padding(CrmTheme.dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(CrmTheme.dimens.spacing4),
    ) {
        Text(
            text = value,
            style = CrmTheme.type.display.copy(
                color = tone,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = label.uppercase(),
            style = CrmTheme.type.label.copy(color = palette.onDarkMuted),
        )
    }
}

@Composable
private fun NextOrderCard(
    order: MyOrder,
    onOpen: () -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.spacing16),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius20))
                .background(palette.surface)
                .border(
                    width = dimens.borderThin,
                    color = palette.line,
                    shape = RoundedCornerShape(dimens.radius20),
                )
                .clickable(onClick = onOpen)
                .padding(dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing8),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spacing8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(token = order.status.statusToken(), text = order.status.label())
                Text(
                    text = "Następne zlecenie".uppercase(),
                    style = CrmTheme.type.label.copy(color = palette.muted),
                )
            }
            Text(
                text = order.custName ?: "—",
                style = CrmTheme.type.title.copy(color = palette.ink),
            )
            Text(
                text = order.orderRegNum,
                style = CrmTheme.type.mono.copy(color = palette.muted),
            )
            Spacer(Modifier.height(dimens.spacing4))
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
                GhostActionBtn(icon = Icons.Outlined.PlayArrow, label = "Start", onClick = onOpen)
                GhostActionBtn(icon = Icons.Outlined.ArrowBack, label = "Karta", onClick = onOpen)
                GhostActionBtn(icon = Icons.Outlined.Phone, label = "Tel.", onClick = {})
            }
        }
    }
}

@Composable
private fun GhostActionBtn(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
            .background(palette.surface100)
            .clickable(onClick = onClick)
            .padding(horizontal = CrmTheme.dimens.spacing12, vertical = CrmTheme.dimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CrmTheme.dimens.spacing4),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.ink,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            style = CrmTheme.type.label.copy(color = palette.ink),
        )
    }
}

@Composable
private fun OnDarkIconBtn(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    Box(
        modifier = Modifier
            .size(CrmTheme.dimens.hamSize)
            .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
            .background(palette.onDarkGlass)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.onDark,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun MyDayFab(onClick: () -> Unit) {
    val palette = CrmTheme.colors
    Box(
        modifier = Modifier
            .size(CrmTheme.dimens.fab)
            .clip(RoundedCornerShape(CrmTheme.dimens.radius17))
            .background(palette.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Nowe zlecenie",
            tint = palette.onPrimary,
        )
    }
}

@Composable
private fun PrimaryGhostBtn(
    label: String,
    onClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(CrmTheme.dimens.radius14))
            .border(
                width = CrmTheme.dimens.borderMed,
                color = palette.primary,
                shape = RoundedCornerShape(CrmTheme.dimens.radius14),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = CrmTheme.dimens.spacing20, vertical = CrmTheme.dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = CrmTheme.type.label.copy(color = palette.primaryDeep),
        )
    }
}

/* ----------------------------- helpers ----------------------------- */

private fun formatDayStamp(date: LocalDate): String {
    val locale = Locale("pl")
    val pattern = DateTimeFormatter.ofPattern("EEEE · d MMMM yyyy", locale)
    return date.format(pattern)
}

private fun pluralOrders(n: Int): String = when {
    n == 1 -> "zlecenie"
    n % 10 in 2..4 && (n % 100 !in 12..14) -> "zlecenia"
    else -> "zleceń"
}

@Suppress("UnusedPrivateMember")
private fun unusedColorRef(): Color = Color.Transparent
