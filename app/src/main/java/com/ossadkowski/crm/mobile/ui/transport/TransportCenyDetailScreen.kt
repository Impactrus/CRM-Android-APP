package com.ossadkowski.crm.mobile.ui.transport

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.data.model.TransportPriceHistoryItem
import com.ossadkowski.crm.mobile.data.model.TransportPriceListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportCenyDetailScreen(
    viewModel: TransportCenyDetailViewModel,
    requestId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isLogistics = remember {
        sessionManager.role.lowercase().let { it.contains("logist") || it.contains("admin") }
    }

    LaunchedEffect(requestId) {
        viewModel.loadDetail(requestId)
    }

    LaunchedEffect(viewModel.reviewSuccess) {
        if (viewModel.reviewSuccess) {
            Toast.makeText(context, "Decyzja została zapisana", Toast.LENGTH_SHORT).show()
            viewModel.resetReviewState()
        }
    }

    LaunchedEffect(viewModel.reviewError) {
        viewModel.reviewError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetReviewState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły wniosku #${requestId}", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1F2937)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
        ) {
            when {
                viewModel.isLoading && viewModel.requestDetails == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF0284C7))
                    }
                }
                viewModel.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(viewModel.error ?: "Wystąpił błąd", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadDetail(requestId) }) {
                                Text("Spróbuj ponownie")
                            }
                        }
                    }
                }
                viewModel.requestDetails != null -> {
                    val item = viewModel.requestDetails!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Status Card
                        StatusCard(item)

                        // Main Details Card
                        DetailsCard(item)

                        // Action Form for Logistics (if Pending)
                        if (isLogistics && item.status.uppercase() in listOf("PENDING", "OCZEKUJĄCY", "OCZEKUJACY")) {
                            LogisticsActionCard(
                                isSubmitting = viewModel.isSubmittingReview,
                                defaultCost = item.szacowanyKosztTransportu,
                                onSubmit = { approved, approvedCost, comment ->
                                    viewModel.submitReview(item.id, approved, approvedCost, comment)
                                }
                            )
                        }

                        // History Timeline Card
                        if (viewModel.history.isNotEmpty()) {
                            HistoryCard(viewModel.history)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(item: TransportPriceListItem) {
    val statusStyle = TransportStatusColors.forStatus(item.status)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Status wniosku", fontSize = 12.sp, color = Color(0xFF6B7280))
                Text(
                    text = when (item.status.uppercase()) {
                        "PENDING" -> "Oczekujący na wycenę"
                        "APPROVED" -> "Zatwierdzony"
                        "REJECTED" -> "Odrzucony"
                        "COMPLETED" -> "Zakończony"
                        else -> item.status
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusStyle.bg
            ) {
                Text(
                    text = item.status.uppercase(),
                    color = statusStyle.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun DetailsCard(item: TransportPriceListItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Dane Zlecenia", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
            Divider(color = Color(0xFFF3F4F6))

            DetailRow("Kontrahent", item.kontrahentNazwa)
            if (!item.axVendContractId.isNullOrEmpty()) {
                DetailRow("Kontrakt AX (Zakup)", item.axVendContractId)
            }
            if (!item.axCustContractId.isNullOrEmpty()) {
                DetailRow("Kontrakt AX (Sprzedaż)", item.axCustContractId)
            }
            DetailRow("Towar", item.towar ?: "—")
            DetailRow("Ilość ton", "${item.iloscTon ?: 0.0} t")
            DetailRow("Skład", item.sklad ?: "—")
            DetailRow("Adres załadunku", item.adresZaladunku ?: "—")
            DetailRow("Odbiorca", item.odbiorca ?: "—")
            DetailRow("Adres odbioru", item.adresOdbioru ?: "—")
            DetailRow("Szacowany koszt", "${item.szacowanyKosztTransportu} PLN")
            
            if (item.zatwierdzonyKoszt != null) {
                DetailRow("Zatwierdzony koszt", "${item.zatwierdzonyKoszt} PLN", isHighlight = true)
            }

            if (!item.komentarzHandlowiec.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text("Komentarz handlowca:", fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(item.komentarzHandlowiec, fontSize = 13.sp, color = Color(0xFF374151))
                }
            }

            if (!item.komentarzLogistyka.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text("Komentarz logistyki:", fontSize = 11.sp, color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(item.komentarzLogistyka, fontSize = 13.sp, color = Color(0xFF1E40AF))
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF6B7280))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) Color(0xFF059669) else Color(0xFF1F2937)
        )
    }
}

@Composable
fun LogisticsActionCard(
    isSubmitting: Boolean,
    defaultCost: Double,
    onSubmit: (Boolean, Double?, String) -> Unit
) {
    var isApprove by remember { mutableStateOf(true) }
    var inputCost by remember { mutableStateOf(defaultCost.toString()) }
    var inputComment by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Decyzja Logistyki", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
            Divider(color = Color(0xFFF3F4F6))

            // Action Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { isApprove = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApprove) Color(0xFF059669) else Color(0xFFE5E7EB),
                        contentColor = if (isApprove) Color.White else Color(0xFF4B5563)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Zatwierdź")
                }
                Button(
                    onClick = { isApprove = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isApprove) Color(0xFFDC2626) else Color(0xFFE5E7EB),
                        contentColor = if (!isApprove) Color.White else Color(0xFF4B5563)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Odrzuć")
                }
            }

            AnimatedVisibility(visible = isApprove) {
                OutlinedTextField(
                    value = inputCost,
                    onValueChange = { inputCost = it },
                    label = { Text("Zatwierdzony koszt transportu (PLN) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            OutlinedTextField(
                value = inputComment,
                onValueChange = { inputComment = it },
                label = { Text("Komentarz / Uzasadnienie") },
                placeholder = { Text("Wpisz uwagi...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(8.dp)
            )

            Button(
                onClick = {
                    val cost = if (isApprove) inputCost.toDoubleOrNull() else null
                    onSubmit(isApprove, cost, inputComment)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isApprove) "Zatwierdź wniosek" else "Odrzuć wniosek")
                }
            }
        }
    }
}

@Composable
fun HistoryCard(historyList: List<TransportPriceHistoryItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Historia zmian", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
            Divider(color = Color(0xFFF3F4F6))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                historyList.forEachIndexed { index, item ->
                    TimelineItem(
                        item = item,
                        isLast = index == historyList.size - 1
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(item: TransportPriceHistoryItem, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(Color(0xFFE5E7EB))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.akcja,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = item.createdAt?.take(16)?.replace("T", " ") ?: "",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Text(
                text = "Użytkownik: ${item.username ?: "System"}",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 2.dp)
            )

            if (!item.komentarz.isNullOrEmpty()) {
                Text(
                    text = item.komentarz,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}
