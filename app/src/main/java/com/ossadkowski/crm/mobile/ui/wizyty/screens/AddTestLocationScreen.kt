package com.ossadkowski.crm.mobile.ui.wizyty.screens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.wizyty.location.LocationPermissions
import com.tomtom.sdk.location.GeoPoint as TomTomGeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.ui.MapView as TomTomMapView
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.gesture.MapClickListener
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.MarkerOptions

/**
 * "Dodaj lokalizację testową": the rep types an address (server-proxied TomTom search),
 * picks a suggestion, or clicks/taps directly on the interactive TomTom map, and saves it
 * as a geofenced test point. Saving (re)starts the work session so the location is watched
 * immediately — arriving there auto-detects a visit and posts a local notification.
 */
@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTestLocationScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddTestLocationViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        if (LocationPermissions.hasForegroundLocation(context)) viewModel.save(onSaved)
    }

    fun onSaveClick() {
        if (LocationPermissions.hasForegroundLocation(context)) {
            viewModel.save(onSaved)
        } else {
            permissionLauncher.launch(LocationPermissions.requiredForegroundPermissions())
        }
    }

    val mapView = remember {
        val options = MapOptions(mapKey = "HYnyrS2sa9vkTCYH5QjDfSWDV3XJMccH")
        TomTomMapView(context, options).apply {
            onCreate(null) // Initialize TomTom map engine context
            getMapAsync { map ->
                // Center on Poland on first load
                map.moveCamera(CameraOptions(position = TomTomGeoPoint(52.069167, 19.480556), zoom = 6.0))
            }
            setOnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
    }

    LaunchedEffect(state.selected) {
        val selected = state.selected
        mapView.getMapAsync { map ->
            map.removeMarkers()
            if (selected != null) {
                val position = TomTomGeoPoint(selected.lat, selected.lng)
                val markerImage = ImageFactory.fromResource(R.drawable.ic_location)
                map.addMarker(MarkerOptions(coordinate = position, pinImage = markerImage))
                map.animateCamera(CameraOptions(position = position, zoom = 15.0))
            }
        }
    }

    DisposableEffect(mapView) {
        var listener: MapClickListener? = null
        var mapInstance: TomTomMap? = null
        mapView.getMapAsync { map ->
            mapInstance = map
            val clickListener = MapClickListener { point ->
                viewModel.onMapClick(point.latitude, point.longitude, context)
                true
            }
            listener = clickListener
            map.addMapClickListener(clickListener)
        }
        onDispose {
            val map = mapInstance
            val clickL = listener
            if (map != null && clickL != null) {
                try {
                    map.removeMapClickListener(clickL)
                } catch (e: Exception) {
                    // Map might be already closed, safe to ignore
                }
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lokalizacja testowa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Wpisz adres testowy lub kliknij bezpośrednio na mapie, aby wybrać lokalizację.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nazwa lokalizacji") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )

            OutlinedTextField(
                value = state.addressQuery,
                onValueChange = viewModel::onAddressQueryChange,
                label = { Text("Adres / Współrzędne") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )

            if (state.suggestions.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column {
                        state.suggestions.forEachIndexed { index, suggestion ->
                            if (index > 0) HorizontalDivider()
                            Text(
                                text = suggestion.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onSuggestionSelected(suggestion) }
                                    .padding(12.dp),
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { mapView }
                )
            }

            state.selected?.let {
                Text(
                    text = "Wybrano: ${"%.5f".format(it.lat)}, ${"%.5f".format(it.lng)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            Button(
                onClick = { onSaveClick() },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp).padding(end = 4.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text("Zapisz i włącz wykrywanie")
            }
        }
    }
}
