package com.ossadkowski.crm.mobile.ui.serwis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

/**
 * Dark-green gradient header used on Login / Mój dzień / Maszyna 360 / Profil.
 *
 * Spec: `components/HeroHeader.md`.
 *  - vertical gradient `dark1 -> dark2` (export uses 3-stop incl. dark3; we
 *    keep a 2-stop primary spec as instructed and let `extra` slot host any
 *    nested overlays the screen needs)
 *  - bottom rounded corners (30dp) — header bleeds into content
 *  - inner padding: top = statusBarPad + 16dp, bottom = 24dp, horizontal = 20dp
 *
 * Slots:
 *  - [topBar] — back/menu + title + actions (rendered inside the gradient)
 *  - [dayStamp] — small uppercase label rendered above the H1
 *  - [title] — H1 string ([CrmTheme.type.title] in white)
 *  - [content] — extra blocks below H1 (subtitle, stat-pads, tags row...)
 */
@Composable
fun HeroHeader(
    title: String,
    modifier: Modifier = Modifier,
    dayStamp: String? = null,
    topBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = dimens.radius30,
                    bottomEnd = dimens.radius30,
                ),
            )
            .background(
                Brush.verticalGradient(
                    0f to palette.dark1,
                    1f to palette.dark2,
                ),
            )
            .padding(horizontal = dimens.spacing20)
            .padding(
                top = dimens.statusBarPad + dimens.spacing16,
                bottom = dimens.spacing24,
            ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (topBar != null) {
                topBar()
                Spacer(Modifier.height(dimens.spacing12))
            }
            if (dayStamp != null) {
                Text(
                    text = dayStamp.uppercase(),
                    style = CrmTheme.type.label.copy(color = palette.onDarkMuted),
                )
                Spacer(Modifier.height(dimens.spacing4))
            }
            Text(
                text = title,
                style = CrmTheme.type.title.copy(
                    color = palette.onDark,
                    fontWeight = FontWeight.Bold,
                ),
            )
            content()
        }
    }
}

@Preview
@Composable
private fun HeroHeaderPreview() {
    CrmTheme {
        HeroHeader(
            title = "Dziś masz 5 zleceń",
            dayStamp = "Wtorek · 6 maja",
            topBar = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "MENU",
                        style = CrmTheme.type.label.copy(color = CrmTheme.colors.onDark),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "SERWIS",
                        style = CrmTheme.type.label.copy(color = CrmTheme.colors.onDarkMuted),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "🔔",
                        style = CrmTheme.type.label.copy(color = CrmTheme.colors.onDark),
                    )
                }
            },
            content = {
                Spacer(Modifier.height(CrmTheme.dimens.spacing8))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CrmTheme.dimens.spacing8),
                ) {
                    StatusPill(
                        token = com.ossadkowski.crm.mobile.ui.serwis.theme.StatusToken.BRAND,
                        text = "Aktywne",
                        showDot = true,
                    )
                }
            },
        )
    }
}
