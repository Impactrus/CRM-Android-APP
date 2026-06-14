package com.ossadkowski.crm.mobile.ui.nawozy.screens.lista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieNawozy
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieStatus
import com.ossadkowski.crm.mobile.ui.nawozy.common.formatPln

/** Status chips shown in the filter row (UNKNOWN is internal-only and excluded). */
private val FILTERABLE_STATUSES = ZamowienieStatus.entries.filter { it != ZamowienieStatus.UNKNOWN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZamowieniaListScreen(
    onBack: () -> Unit,
    onCreateNew: () -> Unit,
    onOrderClick: (Long) -> Unit,
    viewModel: ZamowieniaListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Refresh when returning to the list (e.g. after placing an order). Skip the
    // first resume — init() already loaded. rememberSaveable so a rotation doesn't
    // re-arm the skip and swallow a legitimate return-refresh.
    val firstResume = rememberSaveable { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        if (firstResume.value) firstResume.value = false else viewModel.refresh()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zamówienia nawozy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Powrót do menu")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNew) {
                Icon(Icons.Filled.Add, contentDescription = "Nowe zamówienie")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            StatusFilterRow(
                selected = state.statusFilter,
                onSelect = viewModel::setStatusFilter,
            )
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.loading -> CenteredBox { CircularProgressIndicator() }
                    state.error != null -> ErrorState(message = state.error!!, onRetry = viewModel::retry)
                    state.items.isEmpty() -> EmptyState()
                    else -> OrdersList(items = state.items, onOrderClick = onOrderClick)
                }
            }
        }
    }
}

@Composable
private fun StatusFilterRow(
    selected: ZamowienieStatus?,
    onSelect: (ZamowienieStatus?) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Wszystkie") },
            )
        }
        items(FILTERABLE_STATUSES) { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelect(status) },
                label = { Text(status.label) },
            )
        }
    }
}

@Composable
private fun OrdersList(
    items: List<ZamowienieNawozy>,
    onOrderClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.id }) { order ->
            OrderCard(order = order, onClick = { onOrderClick(order.id) })
        }
    }
}

@Composable
private fun OrderCard(order: ZamowienieNawozy, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = order.nrZamowienia.ifBlank { "—" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                StatusPill(order.status)
            }
            Text(
                text = order.kontrahentNazwa,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Poz.: ${order.iloscTowarow}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatPln(order.wartoscNetto),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(status: ZamowienieStatus) {
    val color = statusColor(status)
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f),
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

private fun statusColor(status: ZamowienieStatus): Color = when (status) {
    ZamowienieStatus.KOSZYK -> Color(0xFF6B7280)
    ZamowienieStatus.DRAFT -> Color(0xFF2563EB)
    ZamowienieStatus.CZEKA_NA_ZATWIERDZENIE -> Color(0xFFD97706)
    ZamowienieStatus.WYSLANY_OCZEKUJE_AX -> Color(0xFF7C3AED)
    ZamowienieStatus.WYSLANY -> Color(0xFF059669)
    ZamowienieStatus.ANULOWANY -> Color(0xFF6B7280)
    ZamowienieStatus.ODRZUCONE_KIEROWNIK -> Color(0xFFDC2626)
    ZamowienieStatus.UNKNOWN -> Color(0xFF9CA3AF)
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun EmptyState() {
    CenteredBox {
        Text(
            text = "Brak zamówień nawozowych.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    CenteredBox {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            androidx.compose.material3.TextButton(onClick = onRetry) { Text("Ponów") }
        }
    }
}
