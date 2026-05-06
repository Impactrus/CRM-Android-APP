package com.ossadkowski.crm.mobile.ui.serwis.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography tokens for the Serwis module. Ported from `compose/Type.kt`.
 *
 * Per design spec:
 *  - UI text: Inter
 *  - Numerics / timer / SN: JetBrains Mono
 *
 * For this foundation pass we resolve via system defaults
 * ([FontFamily.Default] + [FontFamily.Monospace]) so the module compiles
 * without bundling Downloadable-Fonts certs / res/font xml files. Wiring
 * `androidx.compose.ui.text.googlefonts.GoogleFont` is a follow-up that only
 * needs to swap [InterFamily] / [MonoFamily] below — every token already
 * routes through these two FontFamily instances.
 */
internal val InterFamily: FontFamily = FontFamily.Default
internal val MonoFamily: FontFamily = FontFamily.Monospace

@Immutable
data class CrmTypography(
    /** 36sp / 600 / mono — used for big numerals (timers, summary card). */
    val display: TextStyle,
    /** 22sp / 700 — H1 in hero header. */
    val title: TextStyle,
    /** 18sp / 700 — section headlines. */
    val headline: TextStyle,
    /** 14sp / 700 — card titles, list-row titles when emphasized. */
    val titleMd: TextStyle,
    /** 13.5sp / 500 — body copy / list rows. */
    val body: TextStyle,
    /** 11sp / 700 / uppercase / 0.5 letterSpacing — labels, kickers, pill text. */
    val label: TextStyle,
    /** 10.5sp / 600 / mono — tiny mono (counters, IDs). */
    val caption: TextStyle,
    /** 13sp / 600 / mono — inline mono (SN, time ranges). */
    val mono: TextStyle,
)

fun crmTypography(): CrmTypography = CrmTypography(
    display = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        letterSpacing = (-0.5).sp,
    ),
    title = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.3).sp,
    ),
    headline = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
    ),
    titleMd = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
    ),
    body = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.5.sp,
    ),
    label = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
    ),
    caption = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp,
    ),
    mono = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
    ),
)

internal val LocalCrmType = staticCompositionLocalOf<CrmTypography> {
    error("CrmType not provided. Wrap your Composable in CrmTheme { ... }.")
}
