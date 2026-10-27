package com.agon.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// GeoField palette — field-instrument inspired: deep teal primary, hi-vis amber
// secondary, warm neutral surfaces.
// ---------------------------------------------------------------------------

// Light
val TealPrimary = Color(0xFF0E6E68)
val TealOnPrimary = Color(0xFFFFFFFF)
val TealPrimaryContainer = Color(0xFF9CF2E8)
val TealOnPrimaryContainer = Color(0xFF00201D)

val AmberSecondary = Color(0xFF8A5B00)
val AmberOnSecondary = Color(0xFFFFFFFF)
val AmberSecondaryContainer = Color(0xFFFFDEAC)
val AmberOnSecondaryContainer = Color(0xFF2B1800)

val OliveTertiary = Color(0xFF45664F)
val OliveOnTertiary = Color(0xFFFFFFFF)
val OliveTertiaryContainer = Color(0xFFC8EBCB)
val OliveOnTertiaryContainer = Color(0xFF00210D)

val LightBackground = Color(0xFFF4F7F6)
val LightOnBackground = Color(0xFF161D1C)
val LightSurface = Color(0xFFF4F7F6)
val LightOnSurface = Color(0xFF161D1C)
val LightSurfaceVariant = Color(0xFFDAE5E2)
val LightOnSurfaceVariant = Color(0xFF3F4947)
val LightSurfaceContainer = Color(0xFFE8EEEC)
val LightSurfaceContainerHigh = Color(0xFFE2EAE7)
val LightOutline = Color(0xFF6F7977)
val LightOutlineVariant = Color(0xFFBEC9C6)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

// Dark
val TealPrimaryDark = Color(0xFF80D6CB)
val TealOnPrimaryDark = Color(0xFF003734)
val TealPrimaryContainerDark = Color(0xFF00504C)
val TealOnPrimaryContainerDark = Color(0xFF9CF2E8)

val AmberSecondaryDark = Color(0xFFFFB950)
val AmberOnSecondaryDark = Color(0xFF492900)
val AmberSecondaryContainerDark = Color(0xFF693F00)
val AmberOnSecondaryContainerDark = Color(0xFFFFDEAC)

val OliveTertiaryDark = Color(0xFFAECFAC)
val OliveOnTertiaryDark = Color(0xFF163723)
val OliveTertiaryContainerDark = Color(0xFF2D4E37)
val OliveOnTertiaryContainerDark = Color(0xFFC8EBCB)

val DarkBackground = Color(0xFF0D1413)
val DarkOnBackground = Color(0xFFDDE4E1)
val DarkSurface = Color(0xFF0D1413)
val DarkOnSurface = Color(0xFFDDE4E1)
val DarkSurfaceVariant = Color(0xFF3F4947)
val DarkOnSurfaceVariant = Color(0xFFBEC9C6)
val DarkSurfaceContainer = Color(0xFF192220)
val DarkSurfaceContainerHigh = Color(0xFF232C2A)
val DarkOutline = Color(0xFF899390)
val DarkOutlineVariant = Color(0xFF3F4947)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

/**
 * Per-category accent colours used by the point list, charts and badges.
 */
data class CategoryPalette(
    val corner: Color,
    val traverse: Color,
    val benchmark: Color,
    val feature: Color,
    val observation: Color,
)

val LightCategoryPalette = CategoryPalette(
    corner = Color(0xFF0E6E68),
    traverse = Color(0xFF8A5B00),
    benchmark = Color(0xFF3D5AFE),
    feature = Color(0xFFB3261E),
    observation = Color(0xFF45664F),
)

val DarkCategoryPalette = CategoryPalette(
    corner = Color(0xFF80D6CB),
    traverse = Color(0xFFFFB950),
    benchmark = Color(0xFF8C9EFF),
    feature = Color(0xFFFFB4AB),
    observation = Color(0xFFAECFAC),
)
