package com.cooper.homelab.ephyra.ui.console

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraWidePreview
import com.cooper.homelab.ephyra.ui.nav.EphyraNavHost
import com.cooper.homelab.ephyra.ui.nav.HubRoute
import com.cooper.homelab.ephyra.ui.nav.openFromHome
import com.cooper.homelab.ephyra.ui.nav.openItem

/**
 * The console on a window wide enough for two panes: the home beside the default page, then beside an open page. On a
 * landscape tablet a page that opens its items as entries is the middle pane, with its item, or the invitation to pick one, as the third.
 */
class ConsoleNavHostScreenshotTest {
    @Composable
    private fun Console(open: NavBackStack<NavKey>.() -> Unit = {}) =
        Surface {
            EphyraNavHost(
                backStack = remember { NavBackStack<NavKey>(HubRoute).apply(open) },
                signedIn = true,
                onBack = {},
                signedOut = {},
                home = {
                    Column {
                        Text("Home")
                        Text("Logs")
                    }
                },
                defaultPage = { Text("Now playing") },
                page = { entry -> Text(entry.page.name) },
                item = { entry -> Text("${entry.page.name} ${entry.item}") },
            )
        }

    @PreviewTest
    @EphyraWidePreview
    @Composable
    fun defaultPage() = Console()

    @PreviewTest
    @EphyraWidePreview
    @Composable
    fun pageOpen() = Console { openFromHome(ConsolePage.Activity) }

    @PreviewTest
    @EphyraWidePreview
    @Composable
    fun itemPageNothingOpen() = Console { openFromHome(ConsolePage.Tasks) }

    @PreviewTest
    @EphyraWidePreview
    @Composable
    fun itemOpen() =
        Console {
            openFromHome(ConsolePage.Tasks)
            openItem("scan")
        }
}
