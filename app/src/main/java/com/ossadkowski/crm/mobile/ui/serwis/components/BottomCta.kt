package com.ossadkowski.crm.mobile.ui.serwis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

/**
 * Sticky bottom CTA (single primary action). Spec: `components/BottomCta.md`.
 *
 *  - Container: gradient scrim from `transparent` -> `bg` (80dp tall) so list
 *    content fades under the button rather than hard-clipping.
 *  - Button: 56dp height, radius 17dp, primary fill, label is uppercase
 *    [CrmTheme.type.label] tinted with [CrmTheme.colors.onPrimary].
 *  - Disabled: alpha 0.5 + click ignored.
 *  - This is NOT a Material `BottomBar`. It expects to live inside a
 *    `Scaffold(bottomBar = ...)` slot or a `Box` overlay with
 *    `Modifier.align(Alignment.BottomCenter)`.
 *  - Respects `WindowInsets.navigationBars`.
 */
@Composable
fun BottomCta(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.4f to palette.bg,
                    1f to palette.bg,
                ),
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                start = dimens.spacing16,
                end = dimens.spacing16,
                top = dimens.spacing24,
                bottom = dimens.spacing16,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.bottomCta)
                .clip(RoundedCornerShape(dimens.radius17))
                .alpha(if (enabled) 1f else 0.5f)
                .background(palette.primary)
                .let { if (enabled) it.clickable(onClick = onClick) else it },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(dimens.spacing8))
            }
            Text(
                text = label.uppercase(),
                style = CrmTheme.type.label.copy(color = palette.onPrimary),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun BottomCtaPreview() {
    CrmTheme {
        Box(
            modifier = Modifier
                .background(CrmTheme.colors.bg)
                .fillMaxWidth(),
        ) {
            BottomCta(
                label = "Zapisz Kartę Pracy",
                icon = Icons.Outlined.Save,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun BottomCtaDisabledPreview() {
    CrmTheme {
        Box(
            modifier = Modifier
                .background(CrmTheme.colors.bg)
                .fillMaxWidth(),
        ) {
            BottomCta(
                label = "Zapisz Kartę Pracy",
                icon = Icons.Outlined.Save,
                enabled = false,
                onClick = {},
            )
        }
    }
}
