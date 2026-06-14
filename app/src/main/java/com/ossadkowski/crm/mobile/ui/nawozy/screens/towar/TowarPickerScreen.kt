package com.ossadkowski.crm.mobile.ui.nawozy.screens.towar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.nawozy.model.AdresDostawy
import com.ossadkowski.crm.mobile.domain.nawozy.model.MagazynStan
import com.ossadkowski.crm.mobile.domain.nawozy.model.PricingResult
import com.ossadkowski.crm.mobile.domain.nawozy.model.StanMagazynowy
import com.ossadkowski.crm.mobile.domain.nawozy.model.TowarNawoz
import com.ossadkowski.crm.mobile.domain.nawozy.model.WariantLogistyczny
import com.ossadkowski.crm.mobile.ui.nawozy.common.formatKm
import com.ossadkowski.crm.mobile.ui.nawozy.common.formatPln
import com.ossadkowski.crm.mobile.ui.nawozy.common.formatPlnPerTon
import com.ossadkowski.crm.mobile.ui.nawozy.common.formatTons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TowarPickerScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    viewModel: TowarPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.added) {
        if (state.added) {
            viewModel.consumeAdded()
            onAdded()
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wybór towaru (nawozy)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Szukaj nawozu…") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.loading && state.products.isEmpty() ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.error != null && state.products.isEmpty() ->
                        Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center).padding(24.dp))
                    state.products.isEmpty() ->
                        Text("Brak nawozów.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                    else -> LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.products, key = { it.itemId }) { towar ->
                            ProductRow(towar = towar, onClick = { viewModel.selectProduct(towar) })
                        }
                    }
                }
            }
        }
    }

    if (state.selected != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::closeSheet,
            sheetState = sheetState,
        ) {
            ProductSheet(
                state = state,
                onQtyChange = viewModel::setQty,
                onRabatChange = viewModel::setRabat,
                onTargetPriceChange = viewModel::setTargetPrice,
                onSelectAddress = viewModel::selectAddress,
                onCalcLogistics = viewModel::loadWarianty,
                onSelectWariant = viewModel::selectWariant,
                onAdd = viewModel::addToCart,
            )
        }
    }
}

@Composable
private fun ProductRow(towar: TowarNawoz, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = towar.nazwa.ifBlank { towar.itemId },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                towar.producent?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(formatPln(towar.cenaBazowa), style = MaterialTheme.typography.bodyMedium)
            }
            StockBadge(towar.stan)
        }
    }
}

