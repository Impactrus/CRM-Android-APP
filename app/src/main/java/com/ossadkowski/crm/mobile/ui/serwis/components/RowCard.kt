package com.ossadkowski.crm.mobile.ui.serwis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import com.ossadkowski.crm.mobile.ui.serwis.theme.StatusToken
import com.ossadkowski.crm.mobile.ui.serwis.theme.colors

/**
 * Generic list-row card. Spec: `components/RowCard.md`.
 *
 *  - Surface bg, 1dp [CrmTheme.colors.line] border, 14dp radius
 *  - Padding 12dp (with extra start padding when `accent` is supplied)
 *  - Soft shadow (4dp blur, alpha 0.16)
 *  - Slots: `leading` icon (24dp), `title` (body bold), `meta` (caption),
 *           `trailingValue` + `trailingLabel` kicker
 *  - Optional 4dp left accent bar (priority color) painted via `drawBehind`
 *    so it sits flush against the rounded clip.
 */
@Composable
fun RowCard(
    title: String,
    meta: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    accent: StatusToken? = null,
    iconStatus: StatusToken = StatusToken.BRAND,
    trailingValue: String? = null,
    trailingLabel: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val palette = CrmTheme.colors
    val accentColor = accent?.let { it.colors(palette).dot }
    val iconBg = iconStatus.colors(palette).bg
    val accentLeftDp = CrmTheme.dimens.accentLeft

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(CrmTheme.dimens.radius14),
                ambientColor = palette.ink.copy(alpha = 0.16f),
                spotColor = palette.ink.copy(alpha = 0.16f),
            )
            .clip(RoundedCornerShape(CrmTheme.dimens.radius14))
            .background(palette.surface)
            .border(
                width = CrmTheme.dimens.borderThin,
                color = palette.line,
                shape = RoundedCornerShape(CrmTheme.dimens.radius14),
            )
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .drawBehind {
                accentColor?.let {
                    drawRect(
                        color = it,
                        size = Size(accentLeftDp.toPx(), size.height),
                    )
                }
            }
            .padding(
                start = if (accentColor != null) CrmTheme.dimens.spacing16 else CrmTheme.dimens.spacing12,
                end = CrmTheme.dimens.spacing12,
                top = CrmTheme.dimens.spacing12,
                bottom = CrmTheme.dimens.spacing12,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CrmTheme.dimens.spacing12),
    ) {
        if (leading != null) {
            Box(
                modifier = Modifier
                    .size(CrmTheme.dimens.rowIcon)
                    .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                leading()
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = CrmTheme.type.body.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = palette.ink,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(CrmTheme.dimens.spacing2))
            Text(
                text = meta,
                style = CrmTheme.type.caption.copy(color = palette.muted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (trailingValue != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = trailingValue,
                    style = CrmTheme.type.mono.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.dark2,
                    ),
                )
                if (trailingLabel != null) {
                    Text(
                        text = trailingLabel,
                        style = CrmTheme.type.label.copy(color = palette.muted),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RowCardPreviews() {
    CrmTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(CrmTheme.colors.bg)
                .padding(16.dp),
        ) {
            RowCard(
                title = "Wymiana noża plazmy",
                meta = "ZL/2024/04/001 · 07:30",
                accent = StatusToken.BAD,
                iconStatus = StatusToken.WARN,
                leading = {
                    Icon(
                        imageVector = Icons.Outlined.Build,
                        contentDescription = null,
                        tint = CrmTheme.colors.warn.text,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingValue = "+18 km",
                trailingLabel = "W TRAKCIE",
                onClick = {},
            )
            RowCard(
                title = "Maszyna A-2024-007",
                meta = "1240 mth · ostatni serwis 12.03",
                iconStatus = StatusToken.BRAND,
                leading = {
                    Icon(
                        imageVector = Icons.Outlined.Build,
                        contentDescription = null,
                        tint = CrmTheme.colors.brand.text,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingValue = "1240",
                trailingLabel = "MTH",
            )
        }
    }
}
