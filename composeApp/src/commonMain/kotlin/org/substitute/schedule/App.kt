package org.substitute.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.substitute.schedule.networking.DsbApiClient
import org.substitute.schedule.networking.TimeTable
import org.substitute.schedule.ui.UpdateDialog
import org.substitute.schedule.ui.screens.LoadingTimeTables
import org.substitute.schedule.ui.screens.settings.SettingsScreen
import org.substitute.schedule.ui.screens.WebViewScreen
import org.substitute.schedule.ui.screens.settings.AccountSettings
import org.substitute.schedule.ui.screens.settings.UiSettings
import org.substitute.schedule.ui.theme.AppTheme
import org.substitute.schedule.update.PlatformUpdateManager
import org.substitute.schedule.update.UpdateViewModel
import org.substitute.schedule.utils.Constants.PASSWORD
import org.substitute.schedule.utils.Constants.USERNAME
import org.substitute.schedule.utils.Destination
import org.substitute.schedule.utils.SecureStorage
import org.substitute.schedule.utils.enums.SelectedScreen
import org.substitute.schedule.utils.Constants.DYNAMICCOLORS
import org.substitute.schedule.utils.Constants.NAVBARTEXT


@Composable
fun App(
    client: DsbApiClient,
    storage: SecureStorage
) {
    val dynamicColors by storage.observeBoolean(DYNAMICCOLORS)
        .collectAsState(initial = storage.getBoolean(DYNAMICCOLORS))
    AppTheme(
        dynamicColor = dynamicColors
    ) {
        var selectedScreen by remember { mutableStateOf(SelectedScreen.TODAY) }
        var distinctTables by remember { mutableStateOf<List<TimeTable>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var loadAttemptFailed by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        val updateManager = remember { PlatformUpdateManager() }
        val viewModel = remember { UpdateViewModel(updateManager) }
        val navBarText by storage.observeBoolean(NAVBARTEXT)
            .collectAsState(initial = storage.getBoolean(NAVBARTEXT))
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            viewModel.checkForUpdates()

            client.username = storage.getString(USERNAME).toString()
            client.password = storage.getString(PASSWORD).toString()

//            println("App(): Username: ${client.username}")
//            println("App(): Password: ${client.password}")

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
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
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
                        },
                        label = {
                            if (navBarText) {
                                Text(
                                    "Today",
                                    fontSize = 12.sp,
                                    lineHeight = 12.sp
                                )
                            }
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
                        },
                        label = {
                            if (navBarText) {
                                Text(
                                    "Tomorrow",
                                    fontSize = 12.sp,
                                    lineHeight = 12.sp
                                )
                            }
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
                        },
                        label = {
                            if (navBarText) {
                                Text(
                                    "Settings",
                                    fontSize = 12.sp,
                                    lineHeight = 12.sp
                                )
                            }
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

                    if (viewModel.showDialog && viewModel.updateInfo != null) {
                        UpdateDialog(
                            updateInfo = viewModel.updateInfo!!,
                            downloadStatus = viewModel.downloadStatus,
                            supportsInAppInstallation = updateManager.supportsInAppInstallation(),
                            onDismiss = { viewModel.dismissDialog() },
                            onUpdate = { viewModel.startDownload() },
                            onOpenStore = { viewModel.openStore() }
                        )
                    } else {

                        LaunchedEffect(isLoading) {

                            if (!isLoading) {

                                if (client.username.isNotEmpty() && !loadAttemptFailed) {
                                    val url = distinctTables.first().detail

                                    // Use popBackStack/replace to prevent the loading route from being on the back stack
                                    navController.popBackStack()
                                    navController.navigate(Destination.Today(url))
                                } else {
                                    navController.popBackStack()
                                    navController.navigate(Destination.AccountSettings(true))
                                }
                            }
                        }

                        LoadingTimeTables()
                    }
                }

                composable<Destination.Today> {
                    selectedScreen = SelectedScreen.TODAY

                    val args = it.toRoute<Destination.Today>()
                    Box(Modifier
                        .fillMaxSize()
                        .padding(contentPadding))
                    {
                        WebViewScreen(args.url)
                    }
                }

                composable<Destination.Tomorrow> {
                    selectedScreen = SelectedScreen.TOMORROW

                    val args = it.toRoute<Destination.Tomorrow>()
                    Box(Modifier
                        .fillMaxSize()
                        .padding(contentPadding))
                    {
                        WebViewScreen(args.url)
                    }

                }
                composable<Destination.Settings> {
                    selectedScreen = SelectedScreen.SETTINGS

                    val args = it.toRoute<Destination.Settings>()

                    SettingsScreen(storage, snackbarHostState, args.noCredentials, navController)
                }
                composable<Destination.AccountSettings> {
                    selectedScreen = SelectedScreen.SETTINGS

                    val args = it.toRoute<Destination.AccountSettings>()
                    AccountSettings(storage, args.noCredentials, snackbarHostState)
                }

                composable<Destination.UiSettings> {
                    selectedScreen = SelectedScreen.SETTINGS
                    UiSettings(storage)
                }
            }
        }
    }
}