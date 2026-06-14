package com.ossadkowski.crm.mobile.ui.nawozy

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ossadkowski.crm.mobile.ui.nawozy.nav.NawozyRoutes
import com.ossadkowski.crm.mobile.ui.nawozy.screens.kontrahent.KontrahentPickerScreen
import com.ossadkowski.crm.mobile.ui.nawozy.screens.koszyk.KoszykScreen
import com.ossadkowski.crm.mobile.ui.nawozy.screens.lista.ZamowieniaListScreen
import com.ossadkowski.crm.mobile.ui.nawozy.screens.towar.TowarPickerScreen

/**
 * Compose-Navigation graph for the fertiliser-order module.
 *
 * Live destination: [NawozyRoutes.LISTA] (the orders list). The customer picker,
 * cart, product picker and logistics destinations are wired as placeholders here
 * and replaced by real screens in phases 2–3 — the graph compiles end-to-end so
 * navigation from the list (FAB → customer picker, row → cart) already works.
 */
@Composable
fun NawozyNavHost(
    navController: NavHostController = rememberNavController(),
    onMenuClick: () -> Unit,
) {
    NavHost(navController = navController, startDestination = NawozyRoutes.LISTA) {

        composable(NawozyRoutes.LISTA) {
            ZamowieniaListScreen(
                onMenuClick = onMenuClick,
                onCreateNew = { navController.navigate(NawozyRoutes.KONTRAHENT) },
                onOrderClick = { koszykId -> navController.navigate(NawozyRoutes.koszyk(koszykId)) },
            )
        }

        composable(NawozyRoutes.KONTRAHENT) {
            KontrahentPickerScreen(
                onBack = { navController.popBackStack() },
                onKoszykStarted = { koszykId ->
                    navController.navigate(NawozyRoutes.koszyk(koszykId)) {
                        // Replace the picker so hardware-back from the cart returns to the list.
                        popUpTo(NawozyRoutes.KONTRAHENT) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = NawozyRoutes.KOSZYK,
            arguments = listOf(navArgument(NawozyRoutes.ARG_KOSZYK_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val koszykId = backStackEntry.arguments?.getLong(NawozyRoutes.ARG_KOSZYK_ID) ?: 0L
            KoszykScreen(
                onBack = { navController.popBackStack() },
                onAddProduct = { navController.navigate(NawozyRoutes.towarPicker(koszykId)) },
                onSubmitted = {
                    // Order placed → back to the list (refreshes on its own).
                    navController.popBackStack(NawozyRoutes.LISTA, inclusive = false)
                },
            )
        }

        composable(
            route = NawozyRoutes.TOWAR_PICKER,
            arguments = listOf(navArgument(NawozyRoutes.ARG_KOSZYK_ID) { type = NavType.LongType }),
        ) {
            TowarPickerScreen(
                onBack = { navController.popBackStack() },
                // Line added → back to the cart, which reloads on resume.
                onAdded = { navController.popBackStack() },
            )
        }
    }
}
