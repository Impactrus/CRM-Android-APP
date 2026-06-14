package com.ossadkowski.crm.mobile.ui.nawozy.screens.koszyk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.nawozy.model.Koszyk
import com.ossadkowski.crm.mobile.domain.nawozy.model.KoszykPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.model.SlownikPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieStatus
import com.ossadkowski.crm.mobile.ui.nawozy.common.LimitBanner
import com.ossadkowski.crm.mobile.ui.nawozy.common.formatPln
import com.ossadkowski.crm.mobile.ui.nawozy.common.formatPlnPerTon
import com.ossadkowski.crm.mobile.ui.nawozy.common.formatTons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KoszykScreen(
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onSubmitted: (ZamowienieStatus) -> Unit,
    viewModel: KoszykViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // Reload the cart when coming back from the product picker (a line may have been
    // added). Skip the first resume — init() already loaded. rememberSaveable so a
    // rotation doesn't re-arm the skip and swallow the return-from-picker reload.
    val firstResume = rememberSaveable { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        if (firstResume.value) firstResume.value = false else viewModel.reload()
        onPauseOrDispose { }
    }

    LaunchedSubmission(state.submittedStatus, onSubmitted, viewModel::consumeSubmission)
    LaunchedMessage(state.message, snackbar, viewModel::consumeMessage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Koszyk") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            state.koszyk?.let { koszyk ->
                SubmitBar(state = state, total = koszyk.wartoscNetto, onToggleRisk = viewModel::toggleRisk, onSubmit = viewModel::submit)
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading && state.koszyk == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null && state.koszyk == null -> Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                state.koszyk != null -> KoszykContent(
                    koszyk = state.koszyk!!,
                    state = state,
                    onPaymentTerm = viewModel::setPaymentTerm,
                    onDlvMode = viewModel::setDlvMode,
                    onDlvTerm = viewModel::setDlvTerm,
                    onAddProduct = onAddProduct,
                    onDeleteLine = viewModel::deleteLine,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KoszykContent(
    koszyk: Koszyk,
    state: KoszykState,
    onPaymentTerm: (String) -> Unit,
    onDlvMode: (String) -> Unit,
    onDlvTerm: (String) -> Unit,
    onAddProduct: () -> Unit,
    onDeleteLine: (Long) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.limitStatus?.let { LimitBanner(it) }

        koszyk.kontrahentNazwa?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Parametry zamówienia", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Łączna ilość", style = MaterialTheme.typography.bodyMedium)
                    Text(formatTons(koszyk.qtyTons), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                SlownikDropdown("Termin płatności", koszyk.paymentTerm, state.paymentTerms, onPaymentTerm)
                SlownikDropdown("Forma dostawy", koszyk.dlvMode, state.dlvModes, onDlvMode)
                SlownikDropdown("Termin dostawy", koszyk.dlvTerm, state.dlvTerms, onDlvTerm)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Pozycje (${koszyk.pozycje.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = onAddProduct) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Dodaj towar", modifier = Modifier.padding(start = 6.dp))
            }
        }

        if (koszyk.pozycje.isEmpty()) {
            Text(
                "Koszyk jest pusty — dodaj nawóz.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            koszyk.pozycje.forEach { line ->
                key(line.lineId) {
                    LineCard(line = line, onDelete = { onDeleteLine(line.lineId) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlownikDropdown(
    label: String,
    selectedKod: String?,
    options: List<SlownikPozycja>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.kod == selectedKod }?.nazwa ?: selectedKod ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.nazwa) },
                    onClick = {
                        expanded = false
                        onSelect(option.kod)
                    },
                )
            }
        }
    }
}

@Composable
private fun LineCard(line: KoszykPozycja, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = line.nazwa.ifBlank { line.itemId },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Usuń pozycję")
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${formatTons(line.qty)} × ${formatPln(line.cenaSprzedazy)}", style = MaterialTheme.typography.bodyMedium)
                Text(formatPln(line.wartoscNetto), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            line.transportPlnT?.let {
                Text(
                    "Transport: ${formatPlnPerTon(it)} (stała stawka)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun SubmitBar(
    state: KoszykState,
    total: Double,
    onToggleRisk: () -> Unit,
    onSubmit: () -> Unit,
) {
    androidx.compose.material3.Surface(tonalElevation = 3.dp) {
        Column(modifier = Modifier.navigationBarsPadding().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Razem netto", style = MaterialTheme.typography.bodyMedium)
                Text(formatPln(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (state.requiresRisk) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.riskAcknowledged, onCheckedChange = { onToggleRisk() })
                    Text(
                        "Rozumiem ryzyko (limit zamrożony/zablokowany)",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Button(
                onClick = onSubmit,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text(if (state.submitting) "Wysyłam…" else "Wyślij zamówienie")
            }
        }
    }
}

@Composable
private fun LaunchedSubmission(
    status: ZamowienieStatus?,
    onSubmitted: (ZamowienieStatus) -> Unit,
    consume: () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(status) {
        status?.let {
            consume()
            onSubmitted(it)
        }
    }
}

@Composable
private fun LaunchedMessage(
    message: String?,
    snackbar: SnackbarHostState,
    consume: () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            consume()
        }
    }
}
