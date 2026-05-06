package com.ossadkowski.crm.mobile.ui.serwis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

/**
 * Segmented toggle (radio-style) — used as the trailing element of section
 * headers (e.g. "RĘCZNY | STOPER" on screen 13).
 *
 * Spec port: snippet in `screens/13-nowy-wpis-czasu.md`.
 *  - container bg [CrmTheme.colors.surface100], radius 10dp, padding 3dp
 *  - selected segment: bg [CrmTheme.colors.surface], radius 7dp, padding 14h/7v
 *  - unselected segment: transparent
 *  - label 11sp / 700 / 0.5 letterSpacing (= [CrmTheme.type.label])
 */
@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = CrmTheme.colors

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
            .background(palette.surface100)
            .padding(3.dp),
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isSelected) palette.surface else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label.uppercase(),
                    style = CrmTheme.type.label.copy(
                        color = if (isSelected) palette.ink else palette.muted,
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SegmentedTogglePreview() {
    CrmTheme {
        Box(
            modifier = Modifier
                .background(CrmTheme.colors.bg)
                .padding(16.dp),
        ) {
            SegmentedToggle(
                options = listOf("Ręczny", "Stoper"),
                selectedIndex = 0,
                onSelect = {},
            )
        }
    }
}
