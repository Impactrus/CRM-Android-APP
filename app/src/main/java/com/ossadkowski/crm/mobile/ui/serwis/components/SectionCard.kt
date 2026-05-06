package com.ossadkowski.crm.mobile.ui.serwis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

/**
 * Generic content card with a header row + divider + body slot.
 *
 * Used for the "CZAS PRACY", "PRZERWY", "KILOMETRÓWKA" sections on screen 13.
 *
 *  - bg [CrmTheme.colors.surface], 1dp [CrmTheme.colors.line] border,
 *    radius 20dp, padding 16dp
 *  - header: optional 18dp icon (primary) + uppercase 12sp/700 label +
 *    spacer + trailing slot (often a [SegmentedToggle] or `+ DODAJ` button)
 *  - 1dp divider beneath the header, then [content]
 */
@Composable
fun SectionCard(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius20))
            .background(palette.surface)
            .border(
                width = dimens.borderThin,
                color = palette.line,
                shape = RoundedCornerShape(dimens.radius20),
            )
            .padding(dimens.spacing16),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(dimens.spacing8))
            }
            Text(
                text = label.uppercase(),
                style = CrmTheme.type.label.copy(color = palette.ink),
            )
            Spacer(Modifier.weight(1f))
            if (trailing != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacing8),
                    verticalAlignment = Alignment.CenterVertically,
                    content = trailing,
                )
            }
        }
        Spacer(Modifier.height(dimens.spacing12))
        HorizontalDivider(color = palette.line, thickness = dimens.borderThin)
        Spacer(Modifier.height(dimens.spacing12))
        content()
    }
}

@Preview
@Composable
private fun SectionCardPreview() {
    CrmTheme {
        Box(
            modifier = Modifier
                .background(CrmTheme.colors.bg)
                .padding(16.dp),
        ) {
            SectionCard(
                label = "Czas pracy",
                icon = Icons.Outlined.Schedule,
                trailing = {
                    SegmentedToggle(
                        options = listOf("Ręczny", "Stoper"),
                        selectedIndex = 0,
                        onSelect = {},
                    )
                },
            ) {
                Text(
                    "Suma czynności: 08:30",
                    style = CrmTheme.type.body,
                )
            }
        }
    }
}
