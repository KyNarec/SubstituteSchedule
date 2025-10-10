package org.substitute.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import kotlinx.coroutines.CoroutineScope
import org.substitute.schedule.networking.DsbApiClient
import org.substitute.schedule.networking.TimeTable
import org.substitute.schedule.ui.screens.LoadingTimeTables
import org.substitute.schedule.ui.screens.SettingsScreen
import org.substitute.schedule.ui.screens.WebViewScreen
import org.substitute.schedule.ui.theme.AppTheme
import org.substitute.schedule.utils.Constants.PASSWORD
import org.substitute.schedule.utils.Constants.USERNAME
import org.substitute.schedule.utils.Destination
import org.substitute.schedule.utils.SecureStorage
import org.substitute.schedule.utils.enums.SelectedScreen

@Composable
fun App(
    client: DsbApiClient,
    storage: SecureStorage
) {
    AppTheme() {
        var selectedScreen by remember { mutableStateOf(SelectedScreen.TODAY) }
        var distinctTables by remember { mutableStateOf<List<TimeTable>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var loadAttemptFailed by remember { mutableStateOf(false) }


        LaunchedEffect(Unit) {
            client.username = storage.getString(USERNAME).toString()
            client.password = storage.getString(PASSWORD).toString()

            println("App(): Username: ${client.username}")
            println("App(): Password: ${client.password}")

            try {
                val tables = client.getTimeTables()
                distinctTables = tables.distinctBy { it.uuid }
                isLoading = false
                loadAttemptFailed = false
            } catch (e: Exception) {
                println("App(): Error loading timetables: ${e.message}")
                isLoading = false
                loadAttemptFailed = true
            }
        }


        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedScreen == SelectedScreen.TODAY,
                        onClick = {
                            if (distinctTables.size > 1 && navController.currentDestination != Destination.Settings) {
                                navController.navigate(Destination.Today(distinctTables[0].detail))
                                selectedScreen = SelectedScreen.TODAY
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Today",
                            )
                        }
                    )

                    NavigationBarItem(
                        selected = selectedScreen == SelectedScreen.TOMORROW,
                        onClick = {
                            if (distinctTables.size > 1 && navController.currentDestination != Destination.Settings) {
                                navController.navigate(Destination.Tomorrow(distinctTables[1].detail))
                                selectedScreen = SelectedScreen.TOMORROW
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Tomorrow",
                            )
                        }
                    )

                    NavigationBarItem(
                        selected = selectedScreen == SelectedScreen.SETTINGS,
                        onClick = {
                            navController.navigate(Destination.Settings(false))
                            selectedScreen = SelectedScreen.SETTINGS
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                            )
                        }
                    )
                }
            },
        ) { contentPadding ->

            NavHost(
                navController,
                startDestination = Destination.InitialLoadingRoute
            ) {
                composable<Destination.InitialLoadingRoute> {

                    LaunchedEffect(isLoading) {

                        if (!isLoading) {

                            if (client.username.isNotEmpty() && !loadAttemptFailed) {
                                val url = distinctTables.first().detail

                                // Use popBackStack/replace to prevent the loading route from being on the back stack
                                navController.popBackStack()
                                navController.navigate(Destination.Today(url))
                            } else {
                                navController.popBackStack()
                                navController.navigate(Destination.Settings(true))
                            }
                        }
                    }

                    LoadingTimeTables()
                }

                composable<Destination.Today> {
                    selectedScreen = SelectedScreen.TODAY

                    val args = it.toRoute<Destination.Today>()
                    WebViewScreen(args.url)
                }

                composable<Destination.Tomorrow> {
                    selectedScreen = SelectedScreen.TOMORROW

                    val args = it.toRoute<Destination.Tomorrow>()
                    WebViewScreen(args.url)

                }
                composable<Destination.Settings> {
                    selectedScreen = SelectedScreen.SETTINGS

                    val args = it.toRoute<Destination.Settings>()

                    box()

//                    Column {
//                        Spacer(Modifier.height(32.dp))
//
//                        if (args.noCredentials) {
//                            Text("No credentials found", color = MaterialTheme.colorScheme.error, fontSize = 20.sp,
//                                modifier = Modifier.align(Alignment.CenterHorizontally))
//                            Spacer(Modifier.height(16.dp))
//                        } else {
//                            Text("Settings screen")
//                        }
//                    }
                    SettingsScreen(storage)
                }
            }
        }
    }
}

@Composable
fun box() {
    Box(modifier = Modifier.height(32.dp)
        .background(MaterialTheme.colorScheme.background))
}