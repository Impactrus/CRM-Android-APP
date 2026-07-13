package com.ossadkowski.crm.mobile.ui.wizyty

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity Compose host for the "Wizyty" (GPS visit-detection) module.
 *
 * Drives [WizytyNavHost]. Reached from the "Wizyty" drawer entry on the legacy
 * `BaseDrawerActivity` surfaces — unlike the fertiliser-order module it is open to
 * every logged-in user (no claim gate). The dashboard's back arrow (and
 * hardware/gesture back) finishes the activity, returning to the main app menu.
 */
@AndroidEntryPoint
class WizytyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            CrmTheme {
                WizytyNavHost(
                    navController = rememberNavController(),
                    onExit = { finish() },
                )
            }
        }
    }
}
