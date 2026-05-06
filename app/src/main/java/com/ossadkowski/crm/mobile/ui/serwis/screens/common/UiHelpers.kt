package com.ossadkowski.crm.mobile.ui.serwis.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderStatus
import com.ossadkowski.crm.mobile.domain.serwis.model.WarrantyStatus
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import com.ossadkowski.crm.mobile.ui.serwis.theme.StatusToken

/**
 * Centered loading spinner with the brand tint. Use as a full-screen state.
 */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = CrmTheme.colors.primary)
    }
}

/**
 * Centered error state with an alert icon, message, and "Ponów" button.
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimens.spacing24),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = palette.bad.dot,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(dimens.spacing12))
        Text(
            text = message,
            style = CrmTheme.type.body.copy(color = palette.ink),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(dimens.spacing16))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(dimens.radius14))
                .background(palette.primary)
                .clickable(onClick = onRetry)
                .padding(horizontal = dimens.spacing20, vertical = dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                tint = palette.onPrimary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(dimens.spacing8))
            Text(
                text = "Ponów",
                style = CrmTheme.type.label.copy(color = palette.onPrimary),
            )
        }
    }
}

/**
 * Centered empty placeholder. Optional [action] composable rendered below the text
 * (typically a CTA button).
 */
@Composable
fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimens.spacing24),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            tint = palette.muted,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(dimens.spacing12))
        Text(
            text = text,
            style = CrmTheme.type.body.copy(color = palette.muted),
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(dimens.spacing16))
            action()
        }
    }
}

/**
 * Light topbar — surface bg, 56dp height, optional subtitle, back arrow, actions slot.
 * Rendered as the `topBar` slot of [com.ossadkowski.crm.mobile.ui.serwis.components.PhoneFrame].
 */
@Composable
fun TopBarLight(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.surface),
    ) {
        Spacer(Modifier.height(dimens.statusBarPad))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = dimens.spacing4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Wstecz",
                    tint = palette.ink,
                )
            }
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = dimens.spacing4)) {
                Text(
                    text = title,
                    style = CrmTheme.type.headline.copy(color = palette.ink),
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = CrmTheme.type.caption.copy(color = palette.muted),
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacing4),
                content = actions,
            )
        }
        HorizontalDivider(color = palette.line, thickness = dimens.borderThin)
    }
}

/* ------------------------- enum -> UI mappings ------------------------- */

fun OrderStatus.label(): String = when (this) {
    OrderStatus.OPEN -> "Otwarte"
    OrderStatus.IN_PROGRESS -> "W trakcie"
    OrderStatus.CLOSED -> "Zakończone"
    OrderStatus.UNKNOWN -> "Nieznany"
}

fun OrderStatus.statusToken(): StatusToken = when (this) {
    OrderStatus.OPEN -> StatusToken.INFO
    OrderStatus.IN_PROGRESS -> StatusToken.WARN
    OrderStatus.CLOSED -> StatusToken.OK
    OrderStatus.UNKNOWN -> StatusToken.BRAND
}

fun WarrantyStatus.label(): String = when (this) {
    WarrantyStatus.ACTIVE -> "Aktywna"
    WarrantyStatus.EXPIRING_SOON -> "Wkrótce wygasa"
    WarrantyStatus.EXPIRED -> "Wygasła"
    WarrantyStatus.UNKNOWN -> "Brak danych"
}

fun WarrantyStatus.statusToken(): StatusToken = when (this) {
    WarrantyStatus.ACTIVE -> StatusToken.OK
    WarrantyStatus.EXPIRING_SOON -> StatusToken.WARN
    WarrantyStatus.EXPIRED -> StatusToken.BAD
    WarrantyStatus.UNKNOWN -> StatusToken.BRAND
}
