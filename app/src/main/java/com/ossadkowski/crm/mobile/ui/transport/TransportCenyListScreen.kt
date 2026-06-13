package com.ossadkowski.crm.mobile.ui.transport

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ossadkowski.crm.mobile.data.model.TransportPriceListItem

object TransportStatusColors {
    data class Style(val bg: Color, val text: Color)
    fun forStatus(status: String?): Style {
        return when (status?.uppercase()) {
            "PENDING", "OCZEKUJĄCY", "OCZEKUJACY" -> Style(Color(0xFFFEF3C7), Color(0xFFD97706))
            "APPROVED", "ZATWIERDZONY" -> Style(Color(0xFFD1FAE5), Color(0xFF059669))
            "REJECTED", "ODRZUCONY" -> Style(Color(0xFFFEE2E2), Color(0xFFDC2626))
            "COMPLETED", "ZAKOŃCZONY", "ZAKONCZONY" -> Style(Color(0xFFF3F4F6), Color(0xFF4B5563))
            else -> Style(Color(0xFFEFF6FF), Color(0xFF2563EB))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportCenyListScreen(
    viewModel: TransportCenyListViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadTransportList()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, TransportCenyNewActivity::class.java))
                },
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nowy wniosek")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
        ) {
            // Zakładki
            TabRow(
                selectedTabIndex = if (viewModel.currentTab == "all") 0 else 1,
                containerColor = Color.White,
                contentColor = Color(0xFF0284C7)
            ) {
                Tab(
                    selected = viewModel.currentTab == "all",
                    onClick = { viewModel.setTab("all") },
                    text = { Text("Wszystkie", fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = viewModel.currentTab == "mine",
                    onClick = { viewModel.setTab("mine") },
                    text = { Text("Moje", fontWeight = FontWeight.Medium) }
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Wyszukiwarka
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = {
                        viewModel.searchQuery = it
                        viewModel.setSearch(it)
                    },
                    placeholder = { Text("Szukaj kontrahenta, towaru...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filtry statusów
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val statuses = listOf("Wszystkie", "PENDING", "APPROVED", "REJECTED", "COMPLETED")
                    statuses.forEach { status ->
                        val isSelected = (status == "Wszystkie" && viewModel.searchStatus == null) || (status == viewModel.searchStatus)
                        val text = when (status) {
                            "PENDING" -> "Oczekujące"
                            "APPROVED" -> "Zatwierdzone"
                            "REJECTED" -> "Odrzucone"
                            "COMPLETED" -> "Zakończone"
                            else -> "Wszystkie"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setStatus(if (status == "Wszystkie") null else status)
                            },
                            label = { Text(text, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista wniosków
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        viewModel.isLoading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF0284C7))
                            }
                        }
                        viewModel.error != null -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(viewModel.error ?: "Wystąpił błąd", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        viewModel.items.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Brak wniosków o transport.", color = Color.Gray)
                            }
                        }
                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(viewModel.items, key = { it.id }) { item ->
                                    TransportListItemCard(item = item) {
                                        val intent = Intent(context, TransportCenyDetailActivity::class.java).apply {
                                            putExtra("TRANSPORT_ID", item.id)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        }
                    }
                }

                // Paginacja
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.prevPage() },
                        enabled = viewModel.currentPage > 1
                    ) {
                        Text("Poprzednia", color = if (viewModel.currentPage > 1) Color(0xFF0284C7) else Color.Gray)
                    }
                    Text("Strona ${viewModel.currentPage} z ${viewModel.totalPages}", fontSize = 14.sp)
                    TextButton(
                        onClick = { viewModel.nextPage() },
                        enabled = viewModel.currentPage < viewModel.totalPages
                    ) {
                        Text("Następna", color = if (viewModel.currentPage < viewModel.totalPages) Color(0xFF0284C7) else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun TransportListItemCard(
    item: TransportPriceListItem,
    onClick: () -> Unit
) {
    val statusStyle = TransportStatusColors.forStatus(item.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.kontrahentNazwa,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusStyle.bg,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = when (item.status.uppercase()) {
                            "PENDING" -> "Oczekujący"
                            "APPROVED" -> "Zatwierdzony"
                            "REJECTED" -> "Odrzucony"
                            "COMPLETED" -> "Zakończony"
                            else -> item.status
                        },
                        color = statusStyle.text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${item.towar ?: "Zboże"} • ${item.iloscTon ?: 0.0} t",
                color = Color(0xFF4B5563),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.adresZaladunku ?: "—"} → ${item.adresOdbioru ?: "—"}",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = Color(0xFFF3F4F6))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Szacowany koszt: ${item.szacowanyKosztTransportu} PLN",
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563)
                )
                if (item.zatwierdzonyKoszt != null) {
                    Text(
                        text = "Zatwierdzony: ${item.zatwierdzonyKoszt} PLN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }
            }
        }
    }
}
