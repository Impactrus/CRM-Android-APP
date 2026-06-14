package com.ossadkowski.crm.mobile.ui.nawozy

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.ossadkowski.crm.mobile.BuildConfig
import com.ossadkowski.crm.mobile.MainActivity
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.ui.nawozy.nav.NawozyRoutes
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single-activity Compose host for the fertiliser-order module.
 *
 * Owns the shared hamburger [ModalNavigationDrawer] and the [androidx.navigation.NavHostController]
 * driving [NawozyNavHost]. Reached from the "Zamówienia" drawer entry on the legacy
 * `BaseDrawerActivity` surfaces, gated by
 * [com.ossadkowski.crm.mobile.ui.nawozy.access.NawozyAccessChecker].
 */
@AndroidEntryPoint
class ZamowieniaNawozyActivity : ComponentActivity() {

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

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.widthIn(max = 300.dp),
                            drawerContainerColor = CrmTheme.colors.surface,
                        ) {
                            NawozyDrawerContent(
                                userName = session.fullName.takeIf { it.isNotBlank() }
                                    ?: session.username.takeIf { it.isNotBlank() }
                                    ?: "Handlowiec",
                                appVersion = BuildConfig.VERSION_NAME,
                                onGoToList = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(NawozyRoutes.LISTA) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                onLogout = ::performLogout,
                            )
                        }
                    },
                ) {
                    NawozyNavHost(
                        navController = navController,
                        onMenuClick = { scope.launch { drawerState.open() } },
                    )
                }
            }
        }
    }

    private fun performLogout() {
        session.clear()
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        finish()
    }
}
