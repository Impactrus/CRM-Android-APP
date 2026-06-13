package com.ossadkowski.crm.mobile.ui.delegacja


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DelegacjeAuditScreen(viewModel: DelegacjeAuditViewModel) {
    LaunchedEffect(Unit) { viewModel.loadList() }

    if (viewModel.selectedId != null) {
        AuditDetailView(viewModel)
    } else {
        AuditListView(viewModel)
    }
}

@Composable
private fun AuditListView(viewModel: DelegacjeAuditViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Audit delegacji — HR", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Read-only historia zmian statusu, audit log i deklaracje BHP.",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            viewModel.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(viewModel.error ?: "Błąd", color = MaterialTheme.colorScheme.error)
            }
            viewModel.delegacje.isEmpty() -> Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
            ) {
                Text("Brak delegacji.", modifier = Modifier.padding(20.dp), color = Color(0xFF6B7280))
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.delegacje, key = { it.id }) { item ->
                    AuditListRow(item) { viewModel.selectDelegacja(item.id) }
                }
            }
        }
    }
}

@Composable
private fun AuditListRow(
    item: com.ossadkowski.crm.mobile.data.model.DelegacjaAuditItem,
    onClick: () -> Unit
) {
    val statusStyle = DelegacjaStatusColors.forStatus(item.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.nrDokumentu ?: "#${item.id}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF1e40af)
                )
                if (!item.employeeName.isNullOrBlank()) {
                    Text(item.employeeName!!, fontSize = 13.sp, color = Color(0xFF374151))
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusStyle.bg
                ) {
                    Text(
                        item.status ?: "—",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusStyle.text
                    )
                }

                // BHP
                Text(
                    if (item.deklaracjaBhpZlozona == true) "✓" else "—",
                    fontSize = 14.sp,
                    color = if (item.deklaracjaBhpZlozona == true) Color(0xFF065f46) else Color(0xFF9CA3AF)
                )

                // Warning flag
                if (item.flagaManagerUwaga == true) {
                    Text("⚠️", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun AuditDetailView(viewModel: DelegacjeAuditViewModel) {
    val detail = viewModel.auditDetail

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.clearSelection() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
            }
            Text(
                "Szczegóły audit — ${detail?.nrDokumentu ?: "#${viewModel.selectedId}"}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            viewModel.detailLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            viewModel.detailError != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(viewModel.detailError ?: "Błąd", color = MaterialTheme.colorScheme.error)
            }
            detail != null -> LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(detail.employeeName ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            val statusStyle = DelegacjaStatusColors.forStatus(detail.status)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = statusStyle.bg,
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    detail.status ?: "—",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = statusStyle.text
                                )
                            }
                        }
                    }
                }

                // Status history
                item {
                    Text("Historia statusu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                val statusHistory = detail.statusHistory ?: emptyList()
                if (statusHistory.isEmpty()) {
                    item { Text("Brak wpisów.", color = Color(0xFF9CA3AF), fontSize = 13.sp) }
                } else {
                    items(statusHistory) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(entry.createdAt ?: "", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                Row {
                                    Text("${entry.statusFrom ?: "(start)"} → ", fontSize = 13.sp)
                                    Text(entry.statusTo ?: "", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text(
                                    "przez ${entry.changedByUsername ?: "system"}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                                if (!entry.reason.isNullOrBlank()) {
                                    Text(
                                        "\"${entry.reason}\"",
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400e),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Audit log
                item {
                    Text("Audit log", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                }
                val auditLog = detail.auditLog ?: emptyList()
                if (auditLog.isEmpty()) {
                    item { Text("Brak wpisów.", color = Color(0xFF9CA3AF), fontSize = 13.sp) }
                } else {
                    items(auditLog) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(entry.createdAt ?: "", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                Text(entry.action ?: "", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(
                                    entry.actorUsername ?: "system",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                                if (!entry.ip.isNullOrBlank()) {
                                    Text("IP: ${entry.ip}", fontSize = 11.sp, color = Color(0xFFD1D5DB))
                                }
                            }
                        }
                    }
                }

                // Deklaracje BHP
                item {
                    Text("Deklaracje BHP", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                }
                val deklaracje = detail.deklaracje ?: emptyList()
                if (deklaracje.isEmpty()) {
                    item { Text("Brak deklaracji.", color = Color(0xFF9CA3AF), fontSize = 13.sp) }
                } else {
                    items(deklaracje) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFd1fae5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Text(entry.createdAt ?: "", fontSize = 13.sp, color = Color(0xFF065f46))
                                if (!entry.ip.isNullOrBlank()) {
                                    Text(" • IP: ${entry.ip}", fontSize = 11.sp, color = Color(0xFF6B7280))
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}
