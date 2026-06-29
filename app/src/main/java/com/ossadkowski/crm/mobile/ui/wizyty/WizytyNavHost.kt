package com.ossadkowski.crm.mobile.ui.wizyty

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ossadkowski.crm.mobile.ui.wizyty.nav.WizytyRoutes
import com.ossadkowski.crm.mobile.ui.wizyty.screens.AddTestLocationScreen
import com.ossadkowski.crm.mobile.ui.wizyty.screens.AddVisitScreen
import com.ossadkowski.crm.mobile.ui.wizyty.screens.TestLocationsListScreen
import com.ossadkowski.crm.mobile.ui.wizyty.screens.VisitsListScreen
import com.ossadkowski.crm.mobile.ui.wizyty.screens.WizytyDashboardScreen

/**
 * Compose-Navigation graph for the "Wizyty" module.
 *
 * Live destination: [WizytyRoutes.DASHBOARD]. The visits list and the manual-add
 * screen are backed by Room (offline-first); the work-session toggle / GPS engine
 * land in later phases.
 */
@Composable
fun WizytyNavHost(
    navController: NavHostController = rememberNavController(),
    onExit: () -> Unit,
) {
    NavHost(navController = navController, startDestination = WizytyRoutes.DASHBOARD) {

        composable(WizytyRoutes.DASHBOARD) {
            WizytyDashboardScreen(
                onBack = onExit,
                onOpenList = { navController.navigate(WizytyRoutes.LIST) },
                onAddVisit = { navController.navigate(WizytyRoutes.ADD) },
                onAddTestLocation = { navController.navigate(WizytyRoutes.TEST_LOCATION) },
                onOpenTestLocations = { navController.navigate(WizytyRoutes.TEST_LOCATIONS) },
            )
        }

        composable(WizytyRoutes.LIST) {
            VisitsListScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(WizytyRoutes.ADD) {
            AddVisitScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(WizytyRoutes.TEST_LOCATION) {
            AddTestLocationScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(WizytyRoutes.TEST_LOCATIONS) {
            TestLocationsListScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
