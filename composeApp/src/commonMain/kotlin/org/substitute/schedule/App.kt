package org.substitute.schedule

import androidx.compose.foundation.background
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
import org.substitute.schedule.networking.DsbApiClient
import org.substitute.schedule.networking.TimeTable
import org.substitute.schedule.ui.screens.WebViewScreen
import org.substitute.schedule.ui.theme.AppTheme
import org.substitute.schedule.utils.Destination
import org.substitute.schedule.utils.enums.SelectedScreen

@Composable
fun App(
    client: DsbApiClient
) {
    AppTheme() {
        var selectedScreen by remember { mutableStateOf(SelectedScreen.TODAY) }
        var distinctTables by remember { mutableStateOf<List<TimeTable>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            try {
                val tables = client.getTimeTables()
                distinctTables = tables.distinctBy { it.uuid }
                println("Distinct Tables: $distinctTables")
                isLoading = false
            } catch (e: Exception) {
                println("Error loading timetables: ${e.message}")
                isLoading = false
            }
        }

        // Show loading state until data is ready
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,

            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Loading timetables...", color = MaterialTheme.colorScheme.onBackground)
                }
            }
            return@AppTheme
        }


        val startDestination = if (distinctTables.isNotEmpty()) {
            Destination.Today(distinctTables[0].detail)
        } else {
            Destination.Settings(true)
        }

        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedScreen == SelectedScreen.TODAY,
                        onClick = {
                            if (distinctTables.size > 1 && startDestination != Destination.Settings) {
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
                            if (distinctTables.size > 1 && startDestination != Destination.Settings) {
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
                startDestination = startDestination
            ) {

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



                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (args.noCredentials) {
                            Text("No credentials found", color = MaterialTheme.colorScheme.error, fontSize = 20.sp)
                            Spacer(Modifier.height(16.dp))
                        } else {
                            Text("Settings screen")
                        }
                    }
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