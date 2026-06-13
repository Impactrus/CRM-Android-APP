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
fun DelegacjeMojeScreen(viewModel: DelegacjeMojeViewModel) {
    LaunchedEffect(Unit) { viewModel.loadDelegacje() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Moje delegacje", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Historia Twoich delegacji służbowych.",
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
                    "Nie masz jeszcze żadnych delegacji.",
                    modifier = Modifier.padding(20.dp),
                    color = Color(0xFF6B7280)
                )
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.delegacje, key = { it.id }) { item ->
                    DelegacjaListCard(item)
                }
            }
        }
    }
}

@Composable
private fun DelegacjaListCard(item: com.ossadkowski.crm.mobile.data.model.DelegacjaListItem) {
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
                    item.nrDokumentu ?: "#${item.id}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
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

            if (!item.celDelegacji.isNullOrBlank() || !item.celMiejscowosc.isNullOrBlank()) {
                Text(
                    item.celDelegacji ?: item.celMiejscowosc ?: "",
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (!item.startAt.isNullOrBlank() || !item.endAt.isNullOrBlank()) {
                Text(
                    "${item.startAt ?: "?"} — ${item.endAt ?: "?"}",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!item.typWyjazdu.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        item.typWyjazdu!!,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        color = Color(0xFF3B82F6)
                    )
                }
            }
        }
    }
}
