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

@Composable
fun DelegacjeTeamScreen(viewModel: DelegacjeTeamViewModel) {
    LaunchedEffect(Unit) { viewModel.loadDelegacje() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Zespół — delegacje", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Delegacje członków Twojego zespołu.",
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
                Text(
                    "Brak delegacji w zespole.",
                    modifier = Modifier.padding(20.dp),
                    color = Color(0xFF6B7280)
                )
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.delegacje, key = { it.id }) { item ->
                    TeamDelegacjaCard(item)
                }
            }
        }
    }
}

@Composable
private fun TeamDelegacjaCard(item: com.ossadkowski.crm.mobile.data.model.DelegacjaListItem) {
    val statusStyle = DelegacjaStatusColors.forStatus(item.status)

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
                Text(
                    item.employeeName ?: "Pracownik",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF111827)
                )
                if (item.flagaManagerUwaga == true) {
                    Text("⚠️", fontSize = 16.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.nrDokumentu ?: "#${item.id}",
                    fontSize = 14.sp,
                    color = Color(0xFF1e40af)
                )
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
                Text(
                    item.celMiejscowosc!!,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!item.startAt.isNullOrBlank() || !item.endAt.isNullOrBlank()) {
                Text(
                    "${item.startAt ?: "?"} — ${item.endAt ?: "?"}",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
