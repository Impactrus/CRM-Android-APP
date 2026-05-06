package com.ossadkowski.crm.mobile.ui.serwis

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ossadkowski.crm.mobile.ui.serwis.nav.SerwisRoutes
import com.ossadkowski.crm.mobile.ui.serwis.screens.machine.Machine360Screen
import com.ossadkowski.crm.mobile.ui.serwis.screens.machines.MachinesScreen
import com.ossadkowski.crm.mobile.ui.serwis.screens.mytime.MyTimeScreen
import com.ossadkowski.crm.mobile.ui.serwis.screens.order.OrderDetailScreen
import com.ossadkowski.crm.mobile.ui.serwis.screens.parts.PartsScreen
import com.ossadkowski.crm.mobile.ui.serwis.screens.profile.ProfileScreen
import com.ossadkowski.crm.mobile.ui.serwis.screens.today.MyDayScreen
import com.ossadkowski.crm.mobile.ui.serwis.screens.workcard.WorkCardScreen

/**
 * Compose-Navigation graph for the Serwis (field-service) module.
 *
 * Hosts 12 destinations:
 *  - 5 flagship screens (Mój dzień / Maszyny / Maszyna 360 / Detal zlecenia / Karta pracy).
 *  - 6 placeholder screens delegating to [PlaceholderScreen] (Plan tygodnia, Skanuj SN,
 *    Części, Alerty, Mój czas, Profil) — wire real composables when those PRs land.
 *
 * Navigation arguments (`serial`, `orderNum`, `cardNum`) are URL-encoded by
 * [SerwisRoutes] helpers and read by each ViewModel via `SavedStateHandle`.
 *
 * The hamburger drawer is owned by the host activity, not this graph — every
 * top-level destination calls [onMenuClick] to surface it.
 */
@Composable
fun SerwisNavHost(
    navController: NavHostController = rememberNavController(),
    onMenuClick: () -> Unit,
    onLogout: () -> Unit = {},
) {
    NavHost(navController = navController, startDestination = SerwisRoutes.TODAY) {
        // 02 — Mój dzień
        composable(SerwisRoutes.TODAY) {
            MyDayScreen(
                onMenuClick = onMenuClick,
                onOrderClick = { orderNum ->
                    navController.navigate(SerwisRoutes.orderDetail(orderNum))
                },
                onNavigatePlan = { navController.navigate(SerwisRoutes.PLAN) },
                onCreateOrder = { /* MVP: snackbar / no-op — backed by drawer FAB later */ },
            )
        }

        // 05 — Maszyny
        composable(SerwisRoutes.MACHINES) {
            MachinesScreen(
                onMenuClick = onMenuClick,
                onMachineClick = { serial ->
                    navController.navigate(SerwisRoutes.machineDetail(serial))
                },
                onScanClick = { navController.navigate(SerwisRoutes.SCAN) },
            )
        }

        // 07 — Maszyna 360
        composable(
            route = SerwisRoutes.MACHINE_DETAIL,
            arguments = listOf(
                navArgument(SerwisRoutes.ARG_SERIAL) { type = NavType.StringType },
            ),
        ) {
            Machine360Screen(
                onBack = { navController.popBackStack() },
                onOrderClick = { orderNum ->
                    navController.navigate(SerwisRoutes.orderDetail(orderNum))
                },
            )
        }

        // 08 — Detal zlecenia
        composable(
            route = SerwisRoutes.ORDER_DETAIL,
            arguments = listOf(
                navArgument(SerwisRoutes.ARG_ORDER_NUM) { type = NavType.StringType },
            ),
        ) {
            OrderDetailScreen(
                onBack = { navController.popBackStack() },
                onWorkCardClick = { orderNum, cardNum ->
                    navController.navigate(SerwisRoutes.workCard(orderNum, cardNum))
                },
            )
        }

        // 13 — Karta pracy / Nowy wpis czasu
        composable(
            route = SerwisRoutes.WORK_CARD,
            arguments = listOf(
                navArgument(SerwisRoutes.ARG_ORDER_NUM) { type = NavType.StringType },
                navArgument(SerwisRoutes.ARG_CARD_NUM) { type = NavType.StringType },
            ),
        ) {
            WorkCardScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        // 03 / 06 / 09 / 10 / 11 / 12 — placeholders (Wkrótce)
        composable(SerwisRoutes.PLAN) {
            PlaceholderScreen(title = "Plan tygodnia", onBack = { navController.popBackStack() })
        }
        composable(SerwisRoutes.SCAN) {
            PlaceholderScreen(title = "Skanuj SN", onBack = { navController.popBackStack() })
        }
        // 09 — Części (Room-backed offline)
        composable(SerwisRoutes.PARTS) {
            PartsScreen(
                onMenuClick = onMenuClick,
                onBack = { navController.popBackStack() },
            )
        }
        composable(SerwisRoutes.ALERTS) {
            PlaceholderScreen(title = "Alerty", onBack = { navController.popBackStack() })
        }

        // 11 — Mój czas
        composable(SerwisRoutes.MY_TIME) {
            MyTimeScreen(
                onMenuClick = onMenuClick,
                onManualEntryClick = {
                    navController.navigate(SerwisRoutes.TODAY) {
                        popUpTo(SerwisRoutes.TODAY) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        // 12 — Profil
        composable(SerwisRoutes.PROFILE) {
            ProfileScreen(
                onMenuClick = onMenuClick,
                onLogout = onLogout,
            )
        }
    }
}
