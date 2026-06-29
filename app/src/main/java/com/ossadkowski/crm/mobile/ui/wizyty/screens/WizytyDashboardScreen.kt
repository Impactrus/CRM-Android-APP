package com.ossadkowski.crm.mobile.ui.wizyty.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.data.wizyty.location.LocationPermissions
import com.ossadkowski.crm.mobile.ui.wizyty.common.NonLiveDataBanner

/**
 * Wizyty landing screen: the work-session toggle (starts/stops GPS detection), plus
 * entries into the visits list and the manual-add flow. Auto-detected visits still
 * require manual confirmation in the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizytyDashboardScreen(
    onBack: () -> Unit,
    onOpenList: () -> Unit,
    onAddVisit: () -> Unit,
    onAddTestLocation: () -> Unit,
    onOpenTestLocations: () -> Unit,
    viewModel: WizytyDashboardViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val sessionActive by viewModel.sessionActive.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        // Start as soon as foreground location is available; background can be added later.
        if (LocationPermissions.hasForegroundLocation(context)) viewModel.startSession()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wizyty") },
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
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NonLiveDataBanner()

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (sessionActive) "Sesja pracy: aktywna" else "Sesja pracy: nieaktywna",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Podczas sesji aplikacja wykrywa wizyty u kontrahentów w pobliżu " +
                            "i pokazuje powiadomienie. Poza sesją nic nie jest śledzone.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (sessionActive) {
                        OutlinedButton(
                            onClick = viewModel::stopSession,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Zakończ sesję") }

                        if (!LocationPermissions.hasBackgroundLocation(context)) {
                            Text(
                                text = "Wykrywanie w tle działa niezawodnie tylko z uprawnieniem " +
                                    "\"Zawsze zezwalaj\". Bez niego wizyty są wykrywane głównie, " +
                                    "gdy aplikacja jest otwarta lub widoczne jest powiadomienie sesji.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                            TextButton(onClick = { openAppSettings(context) }) {
                                Text("Włącz lokalizację w tle (Zawsze zezwalaj)")
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (LocationPermissions.hasForegroundLocation(context)) {
                                    viewModel.startSession()
                                } else {
                                    permissionLauncher.launch(LocationPermissions.requiredForegroundPermissions())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Rozpocznij sesję") }
                    }
                }
            }

            Button(
                onClick = onOpenList,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Filled.List, contentDescription = null)
                Text("  Lista wizyt")
            }

            OutlinedButton(
                onClick = onAddVisit,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  Dodaj wizytę ręcznie")
            }

            OutlinedButton(
                onClick = onAddTestLocation,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Filled.AddLocationAlt, contentDescription = null)
                Text("  Dodaj lokalizację testową")
            }

            OutlinedButton(
                onClick = onOpenTestLocations,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Filled.PinDrop, contentDescription = null)
                Text("  Lokalizacje testowe")
            }
        }
    }
}

private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
