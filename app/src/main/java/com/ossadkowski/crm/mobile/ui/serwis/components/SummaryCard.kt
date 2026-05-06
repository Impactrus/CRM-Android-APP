package com.ossadkowski.crm.mobile.ui.serwis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

/**
 * Brand-green summary card with two big numerical fields.
 *
 * Spec: snippet in `screens/13-nowy-wpis-czasu.md`.
 *  - bg [CrmTheme.colors.primary], radius 22dp, padding 18dp
 *  - left column: small label + big mono "08:30" 36sp/700
 *  - right column: small label + big "124 km" 26sp/700
 *  - both texts use [CrmTheme.colors.onPrimary] (#1B2C0E)
 *  - labels are 70% alpha
 */
@Composable
fun SummaryCard(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    modifier: Modifier = Modifier,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    val labelColor = palette.onPrimary.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius22))
            .background(palette.primary)
            .padding(dimens.spacing18),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = leftLabel.uppercase(),
                    style = CrmTheme.type.label.copy(color = labelColor),
                )
                Spacer(Modifier.height(dimens.spacing4))
                Text(
                    text = leftValue,
                    style = CrmTheme.type.display.copy(
                        color = palette.onPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                    ),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = rightLabel.uppercase(),
                    style = CrmTheme.type.label.copy(color = labelColor),
                )
                Spacer(Modifier.height(dimens.spacing4))
                Text(
                    text = rightValue,
                    style = CrmTheme.type.display.copy(
                        color = palette.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SummaryCardPreview() {
    CrmTheme {
        Box(
            modifier = Modifier
                .background(CrmTheme.colors.bg)
                .padding(16.dp),
        ) {
            SummaryCard(
                leftLabel = "Czas pracy (netto)",
                leftValue = "08:30",
                rightLabel = "Suma km",
                rightValue = "124 km",
            )
        }
    }
}
