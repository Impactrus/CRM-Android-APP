package com.ossadkowski.crm.mobile.ui.wizyty.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.ui.wizyty.common.NonLiveDataBanner

/**
 * Manual add-visit form. The address field reuses the server-proxied TomTom search
 * (`ApiService.searchGeocode`) as autocomplete; picking a suggestion attaches its
 * coordinates to the visit. Saved as a MANUAL/CONFIRMED visit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisitScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddVisitViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj wizytę") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NonLiveDataBanner()

            OutlinedTextField(
                value = state.contractorName,
                onValueChange = viewModel::onContractorNameChange,
                label = { Text("Kontrahent") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )

            OutlinedTextField(
                value = state.addressQuery,
                onValueChange = viewModel::onAddressQueryChange,
                label = { Text("Adres (opcjonalnie)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )

            if (state.suggestions.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column {
                        state.suggestions.forEachIndexed { index, suggestion ->
                            if (index > 0) HorizontalDivider()
                            Text(
                                text = suggestion.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onSuggestionSelected(suggestion) }
                                    .padding(12.dp),
                            )
                        }
                    }
                }
            }

            state.selected?.let {
                Text(
                    text = "Lokalizacja przypisana: ${"%.5f".format(it.lat)}, ${"%.5f".format(it.lng)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp).padding(end = 4.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text("Zapisz wizytę")
            }
        }
    }
}
