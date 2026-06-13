package com.ossadkowski.crm.mobile.ui.transport

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ossadkowski.crm.mobile.data.NetworkResult
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

data class VehicleProfile(
    val id: String,
    val label: String,
    val weight: Double,
    val height: Double,
    val width: Double,
    val length: Double,
    val axleWeight: Double,
    val axles: Int
)

val vehicleProfiles = listOf(
    VehicleProfile("van", "Bus / Van 3.5t", 3.5, 2.7, 2.2, 6.0, 2.1, 2),
    VehicleProfile("medium", "Średni 7.5t", 7.5, 3.2, 2.45, 8.0, 4.5, 2),
    VehicleProfile("rigid12", "Sztywny 12t", 12.0, 3.5, 2.5, 10.0, 6.0, 2),
    VehicleProfile("rigid18", "Sztywny 18t", 18.0, 3.8, 2.55, 12.0, 8.0, 2),
    VehicleProfile("rigid26", "Sztywny 26t (3 osie)", 26.0, 4.0, 2.55, 12.0, 10.0, 3),
    VehicleProfile("semi40", "Naczepa 40t (TIR)", 40.0, 4.0, 2.55, 16.5, 11.5, 5),
    VehicleProfile("train44", "Zestaw 44t (transport kombinowany)", 44.0, 4.0, 2.55, 18.75, 11.5, 6),
    VehicleProfile("custom", "Dedykowana", 40.0, 4.0, 2.55, 16.5, 11.5, 5)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportMapScreen(
    viewModel: TransportMapViewModel
) {
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(6.0)
            controller.setCenter(GeoPoint(52.069167, 19.480556)) // Polska
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    val routeState by viewModel.routeState.collectAsState()

    LaunchedEffect(routeState) {
        mapView.overlays.removeAll { it is Polyline || it is Marker }
        val state = routeState
        if (state is NetworkResult.Success) {
            val route = state.data
            if (route != null) {
                val geoPoints = route.points.map { GeoPoint(it.latitude, it.longitude) }
                if (geoPoints.isNotEmpty()) {
                    val polyline = Polyline(mapView).apply {
                        setPoints(geoPoints)
                        outlinePaint.color = android.graphics.Color.parseColor("#1A56DB")
                        outlinePaint.strokeWidth = 10f
                    }
                    mapView.overlays.add(polyline)

                    val startMarker = Marker(mapView).apply {
                        position = geoPoints.first()
                        title = "Start"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(startMarker)

                    val endMarker = Marker(mapView).apply {
                        position = geoPoints.last()
                        title = "Cel"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(endMarker)

                    mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(geoPoints), true)
                }
            }
        }
        mapView.invalidate()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 300.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContainerColor = Color.White,
        sheetContent = {
            TransportBottomSheetContent(viewModel)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapView }
            )

            // Trasa Overlay
            val state = routeState
            if (state is NetworkResult.Success) {
                val route = state.data
                if (route != null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val kmStr = String.format("%.1f km", route.lengthInMeters / 1000.0)
                                Text(
                                    text = kmStr,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                val hours = route.travelTimeInSeconds / 3600
                                val mins = (route.travelTimeInSeconds % 3600) / 60
                                val timeStr = if (hours > 0) "${hours}h ${mins}min" else "${mins}min"
                                Text(
                                    text = timeStr,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF1A56DB)
                                )
                            }
                        }
                    }
                }
            } else if (state is NetworkResult.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (routeState is NetworkResult.Error) {
                val errorMsg = (routeState as NetworkResult.Error).message ?: "Błąd obliczania trasy"
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Text(errorMsg)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportBottomSheetContent(viewModel: TransportMapViewModel) {
    var startPoint by remember { mutableStateOf("") }
    var endPoint by remember { mutableStateOf("") }
    var startGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var endGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }

    var selectedProfile by remember { mutableStateOf(vehicleProfiles.find { it.id == "semi40" } ?: vehicleProfiles.last()) }
    var length by remember { mutableStateOf(selectedProfile.length.toString().replace('.', ',')) }
    var width by remember { mutableStateOf(selectedProfile.width.toString().replace('.', ',')) }
    var height by remember { mutableStateOf(selectedProfile.height.toString().replace('.', ',')) }
    var maxSpeed by remember { mutableStateOf("89") }
    var weight by remember { mutableStateOf(selectedProfile.weight.toString().replace('.', ',')) }
    var axleWeight by remember { mutableStateOf(selectedProfile.axleWeight.toString().replace('.', ',')) }
    var axles by remember { mutableStateOf(selectedProfile.axles.toString()) }

    var autostrady by remember { mutableStateOf(false) }
    var drogiPlatne by remember { mutableStateOf(false) }
    var promy by remember { mutableStateOf(false) }
    var drogiNieutwardzone by remember { mutableStateOf(false) }
    var pasyHov by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    var startExpanded by remember { mutableStateOf(false) }
    var endExpanded by remember { mutableStateOf(false) }

    val startSuggestions by viewModel.startSuggestions.collectAsState()
    val endSuggestions by viewModel.endSuggestions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Transport - Trasa ciężarówki", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = startPoint,
                onValueChange = {
                    startPoint = it
                    viewModel.searchStartGeocode(it)
                    startExpanded = it.length >= 3
                },
                label = { Text("Punkt startowy") },
                placeholder = { Text("Wpisz adres początkowy...") },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = startExpanded && startSuggestions.isNotEmpty(),
                onDismissRequest = { startExpanded = false },
                properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                modifier = Modifier.fillMaxWidth()
            ) {
                startSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion.label) },
                        onClick = {
                            startPoint = suggestion.label
                            startGeoPoint = GeoPoint(suggestion.lat, suggestion.lng)
                            startExpanded = false
                            viewModel.clearSuggestions()
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = endPoint,
                onValueChange = {
                    endPoint = it
                    viewModel.searchEndGeocode(it)
                    endExpanded = it.length >= 3
                },
                label = { Text("Punkt docelowy") },
                placeholder = { Text("Wpisz adres docelowy...") },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = endExpanded && endSuggestions.isNotEmpty(),
                onDismissRequest = { endExpanded = false },
                properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                modifier = Modifier.fillMaxWidth()
            ) {
                endSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion.label) },
                        onClick = {
                            endPoint = suggestion.label
                            endGeoPoint = GeoPoint(suggestion.lat, suggestion.lng)
                            endExpanded = false
                            viewModel.clearSuggestions()
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Dodaj punkt pośredni")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text("PROFIL POJAZDU", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedProfile.label,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                vehicleProfiles.forEach { profile ->
                    DropdownMenuItem(
                        text = { Text(profile.label) },
                        onClick = {
                            selectedProfile = profile
                            length = profile.length.toString().replace('.', ',')
                            width = profile.width.toString().replace('.', ',')
                            height = profile.height.toString().replace('.', ',')
                            weight = profile.weight.toString().replace('.', ',')
                            axleWeight = profile.axleWeight.toString().replace('.', ',')
                            axles = profile.axles.toString()
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Parametry pojazdu (w 2 kolumnach)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = length,
                onValueChange = { length = it },
                label = { Text("Długość") },
                trailingIcon = { Text("m", modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = width,
                onValueChange = { width = it },
                label = { Text("Szerokość") },
                trailingIcon = { Text("m", modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Wysokość") },
                trailingIcon = { Text("m", modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxSpeed,
                onValueChange = { maxSpeed = it },
                label = { Text("Maks. prędkość") },
                trailingIcon = { Text("km/h", modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Masa brutto") },
                trailingIcon = { Text("t", modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = axleWeight,
                onValueChange = { axleWeight = it },
                label = { Text("Nacisk osi") },
                trailingIcon = { Text("t", modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = axles,
            onValueChange = { axles = it },
            label = { Text("Liczba osi") },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(end = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("OMIJANIE", fontWeight = FontWeight.SemiBold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = autostrady, onCheckedChange = { autostrady = it })
            Text("Autostrady")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = drogiPlatne, onCheckedChange = { drogiPlatne = it })
            Text("Drogi płatne")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = promy, onCheckedChange = { promy = it })
            Text("Promy")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = drogiNieutwardzone, onCheckedChange = { drogiNieutwardzone = it })
            Text("Drogi nieutwardzone")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = pasyHov, onCheckedChange = { pasyHov = it })
            Text("Pasy HOV/carpool")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    startPoint = ""
                    endPoint = ""
                    startGeoPoint = null
                    endGeoPoint = null
                    selectedProfile = vehicleProfiles.find { it.id == "semi40" } ?: vehicleProfiles.last()
                    length = selectedProfile.length.toString().replace('.', ',')
                    width = selectedProfile.width.toString().replace('.', ',')
                    height = selectedProfile.height.toString().replace('.', ',')
                    maxSpeed = "89"
                    weight = selectedProfile.weight.toString().replace('.', ',')
                    axleWeight = selectedProfile.axleWeight.toString().replace('.', ',')
                    axles = selectedProfile.axles.toString()
                    autostrady = false
                    drogiPlatne = false
                    promy = false
                    drogiNieutwardzone = false
                    pasyHov = false
                    viewModel.clearRoute()
                    viewModel.clearSuggestions()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Wyczyść")
            }
            Button(
                onClick = {
                    if (startGeoPoint != null && endGeoPoint != null) {
                        val wKg = (weight.replace(',', '.').toDoubleOrNull() ?: 40.0) * 1000
                        val h = height.replace(',', '.').toDoubleOrNull() ?: 4.0
                        val w = width.replace(',', '.').toDoubleOrNull() ?: 2.55
                        val l = length.replace(',', '.').toDoubleOrNull() ?: 16.5
                        val aw = (axleWeight.replace(',', '.').toDoubleOrNull() ?: 11.5) * 1000
                        val ax = axles.toIntOrNull() ?: 5
                        val sp = maxSpeed.toIntOrNull()

                        viewModel.calculateRoute(
                            startLat = startGeoPoint!!.latitude,
                            startLng = startGeoPoint!!.longitude,
                            endLat = endGeoPoint!!.latitude,
                            endLng = endGeoPoint!!.longitude,
                            weight = wKg.toInt(),
                            height = h,
                            width = w,
                            length = l,
                            axleWeight = aw.toInt(),
                            axles = ax,
                            maxSpeed = sp
                        )
                    }
                },
                enabled = startGeoPoint != null && endGeoPoint != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Oblicz trasę")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
