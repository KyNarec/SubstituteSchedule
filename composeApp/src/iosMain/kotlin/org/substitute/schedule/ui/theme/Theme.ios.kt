package org.substitute.schedule.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun getAppColorScheme(
    darkTheme: Boolean,
    useDynamicColor: Boolean
): ColorScheme {
    return if (darkTheme) darkScheme else lightScheme
}

@Composable
actual fun isSystemInDarkTheme(): Boolean {
    return false
}