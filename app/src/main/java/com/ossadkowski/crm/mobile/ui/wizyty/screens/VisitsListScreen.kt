package com.ossadkowski.crm.mobile.ui.wizyty.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitStatus
import com.ossadkowski.crm.mobile.ui.wizyty.common.NonLiveDataBanner
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())

/**
 * Visits list: auto-detected rows (DETECTED) show Confirm/Reject; manual rows show
 * up already confirmed. Rejected rows are filtered out by the repository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitsListScreen(
    onBack: () -> Unit,
    viewModel: VisitsListViewModel = hiltViewModel(),
) {
    val visits by viewModel.visits.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wizyty") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NonLiveDataBanner()

            if (visits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Brak wizyt.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(visits, key = { it.id }) { visit ->
                        VisitRow(
                            visit = visit,
                            onConfirm = { viewModel.confirm(visit.id) },
                            onReject = { viewModel.reject(visit.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitRow(
    visit: VisitEvent,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = visit.contractorName ?: "Wizyta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            visit.addressLabel?.let {
                Text("Adres: $it", style = MaterialTheme.typography.bodySmall)
            }
            if (visit.lat != null && visit.lng != null) {
                Text(
                    text = "Lokalizacja: ${"%.5f".format(visit.lat)}, ${"%.5f".format(visit.lng)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${DATE_FORMAT.format(visit.occurredAt)} • ${statusLabel(visit.status)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (visit.status == VisitStatus.DETECTED) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(onClick = onConfirm) { Text("Potwierdź") }
                    OutlinedButton(onClick = onReject) { Text("Odrzuć") }
                }
            }
        }
    }
}

private fun statusLabel(status: VisitStatus): String = when (status) {
    VisitStatus.DETECTED -> "wykryta — do potwierdzenia"
    VisitStatus.CONFIRMED -> "potwierdzona"
    VisitStatus.REJECTED -> "odrzucona"
}
