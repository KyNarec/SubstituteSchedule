package org.substitute.schedule.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.substitute.schedule.utils.Constants.PASSWORD
import org.substitute.schedule.utils.Constants.USERNAME
import org.substitute.schedule.utils.SecureStorage

@Composable
fun SettingsScreen(
    secureStorage: SecureStorage,
    snackbarHostState: SnackbarHostState,
    noCredentials: Boolean,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    // Load saved credentials (if any)
    LaunchedEffect(Unit) {
        username = secureStorage.getString(USERNAME) ?: ""
        password = secureStorage.getString(PASSWORD) ?: ""

        println("SettingsScreen: Password: $password")
        println("SettingsScreen: Username: $username")

    }


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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (noCredentials) {
                Text(
                    "No credentials found",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)

                )
            }
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            secureStorage.putString(USERNAME, username)
                            secureStorage.putString(PASSWORD, password)
                            message = "Credentials saved"
                            snackbarHostState.showSnackbar("To apply changes, please restart the app")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            secureStorage.remove(PASSWORD)
                            secureStorage.remove(USERNAME)
                            username = ""
                            password = ""
                            message = "Credentials cleared"
                            snackbarHostState.showSnackbar("To apply changes, please restart the app")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            message?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}