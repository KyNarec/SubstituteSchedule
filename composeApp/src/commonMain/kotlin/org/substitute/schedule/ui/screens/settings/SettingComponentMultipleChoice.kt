package org.substitute.schedule.ui.screens.settings
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.substitute.schedule.utils.SecureStorage

@Composable
fun <T : Enum<T>> SettingComponentEnumChoice(
    icon: ImageVector,
    title: String,
    description: String,
    secureStorage: SecureStorage,
    key: String,
    enumValues: List<T>,
    default: T,
    labelMapper: (T) -> String = { it.name }
) {
    var selected by remember { mutableStateOf(default) }
    var showDialog by remember { mutableStateOf(false) }

    // Load stored value
    LaunchedEffect(Unit) {
        val stored = secureStorage.getString(key)
        selected = stored?.let { name ->
            enumValues.firstOrNull { it.name == name } ?: default
        } ?: default
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(48.dp).padding(8.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            //Text(labelMapper(selected), style = MaterialTheme.typography.bodyMedium)
        }
    }

    // M3 Dialog Popup
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    enumValues.forEach { value ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selected = value
                                    secureStorage.putString(key, value.name)
                                    showDialog = false
                                }
                        ) {
                            RadioButton(
                                selected = selected == value,
                                onClick = {
                                    selected = value
                                    secureStorage.putString(key, value.name)
                                    showDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(labelMapper(value))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}