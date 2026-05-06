package com.ossadkowski.crm.mobile.ui.serwis.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.BuildConfig
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummary
import com.ossadkowski.crm.mobile.ui.serwis.components.PhoneFrame
import com.ossadkowski.crm.mobile.ui.serwis.components.SectionCard
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.ErrorState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.LoadingState
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

@Composable
fun ProfileScreen(
    vm: ProfileViewModel = hiltViewModel(),
    onMenuClick: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    PhoneFrame { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                ProfileUiState.Loading -> LoadingState()
                is ProfileUiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = { vm.refresh() },
                )
                is ProfileUiState.Success -> ProfileContent(
                    profile = s.profile,
                    monthSummary = s.monthSummary,
                    onMenuClick = onMenuClick,
                    onLogout = {
                        vm.logout()
                        onLogout()
                    },
                )
            }
        }
    }
}

/* ----------------------------- content ----------------------------- */

@Composable
private fun ProfileContent(
    profile: ProfileInfo,
    monthSummary: TimeSummary?,
    onMenuClick: () -> Unit,
    onLogout: () -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // 1. Hero header (brand-green primary, NOT dark)
        ProfileHero(
            initials = profile.initials,
            onMenuClick = onMenuClick,
        )

        Spacer(Modifier.height(dimens.spacing16))

        // 2. Name + sub-line
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = profile.fullName,
                style = CrmTheme.type.title.copy(color = palette.ink),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(dimens.spacing4))
            Text(
                text = "@${profile.username} · ${profile.dzial ?: "—"}",
                style = CrmTheme.type.body.copy(color = palette.muted),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(dimens.spacing16))

        // 3. Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing8),
        ) {
            StatTile(
                value = if (monthSummary != null) "${formatHours(monthSummary.totalHours)}h" else "—",
                label = "Praca w tym miesiącu",
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = if (monthSummary != null) "${formatHours(monthSummary.totalTravelHours)}h" else "—",
                label = "Dojazd",
                modifier = Modifier.weight(1f),
            )
            StatTile(
                value = if (monthSummary != null) "${formatKm(monthSummary.totalKilometers)} km" else "—",
                label = "Przejechane",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(dimens.spacing16))

        // 4. Settings section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
        ) {
            SectionCard(label = "Ustawienia") {
                SettingsList()
            }
        }

        Spacer(Modifier.height(dimens.spacing24))

        // 5. Logout button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
        ) {
            LogoutButton(onClick = onLogout)
        }

        Spacer(Modifier.height(dimens.spacing32))
    }
}

/* ----------------------------- pieces ----------------------------- */

@Composable
private fun ProfileHero(
    initials: String,
    onMenuClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = dimens.radius30,
                    bottomEnd = dimens.radius30,
                ),
            )
            .background(palette.primary)
            .padding(horizontal = dimens.spacing20)
            .padding(
                top = dimens.statusBarPad + dimens.spacing16,
                bottom = dimens.spacing24,
            ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OnPrimaryIconBtn(
                    icon = Icons.Outlined.Menu,
                    contentDescription = "Menu",
                    onClick = onMenuClick,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "PROFIL",
                    style = CrmTheme.type.label.copy(color = palette.onPrimary.copy(alpha = 0.7f)),
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(dimens.hamSize))
            }
            Spacer(Modifier.height(dimens.spacing16))
            Text(
                text = "Profil",
                style = CrmTheme.type.title.copy(
                    color = palette.onPrimary,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(Modifier.height(dimens.spacing16))
            // Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(
                            width = 3.dp,
                            color = palette.onDark,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        style = CrmTheme.type.display.copy(
                            color = palette.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun OnPrimaryIconBtn(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    Box(
        modifier = Modifier
            .size(CrmTheme.dimens.hamSize)
            .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
            .background(palette.onPrimary.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.onPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.radius14))
            .background(palette.surface)
            .border(
                width = dimens.borderThin,
                color = palette.line,
                shape = RoundedCornerShape(dimens.radius14),
            )
            .padding(dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(dimens.spacing4),
    ) {
        Text(
            text = value,
            style = CrmTheme.type.display.copy(
                color = palette.ink,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
        )
        Text(
            text = label.uppercase(),
            style = CrmTheme.type.label.copy(color = palette.muted),
        )
    }
}

@Composable
private fun SettingsList() {
    val palette = CrmTheme.colors
    var pushEnabled by remember { mutableStateOf(true) }

    SettingRow(
        title = "Powiadomienia push",
        subtitle = null,
        trailing = {
            Switch(
                checked = pushEnabled,
                onCheckedChange = { pushEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = palette.primary,
                    checkedThumbColor = palette.onPrimary,
                ),
            )
        },
        onClick = null,
    )
    HorizontalDivider(color = palette.line, thickness = CrmTheme.dimens.borderThin)
    SettingRow(
        title = "Język aplikacji",
        subtitle = "Polski",
        trailing = {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = palette.muted,
            )
        },
        onClick = {},
    )
    HorizontalDivider(color = palette.line, thickness = CrmTheme.dimens.borderThin)
    SettingRow(
        title = "Synchronizacja danych",
        subtitle = "Wł.",
        trailing = {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = palette.muted,
            )
        },
        onClick = {},
    )
    HorizontalDivider(color = palette.line, thickness = CrmTheme.dimens.borderThin)
    SettingRow(
        title = "Wersja aplikacji",
        subtitle = BuildConfig.VERSION_NAME,
        trailing = null,
        onClick = null,
    )
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String?,
    trailing: (@Composable () -> Unit)?,
    onClick: (() -> Unit)?,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = dimens.spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = CrmTheme.type.body.copy(
                    color = palette.ink,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = CrmTheme.type.caption.copy(color = palette.muted),
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    val red = palette.bad.text
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimens.bottomCta)
            .clip(RoundedCornerShape(dimens.radius17))
            .border(
                width = dimens.borderMed,
                color = red,
                shape = RoundedCornerShape(dimens.radius17),
            )
            .background(Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.spacing20),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Logout,
            contentDescription = null,
            tint = red,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(dimens.spacing8))
        Text(
            text = "Wyloguj".uppercase(),
            style = CrmTheme.type.label.copy(color = red),
        )
    }
}

/* ----------------------------- helpers ----------------------------- */

private fun formatHours(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(java.util.Locale.ROOT, "%.1f", value)

private fun formatKm(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(java.util.Locale.ROOT, "%.1f", value)
