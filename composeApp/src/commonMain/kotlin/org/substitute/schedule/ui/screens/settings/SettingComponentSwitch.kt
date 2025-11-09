package org.substitute.schedule.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
fun SettingComponentSwitch(
    icon: ImageVector,
    title: String,
    description: String,
    secureStorage: SecureStorage,
    switchId: String
    ) {
    var checked by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        checked = secureStorage.getBoolean(switchId)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterVertically)
                .padding(8.dp)
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(top = 8.dp, bottom = 8.dp, end = 8.dp)
        ) {
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    secureStorage.putBoolean(switchId, it)
                }
            )
        }
    }
}
