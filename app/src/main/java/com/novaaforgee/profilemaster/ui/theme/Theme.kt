package com.novaaforgee.profilemaster.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun NovaProfileMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovaColorScheme,
        content = content
    )
}
