package com.ossadkowski.crm.mobile.ui.serwis

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.ui.serwis.nav.SerwisRoutes
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

/**
 * Side-drawer content for the Serwis module — sections **Praca**, **Procesy**, **Konto**.
 *
 * Designed to live inside a Material3 [androidx.compose.material3.ModalNavigationDrawer]
 * `drawerContent` slot. The host activity (SerwisActivity) wires the actual width
 * (~78% of screen, capped at 300dp) on the parent ModalDrawerSheet.
 */
@Composable
fun SerwisDrawerContent(
    currentRoute: String?,
    technicianName: String,
    appVersion: String,
    onNavigate: (route: String) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrmTheme.colors.surface),
    ) {
        Header(technicianName = technicianName, onClose = onClose)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
        ) {
            DrawerSection(label = "PRACA") {
                DrawerItem(
                    label = "Mój dzień",
                    icon = Icons.Outlined.Today,
                    route = SerwisRoutes.TODAY,
                    currentRoute = currentRoute,
                    onClick = onNavigate,
                )
                DrawerItem(
                    label = "Plan tygodnia",
                    icon = Icons.Outlined.CalendarMonth,
                    route = SerwisRoutes.PLAN,
                    currentRoute = currentRoute,
                    onClick = onNavigate,
                )
                DrawerItem(
                    label = "Maszyny",
                    icon = Icons.Outlined.PrecisionManufacturing,
                    route = SerwisRoutes.MACHINES,
                    currentRoute = currentRoute,
                    onClick = onNavigate,
                )
                DrawerItem(
                    label = "Mój czas",
                    icon = Icons.Outlined.Schedule,
                    route = SerwisRoutes.MY_TIME,
                    currentRoute = currentRoute,
                    onClick = onNavigate,
                )
            }

            DrawerSection(label = "PROCESY") {
                DrawerItem(
                    label = "Skanuj SN",
                    icon = Icons.Outlined.QrCodeScanner,
                    route = SerwisRoutes.SCAN,
                    currentRoute = currentRoute,
                    onClick = onNavigate,
                )
                DrawerItem(
                    label = "Części",
                    icon = Icons.Outlined.Inventory2,
                    route = SerwisRoutes.PARTS,
                    currentRoute = currentRoute,
                    onClick = onNavigate,
                )
                DrawerItem(
                    label = "Alerty",
                    icon = Icons.Outlined.NotificationsActive,
                    route = SerwisRoutes.ALERTS,
                    currentRoute = currentRoute,
                    onClick = onNavigate,
                )
            }

            DrawerSection(label = "KONTO") {
                DrawerItem(
                    label = "Profil",
                    icon = Icons.Outlined.Person,
                    route = SerwisRoutes.PROFILE,
                    currentRoute = currentRoute,
                    onClick = onNavigate,
                )
            }
        }

        Footer(appVersion = appVersion, onLogout = onLogout)
    }
}

@Composable
private fun Header(technicianName: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CrmTheme.colors.brand.bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Build,
                contentDescription = null,
                tint = CrmTheme.colors.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Serwis",
                style = CrmTheme.type.label,
                color = CrmTheme.colors.muted,
            )
            Text(
                text = technicianName,
                style = CrmTheme.type.headline,
                color = CrmTheme.colors.ink,
            )
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Zamknij menu",
                tint = CrmTheme.colors.muted,
            )
        }
    }
}

@Composable
private fun DrawerSection(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = label,
            style = CrmTheme.type.label,
            color = CrmTheme.colors.muted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        content()
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    route: String,
    currentRoute: String?,
    onClick: (String) -> Unit,
) {
    val isActive = currentRoute == route
    val bg = if (isActive) CrmTheme.colors.primary.copy(alpha = 0.12f) else CrmTheme.colors.surface
    val fg = if (isActive) CrmTheme.colors.primary else CrmTheme.colors.ink
    val iconTint = if (isActive) CrmTheme.colors.primary else CrmTheme.colors.muted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onClick(route) }
            .padding(horizontal = 16.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = CrmTheme.type.body,
            color = fg,
        )
    }
}

@Composable
private fun Footer(appVersion: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = null,
                tint = CrmTheme.colors.bad.text,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Wyloguj",
                style = CrmTheme.type.body,
                color = CrmTheme.colors.bad.text,
            )
        }
        Text(
            text = "v $appVersion",
            style = CrmTheme.type.caption,
            color = CrmTheme.colors.muted,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
