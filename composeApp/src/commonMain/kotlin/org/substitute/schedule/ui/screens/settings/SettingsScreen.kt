package org.substitute.schedule.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.substitute.schedule.utils.Destination
import org.substitute.schedule.utils.SecureStorage

@Composable
fun SettingsScreen(
    secureStorage: SecureStorage,
    snackbarHostState: SnackbarHostState,
    noCredentials: Boolean,
    navController: NavHostController,
    modifier: Modifier = Modifier
    ){
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = remember { SnackbarHostState() }
            ) { data ->
                Snackbar(snackbarData = data)
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->

        Column {
//            box()
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Settings", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(32.dp))
            LazyColumn {
                item {
                    SettingFolderComponent(
                        icon = Icons.Default.AccountCircle,
                        title = "Account",
                        description = "Set your account preferences",
                        onClick = {
                            navController.navigate(Destination.AccountSettings(noCredentials))
                        })
                }
                item {
                    SettingFolderComponent(
                        icon = Icons.Default.Edit,
                        title = "UI",
                        description = "Set your ui preferences",
                        onClick = {
                            navController.navigate(Destination.UiSettings)
                        })
                }
            }
        }
    }
}