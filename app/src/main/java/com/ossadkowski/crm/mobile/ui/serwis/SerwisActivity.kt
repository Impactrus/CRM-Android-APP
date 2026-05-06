package com.ossadkowski.crm.mobile.ui.serwis

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ossadkowski.crm.mobile.BuildConfig
import com.ossadkowski.crm.mobile.MainActivity
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single-activity Compose host for the Serwis (field-service) module.
 *
 * Owns:
 *  - The hamburger [ModalNavigationDrawer] shared across every Serwis route.
 *  - The [androidx.navigation.NavHostController] driving [SerwisNavHost].
 *  - Edge-to-edge window setup (transparent status bar; the hero gradient absorbs
 *    the inset via `Modifier.statusBarsPadding()` inside each screen).
 *
 * Entry surface is the "Serwis" tile on `DashboardActivity` (gated by
 * [com.ossadkowski.crm.mobile.ui.serwis.access.SerwisAccessChecker]). There is no
 * launcher intent-filter — this activity is reachable only from the dashboard.
 */
@AndroidEntryPoint
class SerwisActivity : ComponentActivity() {

    @Inject lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            CrmTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.widthIn(max = 300.dp),
                            drawerContainerColor = CrmTheme.colors.surface,
                        ) {
                            SerwisDrawerContent(
                                currentRoute = currentRoute,
                                technicianName = session.fullName.takeIf { it.isNotBlank() }
                                    ?: session.username.takeIf { it.isNotBlank() }
                                    ?: "Technik",
                                appVersion = BuildConfig.VERSION_NAME,
                                onNavigate = { route ->
                                    scope.launch { drawerState.close() }
                                    if (route != currentRoute) {
                                        navController.navigate(route) {
                                            // Keep TODAY as the bottom of the back stack so
                                            // hardware-back from a top-level dest exits via
                                            // popUpTo without piling up duplicates.
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                onLogout = ::performLogout,
                                onClose = { scope.launch { drawerState.close() } },
                            )
                        }
                    },
                ) {
                    SerwisNavHost(
                        navController = navController,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onLogout = ::performLogout,
                    )
                }
            }
        }
    }

    private fun performLogout() {
        // SessionManager.logout() doesn't exist — clear() is the canonical reset.
        session.clear()
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        finish()
    }
}
