package com.ossadkowski.crm.mobile.ui.nawozy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

/**
 * Drawer body for the fertiliser-order host activity. The module is single-section
 * (the orders list), so the drawer mainly brands the surface, shows who is signed
 * in, and offers logout — mirroring the Serwis drawer layout at a smaller scale.
 */
@Composable
fun NawozyDrawerContent(
    userName: String,
    appVersion: String,
    onGoToList: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
    ) {
        Text(
            text = "Zamówienia nawozy",
            style = CrmTheme.type.headline,
            color = CrmTheme.colors.ink,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = userName,
            style = CrmTheme.type.body,
            color = CrmTheme.colors.muted,
            modifier = Modifier.padding(top = 2.dp),
        )

        Spacer(Modifier.padding(8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Inventory2, contentDescription = null) },
            label = { Text("Lista zamówień") },
            selected = true,
            onClick = onGoToList,
        )

        Spacer(Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            label = { Text("Wyloguj") },
            selected = false,
            onClick = onLogout,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "v$appVersion",
                style = CrmTheme.type.label,
                color = CrmTheme.colors.muted,
            )
        }
    }
}
