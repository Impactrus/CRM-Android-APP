package com.ossadkowski.crm.mobile.ui.serwis.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Brand + status palette for the Serwis (field-service) module.
 *
 * Ported 1:1 from the design export `compose/Color.kt`.
 * Kept independent of `MaterialTheme.colorScheme` per the design system rule:
 * the brand lime conflicts with M3 dynamic-color, so screens MUST read
 * [CrmTheme.colors] rather than `MaterialTheme.colorScheme.primary`.
 */
@Immutable
data class StatusColors(
    val bg: Color,
    val text: Color,
    val dot: Color,
)

@Immutable
data class CrmColorPalette(
    // Brand
    val primary: Color,
    val primaryDeep: Color,
    val onPrimary: Color,

    // Dark / hero
    val dark1: Color,
    val dark2: Color,
    val dark3: Color,

    // Neutrals
    val ink: Color,
    val muted: Color,
    val line: Color,
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val surface100: Color,
    val cardBorder: Color,

    // On dark
    val onDark: Color,
    val onDarkMuted: Color,
    val onDarkSubtle: Color,
    val onDarkGlass: Color,

    // Status palettes
    val bad: StatusColors,
    val warn: StatusColors,
    val ok: StatusColors,
    val info: StatusColors,
    val brand: StatusColors,
    val neutral: StatusColors,
)

fun lightCrmColors(): CrmColorPalette = CrmColorPalette(
    primary = Color(0xFF97C11E),
    primaryDeep = Color(0xFF87B01A),
    onPrimary = Color(0xFF1B2C0E),

    dark1 = Color(0xFF264433),
    dark2 = Color(0xFF1B3023),
    dark3 = Color(0xFF14241A),

    ink = Color(0xFF0F1A12),
    muted = Color(0xFF6B7A6E),
    line = Color(0xFFE5E8E1),
    bg = Color(0xFFF4F2EC),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFFAF9F4),
    surface100 = Color(0xFFF1EEE5),
    cardBorder = Color(0xFFEDE9DC),

    onDark = Color(0xFFFFFFFF),
    onDarkMuted = Color(0xFFA8C8B0),
    onDarkSubtle = Color(0xFFC2D6C7),
    onDarkGlass = Color(0x1AFFFFFF),

    bad = StatusColors(
        bg = Color(0xFFFDE8E8),
        text = Color(0xFF9B1C1C),
        dot = Color(0xFFF05252),
    ),
    warn = StatusColors(
        bg = Color(0xFFFEF3C7),
        text = Color(0xFF92400E),
        dot = Color(0xFFF59E0B),
    ),
    ok = StatusColors(
        bg = Color(0xFFDEF7EC),
        text = Color(0xFF03543F),
        dot = Color(0xFF0E9F6E),
    ),
    info = StatusColors(
        bg = Color(0xFFEBF5FF),
        text = Color(0xFF1E40AF),
        dot = Color(0xFF3F83F8),
    ),
    brand = StatusColors(
        bg = Color(0xFFE7F0CC),
        text = Color(0xFF3D5410),
        dot = Color(0xFF97C11E),
    ),
    neutral = StatusColors(
        bg = Color(0xFFF3F4F6),
        text = Color(0xFF374151),
        dot = Color(0xFF6B7280),
    ),
)

/**
 * Status enum used by domain layer; mirrors `taskStatusConfig` from the desktop CRM-OC.
 *
 * BAD   → SLA breach, P1, recall priority 1
 * WARN  → in-progress, recall priority 2-3
 * OK    → done, warranty active, delivered
 * INFO  → new, scheduled, neutral
 * BRAND → brand accent (rare — CTA / selected)
 */
enum class StatusToken { BAD, WARN, OK, INFO, BRAND, NEUTRAL }

fun StatusToken.colors(palette: CrmColorPalette): StatusColors = when (this) {
    StatusToken.BAD -> palette.bad
    StatusToken.WARN -> palette.warn
    StatusToken.OK -> palette.ok
    StatusToken.INFO -> palette.info
    StatusToken.BRAND -> palette.brand
    StatusToken.NEUTRAL -> palette.neutral
}

internal val LocalCrmColors = staticCompositionLocalOf<CrmColorPalette> {
    error("CrmColors not provided. Wrap your Composable in CrmTheme { ... }.")
}
