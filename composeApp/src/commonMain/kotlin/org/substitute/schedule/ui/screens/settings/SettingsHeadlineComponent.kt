package org.substitute.schedule.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import org.substitute.schedule.utils.Destination

@Composable
fun SettingsHeadlineComponent(
    text: String,
    navController: NavHostController,
){
    if (navController.currentDestination?.route?.startsWith(Destination.Settings::class.qualifiedName!!) != true)
    {
        println("SettingsHeadlineComponent: Settings")
        Spacer(Modifier.height(48.dp))
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(text, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.CenterVertically))
        }
        Spacer(Modifier.height(24.dp))
    } else {
        Spacer(Modifier.height(48.dp))
        Box(Modifier.fillMaxWidth().padding(start = 24.dp)) {
            Text(text, style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(24.dp))
    }
}