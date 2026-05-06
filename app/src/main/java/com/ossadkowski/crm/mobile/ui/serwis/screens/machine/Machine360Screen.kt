package com.ossadkowski.crm.mobile.ui.serwis.screens.machine

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.serwis.model.Machine
import com.ossadkowski.crm.mobile.domain.serwis.model.MachineHistoryEntry
import com.ossadkowski.crm.mobile.ui.serwis.components.PhoneFrame
import com.ossadkowski.crm.mobile.ui.serwis.components.RowCard
import com.ossadkowski.crm.mobile.ui.serwis.components.StatusPill
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.EmptyState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.ErrorState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.LoadingState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.label
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.statusToken
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

@Composable
fun Machine360Screen(
    vm: Machine360ViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOrderClick: (orderRegNum: String) -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    PhoneFrame { padding ->
        when (val s = state) {
            Machine360UiState.Loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) { LoadingState() }
            is Machine360UiState.Error -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) { ErrorState(message = s.message, onRetry = { vm.refresh() }) }
            is Machine360UiState.Success -> Machine360Content(
                machine = s.machine,
                onBack = onBack,
                onOrderClick = onOrderClick,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun Machine360Content(
    machine: Machine,
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Brand-green hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        bottomStart = dimens.radius30,
                        bottomEnd = dimens.radius30,
                    ),
                )
                .background(
                    Brush.verticalGradient(
                        0f to palette.primary,
                        1f to palette.primaryDeep,
                    ),
                )
                .padding(horizontal = dimens.spacing20)
                .padding(
                    top = dimens.statusBarPad + dimens.spacing16,
                    bottom = dimens.spacing24,
                ),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OnPrimaryIconBtn(Icons.Outlined.ArrowBack, "Wstecz", onClick = onBack)
                    Spacer(Modifier.weight(1f))
                    OnPrimaryIconBtn(Icons.Outlined.MoreVert, "Więcej", onClick = {})
                }
                Spacer(Modifier.height(dimens.spacing12))
                Text(
                    text = listOfNotNull(machine.marka, machine.model)
                        .joinToString(" ").ifBlank { "Maszyna" },
                    style = CrmTheme.type.title.copy(
                        color = palette.onPrimary,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = "SN ${machine.numerSeryjny ?: "-"}",
                    style = CrmTheme.type.mono.copy(color = palette.onPrimary.copy(alpha = 0.85f)),
                )
                Spacer(Modifier.height(dimens.spacing12))
                StatRow360(machine = machine)
            }
        }

        Spacer(Modifier.height(dimens.spacing16))

        // Quick action row
        QuickActions()

        Spacer(Modifier.height(dimens.spacing16))

        // Dane maszyny
        Section(title = "Dane maszyny") {
            KvGrid(
                rows = listOfNotNull(
                    machine.marka?.let { "Marka" to it },
                    machine.model?.let { "Model" to it },
                    machine.typMaszyny?.let { "Typ" to it },
                    machine.rokProdukcji?.let { "Rok produkcji" to it.toString() },
                    machine.gwarancjaOd?.let { "Gwarancja od" to it.toString() },
                    machine.gwarancjaDo?.let { "Gwarancja do" to it.toString() },
                    machine.dataSprzedazy?.let { "Data sprzedaży" to it.toString() },
                    machine.nrRejestracyjny?.let { "Nr rejestracyjny" to it },
                ),
            )
        }

        Spacer(Modifier.height(dimens.spacing16))

        // Historia
        Section(title = "Historia zleceń") {
            if (machine.history.isEmpty()) {
                EmptyState(text = "Brak historii zleceń.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
                    machine.history.forEach { entry ->
                        HistoryRow(entry = entry, onClick = { onOrderClick(entry.orderRegNum) })
                    }
                }
            }
        }

        Spacer(Modifier.height(dimens.spacing32))
    }
}

