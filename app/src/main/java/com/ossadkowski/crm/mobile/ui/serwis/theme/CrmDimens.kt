package com.ossadkowski.crm.mobile.ui.serwis.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing, radius, and component-size tokens for the Serwis module.
 * Ported 1:1 from `compose/Dimens.kt`.
 */
@Immutable
data class CrmDimens(
    // Spacing
    val spacing2: Dp,
    val spacing4: Dp,
    val spacing6: Dp,
    val spacing8: Dp,
    val spacing10: Dp,
    val spacing12: Dp,
    val spacing14: Dp,
    val spacing16: Dp,
    val spacing18: Dp,
    val spacing20: Dp,
    val spacing24: Dp,
    val spacing32: Dp,

    // Radii
    val radius8: Dp,
    val radius10: Dp,
    val radius14: Dp,
    val radius17: Dp,
    val radius18: Dp,
    val radius20: Dp,
    val radius22: Dp,
    val radius30: Dp,

    // Component sizes
    val touchMin: Dp,
    val topbarHeight: Dp,
    val statusBarPad: Dp,
    val ctaHeight: Dp,
    val bottomCta: Dp,
    val fab: Dp,
    val hamSize: Dp,
    val rowIcon: Dp,
    val machinePic: Dp,

    // Borders
    val borderThin: Dp,
    val borderMed: Dp,
    val accentLeft: Dp,
)

fun crmDimens(): CrmDimens = CrmDimens(
    spacing2 = 2.dp,
    spacing4 = 4.dp,
    spacing6 = 6.dp,
    spacing8 = 8.dp,
    spacing10 = 10.dp,
    spacing12 = 12.dp,
    spacing14 = 14.dp,
    spacing16 = 16.dp,
    spacing18 = 18.dp,
    spacing20 = 20.dp,
    spacing24 = 24.dp,
    spacing32 = 32.dp,

    radius8 = 8.dp,
    radius10 = 10.dp,
    radius14 = 14.dp,
    radius17 = 17.dp,
    radius18 = 18.dp,
    radius20 = 20.dp,
    radius22 = 22.dp,
    radius30 = 30.dp,

    touchMin = 44.dp,
    topbarHeight = 54.dp,
    statusBarPad = 24.dp,
    ctaHeight = 48.dp,
    bottomCta = 56.dp,
    fab = 52.dp,
    hamSize = 38.dp,
    rowIcon = 38.dp,
    machinePic = 50.dp,

    borderThin = 1.dp,
    borderMed = 1.5.dp,
    accentLeft = 4.dp,
)

internal val LocalCrmDimens = staticCompositionLocalOf<CrmDimens> {
    error("CrmDimens not provided. Wrap your Composable in CrmTheme { ... }.")
}
