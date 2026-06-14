package com.ossadkowski.crm.mobile.ui.nawozy.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.domain.nawozy.model.LimitStatus

/** Severity of a customer's credit-limit state, ordered least→most restrictive. */
private enum class LimitLevel(val label: String, val color: Color) {
    OK("Limit OK", Color(0xFF059669)),
    WARNING("Ostrzeżenie — limit wyczerpany", Color(0xFFD97706)),
    FROZEN("Limit zamrożony", Color(0xFFEA580C)),
    BLOCKED("Kontrahent zablokowany", Color(0xFFDC2626)),
}

private fun LimitStatus.level(): LimitLevel = when {
    isBlocked -> LimitLevel.BLOCKED
    isFrozen -> LimitLevel.FROZEN
    (dostepne ?: Double.MAX_VALUE) <= 0.0 -> LimitLevel.WARNING
    else -> LimitLevel.OK
}

/**
 * Colour-coded credit-limit banner shown on the customer picker and the cart.
 * Restricted states (frozen/blocked) are what gate the submit risk-acknowledgement.
 */
@Composable
fun LimitBanner(status: LimitStatus, modifier: Modifier = Modifier) {
    val level = status.level()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = level.color.copy(alpha = 0.12f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (level == LimitLevel.OK) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = level.color,
            )
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = level.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = level.color,
                    fontWeight = FontWeight.SemiBold,
                )
                val details = buildString {
                    if (status.dostepne != null) append("Dostępne: ${formatPln(status.dostepne)}")
                    if (status.limitMax != null) {
                        if (isNotEmpty()) append(" / ")
                        append("Limit: ${formatPln(status.limitMax)}")
                    }
                }
                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                status.frozenReason?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = level.color,
                    )
                }
            }
        }
    }
}
