package com.ossadkowski.crm.mobile.ui.serwis.screens.machines

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.serwis.model.Machine
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachinesScreen(
    vm: MachinesViewModel = hiltViewModel(),
    onMenuClick: () -> Unit,
    onMachineClick: (serial: String) -> Unit,
    onScanClick: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()

    PhoneFrame(
        topBar = { MachinesTopBar(onMenuClick = onMenuClick) },
        floatingActionButton = { ScanFab(onScanClick) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state is MachinesUiState.Loading,
            onRefresh = { vm.refresh() },
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                MachinesUiState.Loading -> LoadingState()
                is MachinesUiState.Error -> ErrorState(s.message, onRetry = { vm.refresh() })
                is MachinesUiState.Success -> MachinesContent(
                    state = s,
                    onMachineClick = onMachineClick,
                    onQueryChange = vm::setQuery,
                    onFilterChange = vm::setWarrantyFilter,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MachinesContent(
    state: MachinesUiState.Success,
    onMachineClick: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterChange: (MachineWarrantyFilter) -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = { Text("Szukaj maszyny…", style = CrmTheme.type.body) },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = palette.muted)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface,
                focusedIndicatorColor = palette.primary,
                unfocusedIndicatorColor = palette.line,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16, vertical = dimens.spacing8),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
        ) {
            SegmentedToggle(
                options = listOf("Wszystkie", "Z gwarancją", "Bez gwarancji"),
                selectedIndex = state.warrantyFilter.ordinal,
                onSelect = { idx -> onFilterChange(MachineWarrantyFilter.values()[idx]) },
            )
        }

        Spacer(Modifier.height(dimens.spacing12))

        val filtered = state.filtered
        if (filtered.isEmpty()) {
            EmptyState(text = "Brak maszyn pasujących do kryteriów.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = dimens.spacing16,
                    end = dimens.spacing16,
                    top = dimens.spacing4,
                    bottom = dimens.spacing32 + dimens.spacing24,
                ),
                verticalArrangement = Arrangement.spacedBy(dimens.spacing8),
            ) {
                items(filtered, key = { it.id }) { m ->
                    MachineRow(machine = m, onClick = onMachineClick)
                }
            }
        }
    }
}

@Composable
private fun MachineRow(machine: Machine, onClick: (String) -> Unit) {
    val title = listOfNotNull(machine.marka, machine.model)
        .joinToString(" ")
        .ifBlank { "Maszyna" }
    val meta = "SN: ${machine.numerSeryjny ?: "-"} · ${machine.accountNum ?: "-"}"
    val warrantyToken = machine.warrantyStatus.statusToken()
    val pillText = machine.warrantyStatus.label()
    Box(modifier = Modifier.fillMaxWidth()) {
        RowCard(
            title = title,
            meta = meta,
            leading = {
                Icon(
                    imageVector = Icons.Outlined.Build,
                    contentDescription = null,
                    tint = CrmTheme.colors.brand.text,
                    modifier = Modifier.size(20.dp),
                )
            },
            iconStatus = StatusToken.BRAND,
            onClick = { machine.numerSeryjny?.let(onClick) },
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = CrmTheme.dimens.spacing12),
        ) {
            StatusPill(token = warrantyToken, text = pillText)
        }
    }
}

@Composable
private fun MachinesTopBar(onMenuClick: () -> Unit) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surface),
    ) {
        Spacer(Modifier.height(dimens.statusBarPad))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = dimens.spacing4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Outlined.Menu, contentDescription = "Menu", tint = palette.ink)
            }
            Text(
                text = "Maszyny",
                style = CrmTheme.type.headline.copy(color = palette.ink),
                modifier = Modifier.padding(start = dimens.spacing4),
            )
        }
    }
}

@Composable
private fun ScanFab(onClick: () -> Unit) {
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
            contentDescription = "Skanuj",
            tint = palette.onPrimary,
        )
    }
}
