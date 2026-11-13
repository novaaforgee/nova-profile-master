package com.novaaforgee.profilemaster.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val BgDark = Color(0xFF090D16)
val SurfaceDark = Color(0xFF131B2E)
val CardDark = Color(0xFF1E293B)
val BorderDark = Color(0xFF334155)
val AccentCyan = Color(0xFF38BDF8)
val AccentIndigo = Color(0xFF6366F1)
val SuccessGreen = Color(0xFF22C55E)
val TextLight = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)

val NovaColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BgDark,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = AccentCyan,
    secondary = AccentIndigo,
    background = BgDark,
    surface = SurfaceDark,
    onBackground = TextLight,
    onSurface = TextLight
)