@Composable
private fun StockBadge(stan: StanMagazynowy) {
    val (label, color) = when (stan) {
        StanMagazynowy.DOSTEPNY -> "Dostępny" to Color(0xFF059669)
        StanMagazynowy.MALO -> "Mało" to Color(0xFFD97706)
        StanMagazynowy.BRAK -> "Brak" to Color(0xFFDC2626)
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSheet(
    state: TowarPickerState,
    onQtyChange: (Double) -> Unit,
    onRabatChange: (Double) -> Unit,
    onTargetPriceChange: (Double) -> Unit,
    onSelectAddress: (AdresDostawy) -> Unit,
    onCalcLogistics: () -> Unit,
    onSelectWariant: (WariantLogistyczny) -> Unit,
    onAdd: () -> Unit,
) {
    val towar = state.selected ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(towar.nazwa.ifBlank { towar.itemId }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Cena bazowa: ${formatPln(towar.cenaBazowa)}", style = MaterialTheme.typography.bodyMedium)

        // Stock per warehouse
        SectionLabel("Stan magazynowy")
        when {
            state.magazynyLoading -> CircularProgressIndicator(Modifier.padding(8.dp))
            state.magazyny.isEmpty() -> Text("Brak danych magazynowych.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> state.magazyny.forEach { WarehouseRow(it) }
        }

        // Quantity
        SectionLabel("Ilość")
        QtyRow(tons = state.qty, onChange = onQtyChange)

        // Pricing calculator (backend = source of truth)
        SectionLabel("Cena i rabat")
        if (state.paymTermId == null) {
            Text(
                "Ustaw termin płatności w koszyku, aby policzyć cenę z kredytem kupieckim.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PricingCalculator(
            pricing = state.pricing,
            loading = state.pricingLoading,
            enabled = state.paymTermId != null,
            onRabatChange = onRabatChange,
            onTargetPriceChange = onTargetPriceChange,
        )

        // Logistics
        SectionLabel("Kalkulator logistyczny")
        AddressDropdown(
            selected = state.selectedAddress,
            options = state.addresses,
            onSelect = onSelectAddress,
        )
        OutlinedButton(onClick = onCalcLogistics, enabled = !state.wariantyLoading) {
            Text(if (state.wariantyLoading) "Liczę warianty…" else "Oblicz transport")
        }
        state.warianty.forEach { wariant ->
            WariantRow(
                wariant = wariant,
                selected = state.selectedWariant?.loadLocationId == wariant.loadLocationId,
                onClick = { onSelectWariant(wariant) },
            )
        }

        HorizontalDivider()
        Button(
            onClick = onAdd,
            enabled = state.canAdd,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.adding) "Dodaję…" else "Dodaj do koszyka")
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun WarehouseRow(stan: MagazynStan) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(stan.magazynNazwa ?: stan.magazynId, style = MaterialTheme.typography.bodyMedium)
            stan.numerPartii?.takeIf { it.isNotBlank() }?.let {
                Text("Partia: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatTons(stan.dostepne), style = MaterialTheme.typography.bodyMedium)
            if (stan.przeterminowany) {
                Text("Przeterminowany", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626))
            }
        }
    }
}

@Composable
private fun QtyRow(tons: Double, onChange: (Double) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange((tons - 1).coerceAtLeast(1.0)) }) {
            Icon(Icons.Filled.Remove, contentDescription = "Mniej")
        }
        Text(formatTons(tons), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = { onChange(tons + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = "Więcej")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PricingCalculator(
    pricing: PricingResult?,
    loading: Boolean,
    enabled: Boolean,
    onRabatChange: (Double) -> Unit,
    onTargetPriceChange: (Double) -> Unit,
) {
    var rabatText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = rabatText,
                onValueChange = {
                    rabatText = it
                    it.replace(',', '.').toDoubleOrNull()?.let(onRabatChange)
                },
                enabled = enabled,
                singleLine = true,
                label = { Text("Rabat (zł)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = priceText,
                onValueChange = {
                    priceText = it
                    it.replace(',', '.').toDoubleOrNull()?.let(onTargetPriceChange)
                },
                enabled = enabled,
                singleLine = true,
                label = { Text("Cena sprzedaży (zł)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        if (loading) {
            CircularProgressIndicator(Modifier.padding(4.dp))
        }
        pricing?.let { p ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    PriceLine("Cena bazowa", formatPln(p.cenaBazowa))
                    PriceLine("Kredyt kupiecki", formatPln(p.kredytKupiecki))
                    PriceLine("Rabat", "${p.rabatProcentowy}%")
                    PriceLine("Cena sprzedaży", formatPln(p.cenaSprzedazy), bold = true)
                    if (p.maxRabatPrzekroczony) {
                        Text(
                            "Przekroczono maksymalny rabat — wymagana akceptacja kierownika.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD97706),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceLine(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressDropdown(
    selected: AdresDostawy?,
    options: List<AdresDostawy>,
    onSelect: (AdresDostawy) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.label?.ifBlank { selected.adres ?: "" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Adres dostawy") },
            placeholder = { Text("Wybierz adres") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label.ifBlank { option.adres ?: "—" }) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun WariantRow(wariant: WariantLogistyczny, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "${wariant.loadLocationNazwa} → ${wariant.deliveryLabel}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatKm(wariant.km), style = MaterialTheme.typography.bodySmall)
                // PLN/t is read-only — it comes from the Transport cost engine.
                Text("${formatPlnPerTon(wariant.stawkaPlnT)} (stała)", style = MaterialTheme.typography.bodySmall)
                Text(formatPln(wariant.kosztTotal), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
