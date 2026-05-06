package com.ossadkowski.crm.mobile.ui.serwis.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Root theme wrapper for the Serwis (field-service) module.
 *
 * Provides three independent token layers:
 *  - [CrmTheme.colors] — brand + status palette (do NOT use M3 colorScheme for brand)
 *  - [CrmTheme.type]   — Inter + JetBrains Mono typography tokens
 *  - [CrmTheme.dimens] — radii / spacing / component sizes
 *
 * `MaterialTheme` is still set up with a basic `lightColorScheme` mapping so
 * built-in M3 components (Button, Card, Snackbar, ...) pick sensible defaults.
 * For brand-critical surfaces always read [CrmTheme.colors].primary instead of
 * `MaterialTheme.colorScheme.primary` — the design system rule from the export
 * README warns this avoids dynamic-color leaks.
 */
@Composable
fun CrmTheme(
    colors: CrmColorPalette = lightCrmColors(),
    typography: CrmTypography = crmTypography(),
    dimens: CrmDimens = crmDimens(),
    content: @Composable () -> Unit,
) {
    val materialColorScheme = lightColorScheme(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        primaryContainer = colors.brand.bg,
        onPrimaryContainer = colors.brand.text,
        secondary = colors.dark2,
        onSecondary = colors.onDark,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.surface2,
        background = colors.bg,
        onBackground = colors.ink,
        outline = colors.line,
        error = colors.bad.dot,
        onError = colors.onDark,
    )

    val materialTypography = Typography(
        displaySmall = typography.display,
        headlineSmall = typography.title,
        titleLarge = typography.headline,
        titleMedium = typography.titleMd,
        bodyMedium = typography.body,
        labelSmall = typography.label,
    )

    CompositionLocalProvider(
        LocalCrmColors provides colors,
        LocalCrmType provides typography,
        LocalCrmDimens provides dimens,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = materialTypography,
            content = content,
        )
    }
}

/**
 * Token accessor. Read at composition with `CrmTheme.colors.primary`,
 * `CrmTheme.type.title`, `CrmTheme.dimens.radius22`, etc.
 */
object CrmTheme {
    val colors: CrmColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalCrmColors.current

    val type: CrmTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalCrmType.current

    val dimens: CrmDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalCrmDimens.current
}
