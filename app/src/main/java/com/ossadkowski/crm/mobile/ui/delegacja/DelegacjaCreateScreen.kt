package com.ossadkowski.crm.mobile.ui.delegacja

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import androidx.compose.ui.graphics.asImageBitmap
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelegacjaCreateScreen(
    viewModel: DelegacjaCreateViewModel,
    currentUserId: Int
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val routes by viewModel.routes.collectAsState()
    val attachments by viewModel.attachments.collectAsState()

    // Activity Result Launcher for attachments
    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addAttachment(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CrmTheme.colors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Wizard Header
            WizardStepIndicator(currentStep = viewModel.currentStep)
            Spacer(modifier = Modifier.height(20.dp))

            // Main body based on step
            when (viewModel.currentStep) {
                1 -> Step1TypeSelection(
                    viewModel = viewModel,
                    userId = currentUserId
                )
                2 -> Step2FormAndRoutes(
                    viewModel = viewModel,
                    routes = routes,
                    countries = viewModel.countriesList
                )
                3 -> Step3CostsSummary(
                    viewModel = viewModel,
                    attachments = attachments,
                    onAddAttachmentClick = { attachmentLauncher.launch("image/*") },
                    onDeleteAttachment = { viewModel.removeAttachment(it) },
                    onSubmitClick = {
                        viewModel.submitDelegacja(currentUserId, context.contentResolver)
                    }
                )
            }

            Spacer(modifier = Modifier.height(100.dp)) // padding for bottom buttons
        }

        // Navigation Footer (except when successfully submitted on Step 3)
        val showFooter = !(viewModel.currentStep == 3 && viewModel.submitResult is NetworkResult.Success)

        if (showFooter) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = CrmTheme.colors.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (viewModel.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.prevStep() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmTheme.colors.dark1),
                            border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Wstecz", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    val canGoNext = when (viewModel.currentStep) {
                        1 -> viewModel.selectedType != null && viewModel.selectedWniosekId != null
                        2 -> viewModel.celDelegacji.isNotBlank() &&
                                viewModel.celMiejscowosc.isNotBlank() &&
                                viewModel.poczatekPodrozy.isNotBlank() &&
                                viewModel.startAt.isNotBlank() &&
                                viewModel.endAt.isNotBlank() &&
                                (viewModel.selectedType == "krajowa" || viewModel.selectedCountryCode != null) &&
                                (!routes.any { it.srodekLokomocji == "Samochód służbowy" } || viewModel.nrRejestracji.isNotBlank()) &&
                                viewModel.costCalculated != null
                        3 -> false // submit handles this
                        else -> false
                    }

                    if (viewModel.currentStep < 3) {
                        Button(
                            onClick = { viewModel.nextStep() },
                            enabled = canGoNext,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrmTheme.colors.primary,
                                contentColor = CrmTheme.colors.onPrimary,
                                disabledContainerColor = CrmTheme.colors.muted.copy(alpha = 0.2f),
                                disabledContentColor = CrmTheme.colors.muted
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text("Dalej", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WizardStepIndicator(currentStep: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface),
        border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kreator delegacji służbowej",
                style = CrmTheme.type.titleMd,
                color = CrmTheme.colors.ink
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                StepCircle(step = 1, label = "Typ", active = currentStep == 1, completed = currentStep > 1)
                StepConnector(completed = currentStep > 1)
                StepCircle(step = 2, label = "Trasa", active = currentStep == 2, completed = currentStep > 2)
                StepConnector(completed = currentStep > 2)
                StepCircle(step = 3, label = "Finanse", active = currentStep == 3, completed = currentStep > 3)
            }
        }
    }
}

