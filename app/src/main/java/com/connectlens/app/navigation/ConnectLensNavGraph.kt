package com.connectlens.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.connectlens.app.feature.analytics.AnalyticsScreen
import com.connectlens.app.feature.callhistory.CallHistoryScreen
import com.connectlens.app.feature.contacts.ContactDetailScreen
import com.connectlens.app.feature.contacts.ContactListScreen
import com.connectlens.app.feature.dashboard.DashboardScreen
import com.connectlens.app.feature.onboarding.OnboardingScreen
import com.connectlens.app.feature.settings.SettingsScreen
import java.net.URLDecoder

/**
 * Root navigation graph for ConnectLens wrapped with [MainScaffold].
 */
@Composable
fun ConnectLensNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Onboarding.route
) {
    MainScaffold(navController = navController) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController     = navController,
                startDestination  = startDestination
            ) {

                // ── Onboarding ────────────────────────────────────────────────────
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onOnboardingCompleted = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                // ── Dashboard ─────────────────────────────────────────────────────
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToContact  = { contactId ->
                            navController.navigate(Screen.ContactDetail.createRoute(contactId, null))
                        }
                    )
                }

                // ── Contact List ──────────────────────────────────────────────────
                composable(Screen.ContactList.route) {
                    ContactListScreen(
                        onNavigateBack     = { navController.popBackStack() },
                        onNavigateToDetail = { contactId ->
                            navController.navigate(Screen.ContactDetail.createRoute(contactId, null))
                        }
                    )
                }

                // ── Contact Detail ────────────────────────────────────────────────
                composable(
                    route = Screen.ContactDetail.route,
                    arguments = listOf(
                        navArgument("contactId") {
                            type = NavType.StringType
                            nullable = false
                            defaultValue = "null"
                        },
                        navArgument("phoneNumber") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val contactIdStr  = backStackEntry.arguments?.getString("contactId")
                    val contactId     = contactIdStr?.takeIf { it != "null" }?.toLongOrNull()
                    val rawPhone      = backStackEntry.arguments?.getString("phoneNumber")
                    val phoneNumber   = rawPhone
                        ?.takeIf { it.isNotBlank() }
                        ?.let { URLDecoder.decode(it, "UTF-8") }

                    ContactDetailScreen(
                        contactId      = contactId,
                        phoneNumber    = phoneNumber,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ── Call History ──────────────────────────────────────────────────
                composable(Screen.CallHistory.route) {
                    CallHistoryScreen(
                        onNavigateBack      = { navController.popBackStack() },
                        onRequestPermission = { }
                    )
                }

                // ── Analytics ─────────────────────────────────────────────────────
                composable(Screen.Analytics.route) {
                    AnalyticsScreen(onNavigateBack = { navController.popBackStack() })
                }

                // ── Settings ──────────────────────────────────────────────────────
                composable(Screen.Settings.route) {
                    SettingsScreen(onNavigateBack = { navController.popBackStack() })
                }
            }
        }
    }
}
