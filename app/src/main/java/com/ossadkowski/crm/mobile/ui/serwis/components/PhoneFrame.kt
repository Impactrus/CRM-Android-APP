package com.ossadkowski.crm.mobile.ui.serwis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

/**
 * Edge-to-edge layout helper for Serwis screens.
 *
 * Spec: `components/PhoneFrame.md`. Wraps Material3 [Scaffold] but:
 *  - background defaults to [CrmTheme.colors.bg]
 *  - `contentWindowInsets` is [WindowInsets.systemBars] so screens still get
 *    inset-aware padding without forcing a fixed status-bar height
 *  - status bar / nav bar are expected to be transparent (the host activity
 *    must call `WindowCompat.setDecorFitsSystemWindows(window, false)` and
 *    set both bar colors to `Color.TRANSPARENT`)
 *
 * Use the same slots as Scaffold: [topBar], [bottomBar], [floatingActionButton].
 * Insets handling for hero gradients (statusBar pad) lives inside [HeroHeader]
 * itself — pass it to [topBar] and let it absorb the inset.
 */
@Composable
fun PhoneFrame(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = CrmTheme.colors.bg,
        contentWindowInsets = WindowInsets.systemBars,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CrmTheme.colors.bg),
            ) {
                content(padding)
            }
        },
    )
}