@Composable
fun StepCircle(step: Int, label: String, active: Boolean, completed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        completed -> CrmTheme.colors.primary
                        active -> CrmTheme.colors.dark1
                        else -> CrmTheme.colors.surface100
                    }
                )
                .border(
                    1.dp,
                    if (active) CrmTheme.colors.primary else CrmTheme.colors.cardBorder,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Text("✓", color = CrmTheme.colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Text(
                    text = step.toString(),
                    color = if (active) Color.White else CrmTheme.colors.muted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = CrmTheme.type.caption,
            color = if (active || completed) CrmTheme.colors.ink else CrmTheme.colors.muted,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun StepConnector(completed: Boolean) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(2.dp)
            .padding(bottom = 12.dp)
            .background(if (completed) CrmTheme.colors.primary else CrmTheme.colors.cardBorder)
    )
}

@Composable
fun Step1TypeSelection(
    viewModel: DelegacjaCreateViewModel,
    userId: Int
) {
    val context = LocalContext.current

    // Auto-load wnioski on first display
    LaunchedEffect(userId) {
        if (viewModel.approvedWnioski.isEmpty() && !viewModel.isWnioskiLoading) {
            viewModel.loadApprovedWnioski(userId)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Wybierz typ wyjazdu", style = CrmTheme.type.headline, color = CrmTheme.colors.ink)

        TypeCard(
            title = "Krajowa (PL)",
            description = "Delegacje na terenie Rzeczypospolitej Polskiej. Stała stawka diety: 45 PLN.",
            selected = viewModel.selectedType == "krajowa",
            onClick = { viewModel.selectType("krajowa") }
        )

        TypeCard(
            title = "Krajowa + Zagraniczna (Mieszana)",
            description = "Wyjazdy łączone (np. wyjazd z bazy w PL, dojazd do granicy i jazda za granicą). Kalkulacja proporcjonalna.",
            selected = viewModel.selectedType == "mieszana",
            onClick = { viewModel.selectType("mieszana") }
        )

        TypeCard(
            title = "Zagraniczna",
            description = "Delegacje w całości odbywające się poza terytorium PL. Diety liczone według stawek kraju docelowego.",
            selected = viewModel.selectedType == "zagraniczna",
            onClick = { viewModel.selectType("zagraniczna") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = CrmTheme.colors.cardBorder)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zatwierdzone wnioski *",
                style = CrmTheme.type.titleMd,
                color = CrmTheme.colors.ink
            )
            TextButton(onClick = { viewModel.loadApprovedWnioski(userId) }) {
                Text("Odśwież", color = CrmTheme.colors.primaryDeep, fontWeight = FontWeight.Bold)
            }
        }

        if (viewModel.isWnioskiLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CrmTheme.colors.primary)
            }
        } else if (!viewModel.wnioskiError.isNullOrBlank()) {
            Text(
                text = "Błąd pobierania wniosków: ${viewModel.wnioskiError}",
                color = CrmTheme.colors.bad.text,
                style = CrmTheme.type.body
            )
        } else if (viewModel.approvedWnioski.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface2),
                border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Brak zatwierdzonych wniosków delegacyjnych.",
                        style = CrmTheme.type.body,
                        color = CrmTheme.colors.muted
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.approvedWnioski.forEach { wniosek ->
                    val isSelected = viewModel.selectedWniosekId == wniosek.id
                    WniosekRow(
                        wniosek = wniosek,
                        selected = isSelected,
                        onClick = {
                            viewModel.applyApprovedWniosek(wniosek)
                            Toast.makeText(context, "Zaciągnięto dane z wniosku #${wniosek.id}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TypeCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) CrmTheme.colors.brand.bg else CrmTheme.colors.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) CrmTheme.colors.primary else CrmTheme.colors.cardBorder
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = CrmTheme.colors.primary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = CrmTheme.type.titleMd,
                    color = if (selected) CrmTheme.colors.brand.text else CrmTheme.colors.ink,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = CrmTheme.type.body,
                    color = CrmTheme.colors.muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun WniosekRow(
    wniosek: WniosekItem,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) CrmTheme.colors.brand.bg else CrmTheme.colors.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) CrmTheme.colors.primary else CrmTheme.colors.cardBorder
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${wniosek.id}",
                        style = CrmTheme.type.caption,
                        color = CrmTheme.colors.primaryDeep,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = wniosek.odDo ?: "",
                        style = CrmTheme.type.body,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = wniosek.powod ?: "Brak opisu",
                    style = CrmTheme.type.body,
                    color = CrmTheme.colors.ink,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CrmTheme.colors.ok.bg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Zatwierdzony",
                    style = CrmTheme.type.caption,
                    color = CrmTheme.colors.ok.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2FormAndRoutes(
    viewModel: DelegacjaCreateViewModel,
    routes: List<DelegacjaRouteDto>,
    countries: List<DelegacjaKrajDto>
) {
    val context = LocalContext.current
    var countriesDropdownExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Uzgodnienia i trasa", style = CrmTheme.type.headline, color = CrmTheme.colors.ink)

        // General Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface),
            border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.celDelegacji,
                    onValueChange = { viewModel.celDelegacji = it },
                    label = { Text("Cel wyjazdu (opis) *") },
                    placeholder = { Text("np. Spotkanie z klientem Acme Sp. z o.o.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = viewModel.celMiejscowosc,
                    onValueChange = { viewModel.celMiejscowosc = it },
                    label = { Text("Miejscowość docelowa *") },
                    placeholder = { Text("np. Berlin") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = viewModel.celAdres,
                    onValueChange = { viewModel.celAdres = it },
                    label = { Text("Adres szczegółowy") },
                    placeholder = { Text("np. ul. Główna 15") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = viewModel.poczatekPodrozy,
                    onValueChange = { viewModel.poczatekPodrozy = it },
                    label = { Text("Początek podróży (Adres startowy) *") },
                    placeholder = { Text("np. Poznań, ul. Główna 5") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = viewModel.zaliczkaKwota,
                    onValueChange = { viewModel.zaliczkaKwota = it },
                    label = { Text("Zaliczka (PLN)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                val hasGovCar = routes.any { it.srodekLokomocji == "Samochód służbowy" }
                if (hasGovCar) {
                    OutlinedTextField(
                        value = viewModel.nrRejestracji,
                        onValueChange = { viewModel.nrRejestracji = it },
                        label = { Text("Numer rejestracyjny pojazdu służbowego *") },
                        placeholder = { Text("np. PO 12345") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Country selector for Foreign/Mixed trips
                if (viewModel.selectedType != "krajowa") {
                    val currentCountryName = countries.find { it.kodKraju == viewModel.selectedCountryCode }?.nazwaKraju ?: "-- Wybierz kraj --"
                    
                    ExposedDropdownMenuBox(
                        expanded = countriesDropdownExpanded,
                        onExpandedChange = { countriesDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = currentCountryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kraj docelowy *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countriesDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = countriesDropdownExpanded,
                            onDismissRequest = { countriesDropdownExpanded = false }
                        ) {
                            countries.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.nazwaKraju) },
                                    onClick = {
                                        viewModel.selectedCountryCode = c.kodKraju
                                        countriesDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Legs of the trip
        Text("Środki lokomocji", style = CrmTheme.type.titleMd, color = CrmTheme.colors.ink)
        Text("Każdy odcinek trasy może mieć inny środek lokomocji. Dodaj tyle ile potrzebujesz.", style = CrmTheme.type.caption, color = CrmTheme.colors.muted)

        var showRulesExpanded by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showRulesExpanded = !showRulesExpanded },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF1F8E9)
            ),
            border = BorderStroke(1.dp, Color(0xFFC5E1A5)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Zasady rozliczenia środków lokomocji — kliknij aby " + if (showRulesExpanded) "zwinąć" else "rozwinąć",
                    style = CrmTheme.type.body,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF33691E)
                )
                if (showRulesExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Samochód prywatny: rozliczany na podstawie kilometrówki. Stawka za 1 km zależy od pojemności silnika: do 900 cm³ (0,89 zł) oraz powyżej 900 cm³ (1,15 zł).\n" +
                               "• Inne środki transportu (Pociąg, Samolot, Autobus): rozliczane na podstawie biletów/faktur załączonych w kroku 3.\n" +
                               "• Samochód służbowy: brak kilometrówki, ewentualne koszty paliwa rozliczane fakturami w kroku 3.",
                        style = CrmTheme.type.body,
                        color = Color(0xFF33691E),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        routes.forEachIndexed { index, route ->
            LegCard(
                index = index,
                route = route,
                onUpdate = { updated -> viewModel.updateRoute(index, updated) },
                onDelete = { viewModel.removeRoute(index) },
                showDelete = routes.size > 1
            )
        }

        OutlinedButton(
            onClick = { viewModel.addRoute() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmTheme.colors.primaryDeep),
            border = BorderStroke(1.dp, CrmTheme.colors.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add leg")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dodaj odcinek", fontWeight = FontWeight.Bold)
        }

        // Section: Duration / Dates
        Text("Czas trwania delegacji", style = CrmTheme.type.titleMd, color = CrmTheme.colors.ink)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface),
            border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateTimePickerField(
                    label = "Początek delegacji *",
                    value = viewModel.startAt,
                    onValueChange = { viewModel.startAt = it }
                )

                DateTimePickerField(
                    label = "Koniec delegacji *",
                    value = viewModel.endAt,
                    onValueChange = { viewModel.endAt = it }
                )
            }
        }

        // Section: Cost parameters & Meals
        Text("Parametry kalkulatora", style = CrmTheme.type.titleMd, color = CrmTheme.colors.ink)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface),
            border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // If mixed trip, we need crossing time & split nights
                if (viewModel.selectedType == "mieszana") {
                    DateTimePickerField(
                        label = "Przekroczenie granicy (wjazd za granicę) *",
                        value = viewModel.czasPrzekroczenia,
                        onValueChange = { viewModel.czasPrzekroczenia = it }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        NumericCounter(
                            label = "Noclegi PL",
                            value = viewModel.liczbaNoclegowKrajowych,
                            onValueChange = { viewModel.liczbaNoclegowKrajowych = it },
                            modifier = Modifier.weight(1f)
                        )
                        NumericCounter(
                            label = "Noclegi zagr.",
                            value = viewModel.liczbaNoclegowZagranicznych,
                            onValueChange = { viewModel.liczbaNoclegowZagranicznych = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    NumericCounter(
                        label = "Liczba noclegów (ryczałt)",
                        value = viewModel.liczbaNoclegow,
                        onValueChange = { viewModel.liczbaNoclegow = it }
                    )
                }

                // Meal counters (For domestic calculation reductions)
                if (viewModel.selectedType == "krajowa") {
                    Text("Płatne posiłki zapewnione przez firmę (redukcja diet):", style = CrmTheme.type.caption, color = CrmTheme.colors.muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NumericCounter(
                            label = "Śniadania",
                            value = viewModel.sniadaniaCount,
                            onValueChange = { viewModel.sniadaniaCount = it },
                            modifier = Modifier.weight(1f)
                        )
                        NumericCounter(
                            label = "Obiady",
                            value = viewModel.obiadyCount,
                            onValueChange = { viewModel.obiadyCount = it },
                            modifier = Modifier.weight(1f)
                        )
                        NumericCounter(
                            label = "Kolacje",
                            value = viewModel.kolacjeCount,
                            onValueChange = { viewModel.kolacjeCount = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }


            }
        }

        // Calculator trigger button
        Button(
            onClick = { viewModel.calculateDiety() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CrmTheme.colors.dark1, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (viewModel.isCalculating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Oblicz diety z systemu", fontWeight = FontWeight.Bold)
            }
        }

        // Calculation errors or successes
        if (!viewModel.calculationError.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.bad.bg),
                border = BorderStroke(1.dp, CrmTheme.colors.bad.dot),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = viewModel.calculationError ?: "",
                    color = CrmTheme.colors.bad.text,
                    modifier = Modifier.padding(16.dp),
                    style = CrmTheme.type.body
                )
            }
        }

        val calc = viewModel.costCalculated
        if (calc != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.ok.bg),
                border = BorderStroke(1.dp, CrmTheme.colors.ok.dot),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Wyniki obliczeń z serwera:", style = CrmTheme.type.titleMd, color = CrmTheme.colors.ok.text)
                    
                    val calculatedDiety = (calc.dieta ?: 0.0) + (calc.krajowyDieta ?: 0.0) + (calc.zagranicznyDieta ?: 0.0)
                    val calculatedRyczalt = (calc.ryczaltNocleg ?: 0.0) + (calc.krajowyRyczaltNocleg ?: 0.0) + (calc.zagranicznyRyczaltNocleg ?: 0.0)
                    val calculatedKm = (calc.kilometrowka ?: 0.0) + (calc.kilometrowkaPln ?: 0.0)

                    CalculatedCostRow(label = "Diety", value = calculatedDiety)
                    CalculatedCostRow(label = "Ryczałt za noclegi", value = calculatedRyczalt)
                    if (calculatedKm > 0.0) {
                        CalculatedCostRow(label = "Kilometrówka", value = calculatedKm)
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayValue = if (value.isNotBlank()) {
        try {
            val parts = value.split("-")
            "${parts[2]}.${parts[1]}.${parts[0]}"
        } catch (_: Exception) { value }
    } else ""

    OutlinedButton(
        onClick = {
            val calendar = Calendar.getInstance()
            if (value.isNotBlank()) {
                try {
                    val parts = value.split("-")
                    calendar.set(Calendar.YEAR, parts[0].toInt())
                    calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                } catch (_: Exception) {}
            }
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val paddedMonth = (month + 1).toString().padStart(2, '0')
                    val paddedDay = day.toString().padStart(2, '0')
                    onValueChange("${year}-${paddedMonth}-${paddedDay}")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CrmTheme.colors.muted.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmTheme.colors.ink),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, style = CrmTheme.type.caption, color = CrmTheme.colors.muted)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = displayValue.ifBlank { "dd.mm.rrrr" },
                    style = CrmTheme.type.body,
                    color = if (displayValue.isBlank()) CrmTheme.colors.muted else CrmTheme.colors.ink
                )
            }
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = CrmTheme.colors.muted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayValue = value

    OutlinedButton(
        onClick = {
            val calendar = Calendar.getInstance()
            if (value.isNotBlank() && value.contains(":")) {
                try {
                    val parts = value.split(":")
                    calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    calendar.set(Calendar.MINUTE, parts[1].toInt())
                } catch (_: Exception) {}
            }
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val paddedHour = hour.toString().padStart(2, '0')
                    val paddedMin = minute.toString().padStart(2, '0')
                    onValueChange("${paddedHour}:${paddedMin}")
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CrmTheme.colors.muted.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmTheme.colors.ink),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, style = CrmTheme.type.caption, color = CrmTheme.colors.muted)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = displayValue.ifBlank { "--:--" },
                    style = CrmTheme.type.body,
                    color = if (displayValue.isBlank()) CrmTheme.colors.muted else CrmTheme.colors.ink
                )
            }
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = CrmTheme.colors.muted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LegCard(
    index: Int,
    route: DelegacjaRouteDto,
    onUpdate: (DelegacjaRouteDto) -> Unit,
    onDelete: () -> Unit,
    showDelete: Boolean
) {
    val isPrivateCar = route.srodekLokomocji == "Samochód prywatny"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface),
        border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(CrmTheme.colors.dark1),
                        contentAlignment = Alignment.Center
                    ) {
                        Text((index + 1).toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isPrivateCar) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🚗 Samochód prywatny", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
                if (showDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrmTheme.colors.bad.text)
                    }
                }
            }

            // Row 1: Środek lokomocji & Kilometry (if private car)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                var locomotionExpanded by remember { mutableStateOf(false) }
                val options = listOf("Samochód prywatny", "Samochód służbowy", "Pociąg", "Samolot", "Inny")

                Column(modifier = Modifier.weight(1f)) {
                    Text("Środek lokomocji *", style = CrmTheme.type.caption, color = CrmTheme.colors.muted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { locomotionExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CrmTheme.colors.muted.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmTheme.colors.ink),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(route.srodekLokomocji, maxLines = 1)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = locomotionExpanded,
                            onDismissRequest = { locomotionExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        val nextRoute = route.copy(srodekLokomocji = opt)
                                        if (opt != "Samochód prywatny") {
                                            onUpdate(nextRoute.copy(kilometry = 0.0, koszt = 0.0))
                                        } else {
                                            onUpdate(nextRoute.copy(kilometry = 0.0, pojemnoscCm3 = 800, koszt = 0.0))
                                        }
                                        locomotionExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (isPrivateCar) {
                    var kmInput by remember(route.kilometry) {
                        mutableStateOf(if (route.kilometry == null || route.kilometry == 0.0) "" else route.kilometry.toString())
                    }
                    OutlinedTextField(
                        value = kmInput,
                        onValueChange = {
                            kmInput = it
                            val km = it.toDoubleOrNull() ?: 0.0
                            val rate = if ((route.pojemnoscCm3 ?: 800) == 1600) 1.15 else 0.89
                            onUpdate(route.copy(kilometry = km, koszt = km * rate))
                        },
                        label = { Text("Kilometry *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Row 2: Pojemność silnika & Miejscowość wyjazdu
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isPrivateCar) {
                    var capacityExpanded by remember { mutableStateOf(false) }
                    val currentCapText = if ((route.pojemnoscCm3 ?: 800) == 1600) ">900 cm³ (1,15 zł/km)" else "≤900 cm³ (0,89 zł/km)"

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pojemność silnika *", style = CrmTheme.type.caption, color = CrmTheme.colors.muted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { capacityExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CrmTheme.colors.muted.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmTheme.colors.ink),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(currentCapText, maxLines = 1)
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = capacityExpanded,
                                onDismissRequest = { capacityExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("≤900 cm³ (0,89 zł/km)") },
                                    onClick = {
                                        val km = route.kilometry ?: 0.0
                                        onUpdate(route.copy(pojemnoscCm3 = 800, koszt = km * 0.89))
                                        capacityExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(">900 cm³ (1,15 zł/km)") },
                                    onClick = {
                                        val km = route.kilometry ?: 0.0
                                        onUpdate(route.copy(pojemnoscCm3 = 1600, koszt = km * 1.15))
                                        capacityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = route.wyjazdMiejscowosc ?: "",
                    onValueChange = { onUpdate(route.copy(wyjazdMiejscowosc = it)) },
                    label = { Text("Miejscowość wyjazdu") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("Skąd") }
                )
            }

            // Row 3: Miejscowość przyjazdu & Data wyjazdu
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = route.przyjazdMiejscowosc ?: "",
                    onValueChange = { onUpdate(route.copy(przyjazdMiejscowosc = it)) },
                    label = { Text("Miejscowość przyjazdu") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("Dokąd") }
                )

                DatePickerField(
                    label = "Data wyjazdu",
                    value = route.wyjazdData ?: "",
                    onValueChange = { onUpdate(route.copy(wyjazdData = it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: Godzina wyjazdu & Data przyjazdu
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TimePickerField(
                    label = "Godzina wyjazdu",
                    value = route.wyjazdGodzina ?: "",
                    onValueChange = { onUpdate(route.copy(wyjazdGodzina = it)) },
                    modifier = Modifier.weight(1f)
                )

                DatePickerField(
                    label = "Data przyjazdu",
                    value = route.przyjazdData ?: "",
                    onValueChange = { onUpdate(route.copy(przyjazdData = it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 5: Godzina przyjazdu
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TimePickerField(
                    label = "Godzina przyjazdu",
                    value = route.przyjazdGodzina ?: "",
                    onValueChange = { onUpdate(route.copy(przyjazdGodzina = it)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Kilometrówka banner
            if (isPrivateCar) {
                val km = route.kilometry ?: 0.0
                val isLarge = (route.pojemnoscCm3 ?: 800) == 1600
                val rate = if (isLarge) 1.15 else 0.89
                val cost = km * rate
                val capLabel = if (isLarge) ">900 cm³" else "≤900 cm³"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, Color(0xFFC5E1A5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "Kilometrówka: %.0f km x %.2f zł = %.2f PLN (pojemność %s)",
                            km, rate, cost, capLabel
                        ),
                        style = CrmTheme.type.body,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
fun DateTimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current
    // Format is "yyyy-MM-ddTHH:mm"
    val displayValue = value.replace("T", " ")

    OutlinedButton(
        onClick = {
            val calendar = Calendar.getInstance()
            // Try to parse existing value
            if (value.contains("T")) {
                try {
                    val parts = value.split("T")
                    val dateParts = parts[0].split("-")
                    val timeParts = parts[1].split(":")
                    calendar.set(Calendar.YEAR, dateParts[0].toInt())
                    calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                } catch (_: Exception) {}
            }

            // Launch date picker first
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    // Launch time picker next
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val paddedMonth = (month + 1).toString().padStart(2, '0')
                            val paddedDay = day.toString().padStart(2, '0')
                            val paddedHour = hour.toString().padStart(2, '0')
                            val paddedMin = minute.toString().padStart(2, '0')
                            onValueChange("${year}-${paddedMonth}-${paddedDay}T${paddedHour}:${paddedMin}")
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CrmTheme.colors.muted.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmTheme.colors.ink)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(label, style = CrmTheme.type.caption, color = CrmTheme.colors.muted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = displayValue.ifBlank { "-- Wybierz date i czas --" },
                style = CrmTheme.type.body,
                color = if (displayValue.isBlank()) CrmTheme.colors.muted else CrmTheme.colors.ink
            )
        }
    }
}

@Composable
fun NumericCounter(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface2),
        border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, style = CrmTheme.type.caption, color = CrmTheme.colors.muted)
                Text(value.toString(), style = CrmTheme.type.titleMd, color = CrmTheme.colors.ink)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = { if (value > 0) onValueChange(value - 1) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, CrmTheme.colors.cardBorder)
                ) {
                    Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                OutlinedButton(
                    onClick = { onValueChange(value + 1) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, CrmTheme.colors.cardBorder)
                ) {
                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun CalculatedCostRow(label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = CrmTheme.type.body, color = CrmTheme.colors.ok.text)
        Text(
            text = String.format(Locale.US, "%.2f PLN", value),
            style = CrmTheme.type.body,
            fontWeight = FontWeight.Bold,
            color = CrmTheme.colors.ok.text
        )
    }
}

@Composable
fun Step3CostsSummary(
    viewModel: DelegacjaCreateViewModel,
    attachments: List<Uri>,
    onAddAttachmentClick: () -> Unit,
    onDeleteAttachment: (Uri) -> Unit,
    onSubmitClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Calculation results
    val calc = viewModel.costCalculated
    val apiDiety = (calc?.dieta ?: 0.0) + (calc?.krajowyDieta ?: 0.0) + (calc?.zagranicznyDieta ?: 0.0)
    val apiRyczaltNocleg = (calc?.ryczaltNocleg ?: 0.0) + (calc?.krajowyRyczaltNocleg ?: 0.0) + (calc?.zagranicznyRyczaltNocleg ?: 0.0)

    val manualNoclegi = viewModel.manualNoclegiRachunki.toDoubleOrNull() ?: 0.0
    val manualDoj = viewModel.manualRyczaltyDojazdy.toDoubleOrNull() ?: 0.0
    val manualDojUdok = viewModel.manualDojazdyUdokumentowane.toDoubleOrNull() ?: 0.0
    val manualInne = viewModel.manualInneWydatki.toDoubleOrNull() ?: 0.0

    val totalCost = apiDiety + apiRyczaltNocleg + manualNoclegi + manualDoj + manualDojUdok + manualInne
    val advance = viewModel.zaliczkaKwota.toDoubleOrNull() ?: 0.0
    val diff = totalCost - advance

    // Handle Success Screen
    if (viewModel.submitResult is NetworkResult.Success) {
        val created = (viewModel.submitResult as NetworkResult.Success<CreateDelegacjaResponse>).data
        SuccessScreen(delegacjaId = created?.id ?: 0, onReset = { viewModel.resetWizard() })
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Podsumowanie i koszty", style = CrmTheme.type.headline, color = CrmTheme.colors.ink)

        // Cost Calculation Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.dark1),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PODSUMOWANIE FINANSOWE",
                    style = CrmTheme.type.label,
                    color = CrmTheme.colors.onDarkMuted
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Razem koszty:", style = CrmTheme.type.body, color = CrmTheme.colors.onDark)
                    Text(
                        text = String.format(Locale.US, "%.2f PLN", totalCost),
                        style = CrmTheme.type.headline,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pobrana zaliczka:", style = CrmTheme.type.body, color = CrmTheme.colors.onDark)
                    Text(
                        text = String.format(Locale.US, "%.2f PLN", advance),
                        style = CrmTheme.type.titleMd,
                        color = CrmTheme.colors.onDarkSubtle
                    )
                }

                Divider(color = CrmTheme.colors.onDarkGlass)

                if (diff >= 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Do wypłaty dla pracownika:", style = CrmTheme.type.body, color = CrmTheme.colors.onDark)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CrmTheme.colors.ok.bg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.2f PLN", diff),
                                style = CrmTheme.type.titleMd,
                                color = CrmTheme.colors.ok.text,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Do zwrotu do kasy firmy:", style = CrmTheme.type.body, color = CrmTheme.colors.onDark)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CrmTheme.colors.warn.bg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.2f PLN", -diff),
                                style = CrmTheme.type.titleMd,
                                color = CrmTheme.colors.warn.text,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Manual Cost Fields
        Text("Wprowadź dodatkowe koszty (Manualne)", style = CrmTheme.type.titleMd, color = CrmTheme.colors.ink)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface),
            border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.manualNoclegiRachunki,
                    onValueChange = { viewModel.manualNoclegiRachunki = it },
                    label = { Text("Noclegi (rachunki)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = viewModel.manualRyczaltyDojazdy,
                    onValueChange = { viewModel.manualRyczaltyDojazdy = it },
                    label = { Text("Ryczałty za dojazdy (np. bagażówki)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = viewModel.manualDojazdyUdokumentowane,
                    onValueChange = { viewModel.manualDojazdyUdokumentowane = it },
                    label = { Text("Dojazdy udokumentowane") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = viewModel.manualInneWydatki,
                    onValueChange = { viewModel.manualInneWydatki = it },
                    label = { Text("Inne wydatki") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Section: File attachments
        Text("Załączniki i rachunki (Zdjęcia)", style = CrmTheme.type.titleMd, color = CrmTheme.colors.ink)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface),
            border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAddAttachmentClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmTheme.colors.primaryDeep),
                    border = BorderStroke(1.dp, CrmTheme.colors.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add photo")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dodaj zdjęcie/fakturę", fontWeight = FontWeight.Bold)
                }

                if (attachments.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(attachments) { uri ->
                            AttachmentPreview(uri = uri, onDelete = { onDeleteAttachment(uri) })
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Brak dodanych załączników.",
                            style = CrmTheme.type.caption,
                            color = CrmTheme.colors.muted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Submit action button
        Button(
            onClick = onSubmitClick,
            enabled = !viewModel.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = CrmTheme.colors.primary,
                contentColor = CrmTheme.colors.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (viewModel.isSubmitting) {
                CircularProgressIndicator(color = CrmTheme.colors.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Zatwierdź i wyślij delegację", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Show Submission Error Banner
        if (viewModel.submitResult is NetworkResult.Error) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.bad.bg),
                border = BorderStroke(1.dp, CrmTheme.colors.bad.dot),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = (viewModel.submitResult as NetworkResult.Error).message ?: "Błąd podczas przesyłania delegacji.",
                    color = CrmTheme.colors.bad.text,
                    modifier = Modifier.padding(16.dp),
                    style = CrmTheme.type.body
                )
            }
        }
    }
}

@Composable
fun AttachmentPreview(uri: Uri, onDelete: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            android.graphics.BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, CrmTheme.colors.cardBorder, RoundedCornerShape(8.dp))
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .padding(2.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun SuccessScreen(
    delegacjaId: Int,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(CrmTheme.colors.ok.bg),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = CrmTheme.colors.ok.text, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Delegacja utworzona!",
            style = CrmTheme.type.headline,
            color = CrmTheme.colors.ok.text,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Twój wniosek oraz rozliczenie delegacji służbowej zostały pomyślnie zapisane w systemie CRM.",
            style = CrmTheme.type.body,
            color = CrmTheme.colors.muted,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 20.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CrmTheme.colors.surface),
            border = BorderStroke(1.dp, CrmTheme.colors.cardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("NUMER DELEGACJI:", style = CrmTheme.type.caption, color = CrmTheme.colors.muted)
                Text("#$delegacjaId", style = CrmTheme.type.titleMd, color = CrmTheme.colors.primaryDeep, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = CrmTheme.colors.primary, contentColor = CrmTheme.colors.onPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Utwórz nową delegację", fontWeight = FontWeight.Bold)
        }
    }
}
