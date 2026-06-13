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

@Composable
fun DelegacjeZaliczkiScreen(viewModel: DelegacjeZaliczkiViewModel) {
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
            Text("Zaliczki delegacyjne", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "Zarządzaj wypłatami zaliczek i rozliczeniami nadpłat.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            TabRow(
                selectedTabIndex = viewModel.selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White
            ) {
                viewModel.tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = viewModel.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                title,
                                fontWeight = if (viewModel.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                viewModel.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.error ?: "Błąd", color = MaterialTheme.colorScheme.error)
                }
                viewModel.items.isEmpty() -> {
                    val emptyMsg = when (viewModel.selectedTab) {
                        0 -> "Brak zaliczek oczekujących na wypłatę."
                        1 -> "Brak wypłaconych zaliczek."
                        else -> "Brak zaliczek do zwrotu."
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                    ) {
                        Text(emptyMsg, modifier = Modifier.padding(20.dp), color = Color(0xFF6B7280))
                    }
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.items, key = { it.id }) { item ->
                        ZaliczkaCard(
                            item = item,
                            showPayButton = viewModel.selectedTab == 0,
                            onPay = { viewModel.zaliczkaWyplacona(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZaliczkaCard(
    item: com.ossadkowski.crm.mobile.data.model.DelegacjaFinanseItem,
    showPayButton: Boolean,
    onPay: () -> Unit
) {
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

            item.zaliczkaKwota?.let {
                Text(
                    "Zaliczka: ${fmt.format(it)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF92400e),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (showPayButton) {
                Button(
                    onClick = onPay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Wypłać", color = Color.White)
                }
            }
        }
    }
}
