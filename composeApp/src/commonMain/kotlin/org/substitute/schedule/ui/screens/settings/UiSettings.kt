package org.substitute.schedule.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.substitute.schedule.utils.Constants.DYNAMICCOLORS
import org.substitute.schedule.utils.Constants.NAVBARTEXT
import org.substitute.schedule.utils.SecureStorage



@Composable
fun UiSettings(
    secureStorage: SecureStorage
) {
    Box(Modifier.fillMaxSize()) {
        Column {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("UI", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(32.dp))

            SettingComponent(
                icon = Icons.Default.TextFields,
                title = "Text in navigation bar",
                description = "Destination as text under the icon",
                secureStorage = secureStorage,
                switchId = NAVBARTEXT
            )

            SettingComponent(
                icon = Icons.Default.FormatColorFill,
                title = "Dynamic colors",
                description = "Use dynamic android native colors",
                secureStorage = secureStorage,
                switchId = DYNAMICCOLORS
            )
        }
    }
}