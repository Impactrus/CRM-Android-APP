package com.ossadkowski.crm.mobile.ui.serwis.screens.parts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.NewPartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartStatus
import com.ossadkowski.crm.mobile.ui.serwis.components.PhoneFrame
import com.ossadkowski.crm.mobile.ui.serwis.components.RowCard
import com.ossadkowski.crm.mobile.ui.serwis.components.StatusPill
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.EmptyState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.LoadingState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.label
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.statusToken
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import com.ossadkowski.crm.mobile.ui.serwis.theme.StatusToken
import kotlinx.coroutines.launch

/**
 * Części (Parts) screen — offline list of parts requests grouped by status.
 *
 * Backend has no parts endpoints yet, so this is purely Room-backed; the
 * [PartsViewModel] writes to and observes the local DB.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartsScreen(
    vm: PartsViewModel = hiltViewModel(),
    onMenuClick: () -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddSheet by remember { mutableStateOf(false) }
    var actionTarget by remember { mutableStateOf<PartRequest?>(null) }

    // Surface errors as snackbar then clear.
    LaunchedEffect(state.error) {
        val msg = state.error
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            vm.clearError()
        }
    }

    PhoneFrame(
        topBar = { PartsTopBar(onMenuClick = onMenuClick) },
        floatingActionButton = { AddPartFab(onClick = { showAddSheet = true }) },
        bottomBar = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            FilterChipsRow(
                selected = state.filter,
                onSelect = vm::setFilter,
            )
            CountsStrip(parts = state.parts)
            HorizontalDivider(
                color = CrmTheme.colors.line,
                thickness = CrmTheme.dimens.borderThin,
            )

            when {
                state.isLoading -> LoadingState()
                else -> {
                    val visible = remember(state.parts, state.filter) {
                        if (state.filter == null) state.parts
                        else state.parts.filter { it.status == state.filter }
                    }
                    if (visible.isEmpty() && state.parts.isEmpty()) {
                        EmptyState(
                            text = "Brak zapotrzebowania na części",
                            action = {
                                AddFirstPartButton(onClick = { showAddSheet = true })
                            },
                        )
                    } else if (visible.isEmpty()) {
                        EmptyState(text = "Brak części pasujących do filtra")
                    } else {
                        PartsList(
                            parts = visible,
                            onPartClick = { actionTarget = it },
                            onPartLongClick = { actionTarget = it },
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddPartSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { req ->
                vm.add(req)
                showAddSheet = false
            },
        )
    }

    val target = actionTarget
    if (target != null) {
        PartActionSheet(
            part = target,
            onDismiss = { actionTarget = null },
            onStatus = { status ->
                vm.changeStatus(target.id, status)
                actionTarget = null
            },
            onDelete = {
                vm.remove(target.id)
                actionTarget = null
            },
        )
    }
}

/* ------------------------------ Topbar / FAB ------------------------------ */

@Composable
private fun PartsTopBar(onMenuClick: () -> Unit) {
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
                text = "Części",
                style = CrmTheme.type.headline.copy(color = palette.ink),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = dimens.spacing4),
            )
            IconButton(onClick = { /* no-op */ }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Więcej", tint = palette.ink)
            }
        }
        HorizontalDivider(color = palette.line, thickness = dimens.borderThin)
    }
}

@Composable
private fun AddPartFab(onClick: () -> Unit) {
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
            contentDescription = "Dodaj część",
            tint = palette.onPrimary,
        )
    }
}

/* --------------------------- Filter / counts ---------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    selected: PartStatus?,
    onSelect: (PartStatus?) -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.spacing8),
        contentPadding = PaddingValues(horizontal = dimens.spacing16),
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing8),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Wszystkie", style = CrmTheme.type.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = palette.primary,
                    selectedLabelColor = palette.onPrimary,
                ),
            )
        }
        items(PartStatus.values().toList()) { s ->
            FilterChip(
                selected = selected == s,
                onClick = { onSelect(s) },
                label = { Text(s.label(), style = CrmTheme.type.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = palette.primary,
                    selectedLabelColor = palette.onPrimary,
                ),
            )
        }
    }
}

@Composable
private fun CountsStrip(parts: List<PartRequest>) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    val requested = parts.count { it.status == PartStatus.REQUESTED }
    val ordered = parts.count { it.status == PartStatus.ORDERED }
    val inTransit = parts.count { it.status == PartStatus.IN_TRANSIT }
    Text(
        text = "$requested zapotrzebowań · $ordered zamówionych · $inTransit w drodze",
        style = CrmTheme.type.body.copy(color = palette.muted),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.spacing16, vertical = dimens.spacing8),
    )
}

/* -------------------------------- List --------------------------------- */

