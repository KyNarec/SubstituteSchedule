package org.substitute.schedule.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsHeadlineComponent(
    text: String
){
    Spacer(Modifier.height(48.dp))
    Box(Modifier.fillMaxWidth().padding(start = 24.dp)) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
    Spacer(Modifier.height(24.dp))
}