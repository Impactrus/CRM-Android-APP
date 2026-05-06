package com.ossadkowski.crm.mobile.ui.serwis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import com.ossadkowski.crm.mobile.ui.serwis.theme.StatusToken
import com.ossadkowski.crm.mobile.ui.serwis.theme.colors

/**
 * Status pill — colored chip with optional leading dot.
 *
 * Spec ref: `components/StatusPill.md`.
 *  - radius 999dp (pill)
 *  - padding 9dp horizontal × 4dp vertical (per export Compose draft)
 *  - dot: 5dp circle, color = `status.dot`
 *  - label: 11sp / 700 / 0.5 letterSpacing (= [CrmTheme.type.label])
 *
 * Variants: BAD / WARN / OK / INFO / BRAND / NEUTRAL.
 */
@Composable
fun StatusPill(
    token: StatusToken,
    text: String,
    modifier: Modifier = Modifier,
    showDot: Boolean = true,
) {
    val palette = CrmTheme.colors
    val sc = token.colors(palette)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(sc.bg)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (showDot) {
            Box(
                Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(sc.dot),
            )
        }
        Text(
            text = text,
            style = CrmTheme.type.label.copy(color = sc.text),
        )
    }
}

@Preview
@Composable
private fun StatusPillPreviews() {
    CrmTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            StatusPill(StatusToken.BAD, "Pilne")
            StatusPill(StatusToken.WARN, "W trakcie")
            StatusPill(StatusToken.OK, "Zakończ.")
            StatusPill(StatusToken.INFO, "Nowe")
            StatusPill(StatusToken.BRAND, "Akcent", showDot = false)
        }
    }
}