@Composable
private fun StatRow360(machine: Machine) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing8),
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatTile360(
            value = (machine.totalOrders ?: 0).toString(),
            label = "Zleceń łącznie",
            modifier = Modifier.weight(1f),
        )
        StatTile360(
            value = (machine.openOrders ?: 0).toString(),
            label = "Otwartych",
            modifier = Modifier.weight(1f),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(dimens.radius14))
                .background(palette.onDarkGlass.copy(alpha = 0.3f))
                .padding(dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing4),
        ) {
            StatusPill(
                token = machine.warrantyStatus.statusToken(),
                text = machine.warrantyStatus.label(),
                showDot = false,
            )
            Text(
                text = "GWARANCJA",
                style = CrmTheme.type.label.copy(color = palette.onPrimary),
            )
        }
    }
}

@Composable
private fun StatTile360(value: String, label: String, modifier: Modifier = Modifier) {
    val palette = CrmTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CrmTheme.dimens.radius14))
            .background(palette.onDarkGlass.copy(alpha = 0.3f))
            .padding(CrmTheme.dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(CrmTheme.dimens.spacing2),
    ) {
        Text(
            text = value,
            style = CrmTheme.type.title.copy(
                color = palette.onPrimary,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = label.uppercase(),
            style = CrmTheme.type.label.copy(color = palette.onPrimary.copy(alpha = 0.7f)),
        )
    }
}

@Composable
private fun QuickActions() {
    val dimens = CrmTheme.dimens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.spacing16),
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing8),
    ) {
        QuickAction("Zlecenie", Icons.Outlined.NoteAdd, Modifier.weight(1f), onClick = {})
        QuickAction("Historia", Icons.Outlined.History, Modifier.weight(1f), onClick = {})
        QuickAction("Karta", Icons.Outlined.Numbers, Modifier.weight(1f), onClick = {})
        QuickAction("Galeria", Icons.Outlined.Image, Modifier.weight(1f), onClick = {})
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CrmTheme.dimens.radius14))
            .background(palette.surface)
            .border(
                width = CrmTheme.dimens.borderThin,
                color = palette.line,
                shape = RoundedCornerShape(CrmTheme.dimens.radius14),
            )
            .clickable(onClick = onClick)
            .padding(vertical = CrmTheme.dimens.spacing12),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CrmTheme.dimens.spacing4),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = CrmTheme.type.label.copy(color = palette.ink),
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Column(modifier = Modifier.padding(horizontal = dimens.spacing16)) {
        Text(
            text = title,
            style = CrmTheme.type.headline.copy(color = palette.ink),
        )
        Spacer(Modifier.height(dimens.spacing12))
        content()
    }
}

@Composable
private fun KvGrid(rows: List<Pair<String, String>>) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius14))
            .background(palette.surface)
            .border(
                width = dimens.borderThin,
                color = palette.line,
                shape = RoundedCornerShape(dimens.radius14),
            )
            .padding(dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(dimens.spacing8),
    ) {
        if (rows.isEmpty()) {
            Text(
                text = "Brak danych",
                style = CrmTheme.type.body.copy(color = palette.muted),
            )
        }
        rows.forEach { (k, v) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = k.uppercase(),
                    style = CrmTheme.type.label.copy(color = palette.muted),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = v,
                    style = CrmTheme.type.body.copy(color = palette.ink),
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: MachineHistoryEntry, onClick: () -> Unit) {
    RowCard(
        title = entry.orderRegNum,
        meta = "${entry.orderDate?.toString() ?: "-"} · ${entry.serviceType ?: "-"}",
        accent = entry.status.statusToken(),
        iconStatus = entry.status.statusToken(),
        leading = {
            Icon(
                imageVector = Icons.Outlined.Build,
                contentDescription = null,
                tint = CrmTheme.colors.brand.text,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingLabel = entry.status.label().uppercase(),
        onClick = onClick,
    )
}

@Composable
private fun OnPrimaryIconBtn(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    Box(
        modifier = Modifier
            .size(CrmTheme.dimens.hamSize)
            .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
            .background(palette.onDarkGlass.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.onPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}