@Composable
private fun PartsList(
    parts: List<PartRequest>,
    onPartClick: (PartRequest) -> Unit,
    onPartLongClick: (PartRequest) -> Unit,
) {
    val dimens = CrmTheme.dimens
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = dimens.spacing16,
            end = dimens.spacing16,
            top = dimens.spacing8,
            bottom = dimens.spacing32 + dimens.spacing24,
        ),
        verticalArrangement = Arrangement.spacedBy(dimens.spacing8),
    ) {
        items(parts, key = { it.id }) { p ->
            PartRow(
                part = p,
                onClick = { onPartClick(p) },
                onLongClick = { onPartLongClick(p) },
            )
        }
    }
}

@Composable
private fun PartRow(
    part: PartRequest,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    val meta = buildString {
        append("${formatQty(part.quantity)} ${part.unit}")
        part.partNumber?.let { append(" · $it") }
        part.orderRegNum?.let { append(" · $it") }
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        RowCard(
            title = part.name,
            meta = meta,
            leading = {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = palette.brand.text,
                    modifier = Modifier.size(20.dp),
                )
            },
            iconStatus = StatusToken.BRAND,
            onClick = onClick,
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = CrmTheme.dimens.spacing12),
        ) {
            StatusPill(token = part.status.statusToken(), text = part.status.label())
        }
    }
}

@Composable
private fun AddFirstPartButton(onClick: () -> Unit) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(dimens.radius14))
            .background(palette.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.spacing20, vertical = dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            tint = palette.onPrimary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.size(dimens.spacing8))
        Text(
            text = "Dodaj pierwszą część",
            style = CrmTheme.type.label.copy(color = palette.onPrimary),
        )
    }
}

/* ------------------------------ Sheets --------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPartSheet(
    onDismiss: () -> Unit,
    onAdd: (NewPartRequest) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens

    var name by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("szt") }
    var orderRegNum by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val canSubmit = name.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
        ) {
            Text(
                text = "Nowa część",
                style = CrmTheme.type.headline.copy(color = palette.ink),
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nazwa części*") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = sheetFieldColors(),
            )
            OutlinedTextField(
                value = partNumber,
                onValueChange = { partNumber = it },
                label = { Text("Numer części") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = sheetFieldColors(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { txt ->
                        // Allow empty + numeric/decimal only.
                        if (txt.isEmpty() || txt.matches(Regex("^\\d*(\\.\\d*)?$"))) {
                            quantityText = txt
                        }
                    },
                    label = { Text("Ilość") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(2f),
                    colors = sheetFieldColors(),
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Jedn.") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = sheetFieldColors(),
                )
            }
            OutlinedTextField(
                value = orderRegNum,
                onValueChange = { orderRegNum = it },
                label = { Text("Powiązane zlecenie") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = sheetFieldColors(),
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notatki") },
                minLines = 2,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                colors = sheetFieldColors(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius14))
                    .background(if (canSubmit) palette.primary else palette.line)
                    .clickable(enabled = canSubmit) {
                        onAdd(
                            NewPartRequest(
                                orderRegNum = orderRegNum.ifBlank { null },
                                jobCardNum = null,
                                name = name.trim(),
                                partNumber = partNumber.ifBlank { null },
                                quantity = quantityText.toDoubleOrNull() ?: 1.0,
                                unit = unit.ifBlank { "szt" },
                                notes = notes.ifBlank { null },
                            )
                        )
                        scope.launch { sheetState.hide() }
                    }
                    .padding(vertical = dimens.spacing16),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Dodaj",
                    style = CrmTheme.type.label.copy(
                        color = if (canSubmit) palette.onPrimary else palette.muted,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartActionSheet(
    part: PartRequest,
    onDismiss: () -> Unit,
    onStatus: (PartStatus) -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing4),
        ) {
            Text(
                text = part.name,
                style = CrmTheme.type.headline.copy(color = palette.ink),
            )
            Text(
                text = "Zmień status",
                style = CrmTheme.type.caption.copy(color = palette.muted),
                modifier = Modifier.padding(bottom = dimens.spacing8),
            )
            PartStatus.values().forEach { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStatus(s) }
                        .padding(vertical = dimens.spacing12, horizontal = dimens.spacing8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusPill(token = s.statusToken(), text = s.label())
                    Spacer(Modifier.size(dimens.spacing12))
                    if (s == part.status) {
                        Text(
                            text = "obecnie",
                            style = CrmTheme.type.caption.copy(color = palette.muted),
                        )
                    }
                }
            }
            HorizontalDivider(
                color = palette.line,
                thickness = dimens.borderThin,
                modifier = Modifier.padding(vertical = dimens.spacing8),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDelete)
                    .padding(vertical = dimens.spacing12, horizontal = dimens.spacing8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = palette.bad.dot,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(dimens.spacing8))
                Text(
                    text = "Usuń",
                    style = CrmTheme.type.body.copy(
                        color = palette.bad.text,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

/* ------------------------------ Helpers -------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun sheetFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = CrmTheme.colors.surface,
    unfocusedContainerColor = CrmTheme.colors.surface,
    focusedIndicatorColor = CrmTheme.colors.primary,
    unfocusedIndicatorColor = CrmTheme.colors.line,
)

private fun formatQty(qty: Double): String =
    if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
