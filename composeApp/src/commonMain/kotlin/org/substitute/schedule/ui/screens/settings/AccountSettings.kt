package org.substitute.schedule.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.substitute.schedule.utils.Constants.PASSWORD
import org.substitute.schedule.utils.Constants.USERNAME
import org.substitute.schedule.utils.SecureStorage


@Composable
fun AccountSettings(
    secureStorage: SecureStorage,
    noCredentials: Boolean,
    snackbarHostState: SnackbarHostState,
    ) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    // Load saved credentials (if any)
    LaunchedEffect(Unit) {
        username = secureStorage.getString(USERNAME) ?: ""
        password = secureStorage.getString(PASSWORD) ?: ""

//        println("SettingsScreen: Password: $password")
//        println("SettingsScreen: Username: $username")

    }
    Box(Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
//                .padding(16.dp)
                .fillMaxSize()
//                .padding(24.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {

//            Text(
//                "Settings",
//                style = MaterialTheme.typography.headlineMedium,
//                modifier = Modifier.padding(bottom = 32.dp)
//            )
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Account", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(32.dp))

            if (noCredentials) {
                Text(
                    "No credentials found",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)

                )
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,  // Can be email in the future
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp, 16.dp, 16.dp, 8.dp)
                    .semantics { contentType = ContentType.Username }

            )


            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 8.dp)
                    .semantics { contentType = ContentType.Password }

            )

            Row(
                Modifier.padding(16.dp),
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
                Box(Modifier.padding(16.dp).align(Alignment.CenterHorizontally)) {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}