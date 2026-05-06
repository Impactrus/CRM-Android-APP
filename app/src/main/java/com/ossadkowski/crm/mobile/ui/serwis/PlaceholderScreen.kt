package com.ossadkowski.crm.mobile.ui.serwis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ossadkowski.crm.mobile.ui.serwis.components.PhoneFrame
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

/**
 * Stub destination used by the 6 not-yet-implemented Serwis routes
 * (Plan tygodnia, Skanuj SN, Części, Alerty, Mój czas, Profil).
 *
 * Renders a simple back-arrow top bar and an "Wkrótce" message so the
 * navigation graph compiles end-to-end without holes — the real screens
 * will replace each entry in a later PR.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit,
    icon: ImageVector = Icons.Outlined.HourglassEmpty,
) {
    PhoneFrame(
        topBar = { PlaceholderTopBar(title = title, onBack = onBack) },
    ) { padding: PaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CrmTheme.colors.muted,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Wkrótce",
                    style = CrmTheme.type.title,
                    color = CrmTheme.colors.ink,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ten ekran jest w przygotowaniu.",
                    style = CrmTheme.type.body,
                    color = CrmTheme.colors.muted,
                )
            }
        }
    }
}

@Composable
private fun PlaceholderTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Wstecz",
                tint = CrmTheme.colors.ink,
            )
        }
        Text(
            text = title,
            style = CrmTheme.type.headline,
            color = CrmTheme.colors.ink,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
