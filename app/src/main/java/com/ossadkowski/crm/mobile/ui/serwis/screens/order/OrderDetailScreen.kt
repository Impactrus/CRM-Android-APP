package com.ossadkowski.crm.mobile.ui.serwis.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.serwis.model.JobCard
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderLogEntry
import com.ossadkowski.crm.mobile.domain.serwis.model.ServiceOrderSummary
import com.ossadkowski.crm.mobile.ui.serwis.components.BottomCta
import com.ossadkowski.crm.mobile.ui.serwis.components.LiveTimerCard
import com.ossadkowski.crm.mobile.ui.serwis.components.LiveTimerState
import com.ossadkowski.crm.mobile.ui.serwis.components.PhoneFrame
import com.ossadkowski.crm.mobile.ui.serwis.components.RowCard
import com.ossadkowski.crm.mobile.ui.serwis.components.StatusPill
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.EmptyState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.ErrorState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.LoadingState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.TopBarLight
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.label
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.statusToken
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import com.ossadkowski.crm.mobile.ui.serwis.theme.StatusToken
import kotlinx.coroutines.launch

@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    onWorkCardClick: (orderNum: String, cardNum: String) -> Unit,
    vm: OrderDetailViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    PhoneFrame(
        topBar = {
            TopBarLight(
                title = "Zlecenie",
                subtitle = vm.orderNum,
                onBack = onBack,
            )
        },
        bottomBar = {
            val firstCard = (state as? OrderDetailUiState.Success)
                ?.jobCards?.firstOrNull()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CrmTheme.colors.bg)
                    .padding(horizontal = CrmTheme.dimens.spacing16, vertical = CrmTheme.dimens.spacing12),
                horizontalArrangement = Arrangement.spacedBy(CrmTheme.dimens.spacing8),
            ) {
                GhostCtaBtn(
                    label = "Karta pracy",
                    enabled = firstCard != null,
                    onClick = {
                        firstCard?.let { card ->
                            onWorkCardClick(vm.orderNum, card.mpeOrderJobCardNum)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                Box(modifier = Modifier.weight(1f)) {
                    BottomCta(
                        label = "Zakończ czynność",
                        onClick = {
                            scope.launch { snackbar.showSnackbar("Wkrótce") }
                        },
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                OrderDetailUiState.Loading -> LoadingState()
                is OrderDetailUiState.Error -> ErrorState(s.message, onRetry = vm::refresh)
                is OrderDetailUiState.Success -> OrderDetailContent(
                    state = s,
                    onTabSelect = vm::selectTab,
                )
            }
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun OrderDetailContent(
    state: OrderDetailUiState.Success,
    onTabSelect: (OrderDetailTab) -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    var localTimerState by remember { mutableStateOf(LiveTimerState.IDLE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimens.spacing16),
    ) {
        Spacer(Modifier.height(dimens.spacing12))

        // Live timer placeholder (IDLE)
        LiveTimerCard(
            state = localTimerState,
            elapsedHhMmSs = "00:00:00",
            stepLabel = "Brak aktywnej czynności",
            meta = "Twój timer",
            onPause = { localTimerState = LiveTimerState.PAUSED },
            onBreak = {},
            onStop = { localTimerState = LiveTimerState.IDLE },
        )

        Spacer(Modifier.height(dimens.spacing12))

        // Header card
        OrderHeaderCard(state.order)

        Spacer(Modifier.height(dimens.spacing12))

        // Tabs
        val tabs = listOf(
            OrderDetailTab.CZYNNOSCI to ("Czynności" to state.jobCards.size),
            OrderDetailTab.CZESCI to ("Części" to 0),
            OrderDetailTab.PLIKI to ("Pliki" to 0),
            OrderDetailTab.LOG to ("Log" to state.log.size),
        )
        TabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            containerColor = palette.surface,
            contentColor = palette.ink,
        ) {
            tabs.forEach { (tab, lbl) ->
                Tab(
                    selected = tab == state.selectedTab,
                    onClick = { onTabSelect(tab) },
                    text = {
                        Text(
                            text = "${lbl.first} (${lbl.second})",
                            style = CrmTheme.type.label,
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(dimens.spacing12))

        when (state.selectedTab) {
            OrderDetailTab.CZYNNOSCI -> CzynnosciTab(state.jobCards)
            OrderDetailTab.CZESCI -> Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                EmptyState(text = "Brak części dla MVP")
            }
            OrderDetailTab.PLIKI -> Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                EmptyState(text = "Brak plików")
            }
            OrderDetailTab.LOG -> LogTab(state.log)
        }

        Spacer(Modifier.height(dimens.spacing32 + dimens.spacing32))
    }
}

@Composable
private fun OrderHeaderCard(order: ServiceOrderSummary) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
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
            .padding(dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(dimens.spacing8),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing6)) {
            StatusPill(token = order.status.statusToken(), text = order.status.label())
            if (order.isWarranty == true) {
                StatusPill(token = StatusToken.OK, text = "Gwarancja")
            }
        }
        Text(
            text = listOfNotNull(order.numerSeryjny, order.custName).joinToString(" · ")
                .ifBlank { order.orderRegNum },
            style = CrmTheme.type.title.copy(color = palette.ink, fontWeight = FontWeight.Bold),
        )
        Text(
            text = order.custAccount ?: "-",
            style = CrmTheme.type.body.copy(color = palette.muted),
        )
    }
}

@Composable
private fun CzynnosciTab(jobCards: List<JobCard>) {
    val dimens = CrmTheme.dimens
    if (jobCards.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            EmptyState(text = "Brak kart pracy")
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
        jobCards.forEach { card ->
            val arrangements = card.arrangements?.split('\n')?.filter { it.isNotBlank() }
                ?: emptyList()
            if (arrangements.isEmpty()) {
                RowCard(
                    title = "Karta ${card.mpeOrderJobCardNum}",
                    meta = card.serviceType ?: "-",
                    iconStatus = StatusToken.BRAND,
                    leading = {
                        Icon(
                            imageVector = Icons.Outlined.Build,
                            contentDescription = null,
                            tint = CrmTheme.colors.brand.text,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )
            } else {
                arrangements.forEach { line ->
                    RowCard(
                        title = line,
                        meta = "Karta ${card.mpeOrderJobCardNum}",
                        iconStatus = StatusToken.BRAND,
                        leading = {
                            Icon(
                                imageVector = Icons.Outlined.Build,
                                contentDescription = null,
                                tint = CrmTheme.colors.brand.text,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LogTab(log: List<OrderLogEntry>) {
    val dimens = CrmTheme.dimens
    if (log.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            EmptyState(text = "Brak wpisów w logu")
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
        log.forEach { entry ->
            RowCard(
                title = entry.description ?: "-",
                meta = "${entry.createdBy ?: "-"} · ${entry.createdAt?.toString() ?: "-"}",
                iconStatus = StatusToken.INFO,
                leading = {
                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = null,
                        tint = CrmTheme.colors.info.text,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun GhostCtaBtn(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = CrmTheme.colors
    val alpha = if (enabled) 1f else 0.5f
    Row(
        modifier = modifier
            .height(CrmTheme.dimens.bottomCta)
            .clip(RoundedCornerShape(CrmTheme.dimens.radius17))
            .background(palette.surface)
            .border(
                width = CrmTheme.dimens.borderMed,
                color = palette.primary,
                shape = RoundedCornerShape(CrmTheme.dimens.radius17),
            )
            .let { if (enabled) it.clickable(onClick = onClick) else it }
            .padding(horizontal = CrmTheme.dimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            tint = palette.primaryDeep.copy(alpha = alpha),
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(CrmTheme.dimens.spacing8))
        Text(
            text = label.uppercase(),
            style = CrmTheme.type.label.copy(color = palette.primaryDeep.copy(alpha = alpha)),
        )
    }
}

@Suppress("UnusedPrivateMember")
@Composable
private fun PaddingValuesUnused(): PaddingValues = PaddingValues(0.dp)

@Suppress("UnusedPrivateMember")
@Composable
private fun ParametersUnused() {
    LaunchedEffect(Unit) { /* placeholder to keep import set used by future expansion */ }
}
