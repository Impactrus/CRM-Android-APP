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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
 * up already confirmed. Click on a confirmed visit allows adding/editing a meeting note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitsListScreen(
    onBack: () -> Unit,
    viewModel: VisitsListViewModel = hiltViewModel(),
) {
    val visits by viewModel.visits.collectAsStateWithLifecycle()
    val currentLoc by viewModel.currentLocation.collectAsStateWithLifecycle()
    var editingVisit by remember { mutableStateOf<VisitEvent?>(null) }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(editingVisit) {
        noteText = editingVisit?.note ?: ""
    }

    // Refresh current location when the screen opens or visits change
    LaunchedEffect(visits) {
        viewModel.refreshLocation()
    }

    if (editingVisit != null) {
        AlertDialog(
            onDismissRequest = { editingVisit = null },
            title = { Text("Notatka ze spotkania") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Opisz przebieg spotkania...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editingVisit?.let { viewModel.updateNote(it.id, noteText) }
                        editingVisit = null
                    }
                ) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingVisit = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

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
                        val isWithinRange = viewModel.isWithinRange(visit)
                        VisitRow(
                            visit = visit,
                            isWithinRange = isWithinRange,
                            onConfirm = { viewModel.confirm(visit.id) },
                            onReject = { viewModel.reject(visit.id) },
                            onClick = { editingVisit = visit },
                            onDelete = { viewModel.delete(visit.id) },
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
    isWithinRange: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = visit.contractorName ?: "Wizyta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Usuń",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

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

            if (!visit.note.isNullOrBlank()) {
                Text(
                    text = "Notatka: ${visit.note}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (visit.status == VisitStatus.CONFIRMED) {
                val hasNote = !visit.note.isNullOrBlank()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(onClick = onClick) {
                        Text(if (hasNote) "Edytuj notatkę" else "Dodaj notatkę")
                    }
                }
            }

            if (visit.status == VisitStatus.DETECTED) {
                if (!isWithinRange) {
                    Text(
                        text = "Musisz być na miejscu (w promieniu 1km), aby zatwierdzić tę wizytę.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = onConfirm,
                        enabled = isWithinRange,
                    ) { Text("Potwierdź") }
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
