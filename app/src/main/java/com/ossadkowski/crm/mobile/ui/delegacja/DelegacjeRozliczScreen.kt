package com.ossadkowski.crm.mobile.ui.delegacja

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelegacjeRozliczScreen(viewModel: DelegacjeRozliczViewModel) {
    LaunchedEffect(Unit) { viewModel.loadItems() }

    val snackbarHostState = remember { SnackbarHostState() }
    val actionResult = viewModel.actionResult
    LaunchedEffect(actionResult) {
        actionResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionResult()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Rozliczenia delegacji — Finanse", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "Zatwierdź, odrzuć lub wyślij pytanie do pracownika.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Status filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.statusOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = viewModel.selectedStatus == value,
                        onClick = { viewModel.setStatus(value) },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            when {
                viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                viewModel.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.error ?: "Błąd", color = MaterialTheme.colorScheme.error)
                }
                viewModel.items.isEmpty() -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                ) {
                    Text(
                        "Brak delegacji w wybranym statusie.",
                        modifier = Modifier.padding(20.dp),
                        color = Color(0xFF6B7280)
                    )
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.items, key = { it.id }) { item ->
                        RozliczCard(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun RozliczCard(item: com.ossadkowski.crm.mobile.data.model.DelegacjaFinanseItem) {
    val statusStyle = DelegacjaStatusColors.forStatus(item.status)
    val fmt = remember { NumberFormat.getCurrencyInstance(Locale("pl", "PL")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        item.nrDokumentu ?: "#${item.id}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF1e40af)
                    )
                    if (!item.employeeName.isNullOrBlank()) {
                        Text(item.employeeName!!, fontSize = 13.sp, color = Color(0xFF374151))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusStyle.bg
                ) {
                    Text(
                        item.status ?: "—",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusStyle.text
                    )
                }
            }

            if (!item.celMiejscowosc.isNullOrBlank()) {
                Text(item.celMiejscowosc!!, fontSize = 13.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(top = 4.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${item.startAt ?: "?"} — ${item.endAt ?: "?"}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
                item.ogolem?.let {
                    Text(
                        fmt.format(it),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF065f46)
                    )
                }
            }
        }
    }
}
