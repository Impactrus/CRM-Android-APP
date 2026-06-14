package com.ossadkowski.crm.mobile.ui.nawozy.screens.kontrahent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.nawozy.model.Kontrahent
import com.ossadkowski.crm.mobile.ui.nawozy.common.LimitBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KontrahentPickerScreen(
    onBack: () -> Unit,
    onKoszykStarted: (Long) -> Unit,
    viewModel: KontrahentPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.startedKoszykId) {
        state.startedKoszykId?.let { id ->
            viewModel.consumeNavigation()
            onKoszykStarted(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wybór kontrahenta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
            )
        },
        bottomBar = {
            state.selected?.let { selected ->
                SelectedCustomerBar(
                    selected = selected,
                    state = state,
                    onStart = viewModel::startOrder,
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Szukaj kontrahenta…") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            )
            FilterChip(
                selected = state.myOnly,
                onClick = viewModel::toggleMyOnly,
                label = { Text("Mój klient") },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.loading && state.customers.isEmpty() ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.error != null && state.customers.isEmpty() ->
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        )
                    state.customers.isEmpty() ->
                        Text(
                            text = "Brak wyników.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.customers, key = { it.accountNum }) { customer ->
                            CustomerRow(
                                customer = customer,
                                selected = state.selected?.accountNum == customer.accountNum,
                                onClick = { viewModel.select(customer) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerRow(customer: Kontrahent, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = customer.nazwa.ifBlank { customer.accountNum },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (customer.isMyClient) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Text(
                            text = "Mój klient",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            customer.adres?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SelectedCustomerBar(
    selected: Kontrahent,
    state: KontrahentPickerState,
    onStart: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Column(modifier = Modifier.navigationBarsPadding().padding(12.dp)) {
            when {
                state.limitLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.padding(end = 8.dp))
                    Text("Sprawdzam limit…", style = MaterialTheme.typography.bodyMedium)
                }
                state.limitStatus != null -> LimitBanner(state.limitStatus)
            }
            Button(
                onClick = onStart,
                enabled = !state.starting,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(if (state.starting) "Tworzę koszyk…" else "Rozpocznij zamówienie — ${selected.nazwa}")
            }
        }
    }
}
