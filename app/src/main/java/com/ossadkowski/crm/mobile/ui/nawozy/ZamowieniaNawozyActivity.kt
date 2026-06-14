package com.ossadkowski.crm.mobile.ui.nawozy

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity Compose host for the fertiliser-order module.
 *
 * Drives [NawozyNavHost]. Reached from the "Zamówienia" drawer entry on the legacy
 * `BaseDrawerActivity` surfaces, gated by
 * [com.ossadkowski.crm.mobile.ui.nawozy.access.NawozyAccessChecker]. The list's
 * back arrow (and hardware/gesture back) finishes the activity, returning to the
 * main app menu that launched it.
 */
@AndroidEntryPoint
class ZamowieniaNawozyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            CrmTheme {
                NawozyNavHost(
                    navController = rememberNavController(),
                    onExit = { finish() },
                )
            }
        }
    }
}
