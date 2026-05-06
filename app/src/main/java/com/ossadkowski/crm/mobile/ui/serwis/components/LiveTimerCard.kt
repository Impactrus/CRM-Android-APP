package com.ossadkowski.crm.mobile.ui.serwis.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import com.ossadkowski.crm.mobile.ui.serwis.theme.StatusToken

/**
 * State of the live timer. Per spec on screen 13:
 *
 *   IDLE ──Start──▶ RUNNING ──Pauza──▶ PAUSED ──Wznów──▶ RUNNING
 *                     │                    │
 *                     └────────Stop────────┴────────▶ DONE
 */
enum class LiveTimerState { IDLE, RUNNING, PAUSED, DONE }

/**
 * Dark live-timer card. Spec: `components/LiveTimer.md`.
 *
 *  - bg `#0F1A12` ([CrmTheme.colors.ink]), radius 22dp, padding 18dp
 *  - top row: status pill ("LIVE" with pulsing dot when [LiveTimerState.RUNNING]) + meta
 *  - center: mono `HH:MM:SS` 36sp/700/white
 *  - bottom: 3 ghost buttons [Pauza | Przerwa | Stop], each 44dp tall
 *  - pulse animation runs only while RUNNING
 */
@Composable
fun LiveTimerCard(
    state: LiveTimerState,
    elapsedHhMmSs: String,
    stepLabel: String,
    onPause: () -> Unit,
    onBreak: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    meta: String? = null,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius22))
            .background(palette.ink)
            .padding(dimens.spacing18),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacing8),
            ) {
                LivePill(running = state == LiveTimerState.RUNNING)
                if (meta != null) {
                    Text(
                        text = meta.uppercase(),
                        style = CrmTheme.type.label.copy(color = palette.onDarkMuted),
                    )
                }
            }
            Spacer(Modifier.height(dimens.spacing8))
            Text(
                text = elapsedHhMmSs,
                style = CrmTheme.type.display.copy(
                    color = palette.onDark,
                    fontWeight = FontWeight.Bold,
                ),
            )
            if (stepLabel.isNotEmpty()) {
                Spacer(Modifier.height(dimens.spacing4))
                Text(
                    text = stepLabel,
                    style = CrmTheme.type.body.copy(color = palette.onDarkSubtle),
                )
            }
            Spacer(Modifier.height(dimens.spacing12))
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing6)) {
                GhostTimerBtn(
                    label = if (state == LiveTimerState.PAUSED) "Wznów" else "Pauza",
                    icon = if (state == LiveTimerState.PAUSED) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                    onClick = onPause,
                    modifier = Modifier.weight(1f),
                )
                GhostTimerBtn(
                    label = "Przerwa",
                    icon = Icons.Outlined.Coffee,
                    onClick = onBreak,
                    modifier = Modifier.weight(1f),
                )
                GhostTimerBtn(
                    label = "Stop",
                    icon = Icons.Outlined.Stop,
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    accent = palette.bad.dot,
                )
            }
        }
    }
}

@Composable
private fun LivePill(running: Boolean) {
    val palette = CrmTheme.colors
    val transition = rememberInfiniteTransition(label = "live-pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (running) 0.95f else 0.4f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "live-pulse-alpha",
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(palette.onDarkGlass)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(
                    if (running) palette.primary.copy(alpha = pulseAlpha)
                    else palette.muted,
                ),
        )
        Text(
            text = "LIVE",
            style = CrmTheme.type.label.copy(color = palette.brand.dot),
        )
    }
}

@Composable
private fun RowScope.GhostTimerBtn(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color? = null,
) {
    val palette = CrmTheme.colors
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
            .background(palette.onDarkGlass)
            .border(
                width = CrmTheme.dimens.borderThin,
                color = palette.onDarkGlass,
                shape = RoundedCornerShape(CrmTheme.dimens.radius10),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = CrmTheme.dimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent ?: palette.onDark,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(CrmTheme.dimens.spacing6))
        Text(
            text = label,
            style = CrmTheme.type.label.copy(
                color = accent ?: palette.onDark,
            ),
        )
    }
}

@Suppress("UnusedPrivateMember")
@Composable
private fun StatusTokenPlaceholderUsage() {
    // No-op: ensures StatusToken import path stays valid for callers wiring
    // this component into screens.
    StatusToken.BRAND
}

@Preview
@Composable
private fun LiveTimerRunningPreview() {
    CrmTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LiveTimerCard(
                state = LiveTimerState.RUNNING,
                elapsedHhMmSs = "01:23:45",
                stepLabel = "Krok 2 · Wymiana noża plazmy",
                meta = "Twój timer",
                onPause = {},
                onBreak = {},
                onStop = {},
            )
        }
    }
}

@Preview
@Composable
private fun LiveTimerPausedPreview() {
    CrmTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LiveTimerCard(
                state = LiveTimerState.PAUSED,
                elapsedHhMmSs = "00:42:10",
                stepLabel = "Pauza",
                meta = "Twój timer",
                onPause = {},
                onBreak = {},
                onStop = {},
            )
        }
    }
}
