package com.ossadkowski.crm.mobile.ui.transport

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ossadkowski.crm.mobile.ui.sales.AxContractSearchActivity
import com.ossadkowski.crm.mobile.KontrahentSearchActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportCenyNewScreen(
    viewModel: TransportCenyNewViewModel,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            Toast.makeText(context, "Wniosek wysłany do logistyki", Toast.LENGTH_LONG).show()
            onSuccess()
        }
    }

    LaunchedEffect(viewModel.error) {
        viewModel.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // Launchers
    val axSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            viewModel.kontraktAx = data?.getStringExtra("AX_CONTRACT_ID")
            val vendorName = data?.getStringExtra("AX_VENDOR_NAME")
            if (!vendorName.isNullOrEmpty()) {
                viewModel.kontrahentNazwa = vendorName
                viewModel.kontrahentId = "AX_IMPORT"
            }
            val itemName = data?.getStringExtra("AX_ITEM_NAME")
            if (!itemName.isNullOrEmpty()) {
                viewModel.towar = itemName
            }
            val quantity = data?.getDoubleExtra("AX_QUANTITY", 0.0) ?: 0.0
            if (quantity > 0) {
                viewModel.ilosc = quantity.toString()
            }
        }
    }

    val kontrahentSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            viewModel.kontrahentId = data?.getStringExtra("KONTRAHENT_ID") ?: ""
            viewModel.kontrahentNazwa = data?.getStringExtra("KONTRAHENT_NAZWA") ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dane Zlecenia Transportowego",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Kontrakt AX
                OutlinedTextField(
                    value = viewModel.kontraktAx ?: "",
                    onValueChange = {},
                    label = { Text("Kontrakt AX (Opcjonalny)") },
                    placeholder = { Text("Kliknij aby wybrać kontrakt...") },
                    readOnly = true,
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            axSearchLauncher.launch(Intent(context, AxContractSearchActivity::class.java))
                        },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Kontrahent (Wymagany)
                OutlinedTextField(
                    value = viewModel.kontrahentNazwa,
                    onValueChange = {},
                    label = { Text("Kontrahent *") },
                    placeholder = { Text("Kliknij aby wybrać kontrahenta...") },
                    readOnly = true,
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            kontrahentSearchLauncher.launch(Intent(context, KontrahentSearchActivity::class.java))
                        },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Towar (Wymagany)
                OutlinedTextField(
                    value = viewModel.towar,
                    onValueChange = { viewModel.towar = it },
                    label = { Text("Towar *") },
                    placeholder = { Text("Wpisz rodzaj towaru (np. Pszenica)...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ilość ton (Wymagany)
                OutlinedTextField(
                    value = viewModel.ilosc,
                    onValueChange = { viewModel.ilosc = it },
                    label = { Text("Ilość ton *") },
                    placeholder = { Text("Wpisz ilość...") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Skład
                var expandedSklad by remember { mutableStateOf(false) }
                val sklady = listOf("Główny", "Ryki", "Warka", "Inny")
                ExposedDropdownMenuBox(
                    expanded = expandedSklad,
                    onExpandedChange = { expandedSklad = !expandedSklad }
                ) {
                    OutlinedTextField(
                        value = sklady[viewModel.skladId - 1],
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Skład") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSklad) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSklad,
                        onDismissRequest = { expandedSklad = false }
                    ) {
                        sklady.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    viewModel.skladId = index + 1
                                    expandedSklad = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Adres załadunku
                OutlinedTextField(
                    value = viewModel.adresZaladunku,
                    onValueChange = { viewModel.adresZaladunku = it },
                    label = { Text("Adres załadunku") },
                    placeholder = { Text("Wpisz adres...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Odbiorca
                OutlinedTextField(
                    value = viewModel.odbiorca,
                    onValueChange = { viewModel.odbiorca = it },
                    label = { Text("Odbiorca") },
                    placeholder = { Text("Wpisz odbiorcę...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Adres odbioru
                OutlinedTextField(
                    value = viewModel.adresOdbioru,
                    onValueChange = { viewModel.adresOdbioru = it },
                    label = { Text("Adres odbioru") },
                    placeholder = { Text("Wpisz adres odbiorcy...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Szacowany koszt (Wymagany)
                OutlinedTextField(
                    value = viewModel.szacowanyKoszt,
                    onValueChange = { viewModel.szacowanyKoszt = it },
                    label = { Text("Szacowany koszt transportu (PLN) *") },
                    placeholder = { Text("Wpisz kwotę...") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Komentarz
                OutlinedTextField(
                    value = viewModel.komentarz,
                    onValueChange = { viewModel.komentarz = it },
                    label = { Text("Komentarz") },
                    placeholder = { Text("Wpisz uwagi...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Przyciski
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Anuluj", color = Color(0xFF4B5563))
                    }
                    Button(
                        onClick = { viewModel.submitRequest() },
                        modifier = Modifier.weight(1.5f),
                        enabled = !viewModel.isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        if (viewModel.isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Wyślij do logistyki")
                        }
                    }
                }
            }
        }
    }
}
