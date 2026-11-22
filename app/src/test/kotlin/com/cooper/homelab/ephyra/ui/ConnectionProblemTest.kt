package com.cooper.homelab.ephyra.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownAndroidComposeRule
import com.cooper.homelab.core.uiandroid.connect.AllowLocalNetwork
import com.cooper.homelab.ephyra.auth.JellyfinSession
import com.cooper.homelab.ephyra.server.JellyfinAccount
import com.cooper.homelab.ephyra.theme.EphyraTheme
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.cooper.homelab.core.uiandroid.R as CoreR

/** A saved server that cannot be reached or read takes the home's place, with Try again, Edit connection and Sign out. */
@RunWith(RobolectricTestRunner::class)
class ConnectionProblemTest {
    @get:Rule
    val compose = createTakeDownAndroidComposeRule<ComponentActivity>()

    private val session =
        JellyfinSession("http://192.168.1.2:8096", "Living room", "a1", "10.11.6", "u1", "sam", "not-a-token")

    private fun state(
        health: ConnectionHealth,
        refreshing: Boolean = false,
    ) = UiState.SignedIn(
        session,
        titles = null,
        refreshing = refreshing,
        health = health,
        account = JellyfinAccount("sam", Json.parseToJsonElement("""{"IsAdministrator":true}""").jsonObject),
    )

    @Test
    fun `an unreachable server shows its problem and the three ways out, not the home`() {
        compose.setContent { EphyraTheme { EphyraRoot(state(ConnectionHealth.Unreachable), Actions()) } }
        compose.onNodeWithText("Can't reach your server").assertExists()
        compose.onNodeWithText("Try again").assertExists()
        compose.onNodeWithText("Edit connection").assertExists()
        compose.onNodeWithText("Sign out").assertExists()
        compose.onAllNodesWithText("Scheduled tasks").assertCountEquals(0)
        compose.onAllNodesWithContentDescription("Your account").assertCountEquals(0)
    }

    @Test
    fun `a server that asks for a client certificate says so`() {
        compose.setContent { EphyraTheme { EphyraRoot(state(ConnectionHealth.ClientCertificateNeeded), Actions()) } }
        compose.onNodeWithText("This server asks for a client certificate").assertExists()
        compose.onNodeWithText("Edit connection").assertExists()
        compose.onNodeWithText("Sign out").assertExists()
    }

    @Test
    fun `a refused certificate says so, leads with editing the connection, and offers no retry`() {
        val calls = mutableListOf<String>()
        compose.setContent {
            EphyraTheme {
                EphyraRoot(
                    state(ConnectionHealth.CertificateRefused),
                    Actions(onRefresh = { calls += "retry" }, onChangeServer = { calls += "edit" }, onAskSignOut = { calls += "signout" }),
                )
            }
        }
        compose.onNodeWithText("This server's certificate isn't trusted").assertExists()
        compose.onAllNodesWithText("Try again").assertCountEquals(0)
        compose.onNodeWithText("Edit connection").performClick()
        compose.onNodeWithText("Sign out").performClick()
        assertEquals(listOf("edit", "signout"), calls)
    }

    @Test
    fun `a refused certificate is not retried on a timer`() {
        var retries = 0
        compose.mainClock.autoAdvance = false
        compose.setContent { EphyraTheme { EphyraRoot(state(ConnectionHealth.CertificateRefused), Actions(onRefresh = { retries++ })) } }
        compose.mainClock.advanceTimeBy(60_000)
        assertEquals(0, retries)
    }

    @Test
    fun `a server that answered something unusable says so`() {
        compose.setContent { EphyraTheme { EphyraRoot(state(ConnectionHealth.CouldNotLoad), Actions()) } }
        compose.onNodeWithText("Couldn't load your server").assertExists()
    }

    @Test
    fun `a healthy server shows the home`() {
        compose.setContent { EphyraTheme { EphyraRoot(state(ConnectionHealth.Healthy), Actions()) } }
        compose.onNodeWithContentDescription("Your account").assertExists()
        compose.onAllNodesWithText("Try again").assertCountEquals(0)
        compose.onAllNodesWithText("Can't reach your server").assertCountEquals(0)
    }

    /**
     * A server that has not answered yet shows nothing of itself: the home is drawn from what was saved last time, so a start
     * that cannot reach it would otherwise show a console that looks usable until the read times out.
     */
    @Test
    fun `a server that has not answered yet shows neither the home nor a problem`() {
        compose.setContent { EphyraTheme { EphyraRoot(state(ConnectionHealth.Checking), Actions()) } }
        compose.onAllNodesWithText("Scheduled tasks").assertCountEquals(0)
        compose.onAllNodesWithContentDescription("Your account").assertCountEquals(0)
        compose.onAllNodesWithText("Try again").assertCountEquals(0)
        compose.onAllNodesWithText("Can't reach your server").assertCountEquals(0)
    }

    @Test
    fun `each way out reaches its action`() {
        val calls = mutableListOf<String>()
        compose.setContent {
            EphyraTheme {
                EphyraRoot(
                    state(ConnectionHealth.Unreachable),
                    Actions(onRefresh = { calls += "retry" }, onChangeServer = { calls += "edit" }, onAskSignOut = { calls += "signout" }),
                )
            }
        }
        compose.onNodeWithText("Try again").performClick()
        compose.onNodeWithText("Edit connection").performClick()
        compose.onNodeWithText("Sign out").performClick()
        assertEquals(listOf("retry", "edit", "signout"), calls)
    }

    @Test
    fun `a retry in flight shows a spinner in place of its label and swallows taps`() {
        val calls = mutableListOf<String>()
        compose.setContent {
            EphyraTheme {
                EphyraRoot(
                    state(ConnectionHealth.Unreachable, refreshing = true),
                    Actions(onRefresh = { calls += "retry" }, onChangeServer = { calls += "edit" }),
                )
            }
        }
        compose.onAllNodesWithText("Try again").assertCountEquals(0)
        // The retry is the first button on the page; the others stay open while it works.
        compose.onAllNodes(hasClickAction()).onFirst().performClick()
        compose.onNodeWithText("Edit connection").performClick()
        assertEquals(listOf("edit"), calls)
    }

    @Test
    fun `a missing local-network permission swaps Try again for the way to grant it, then for the app's settings`() {
        val calls = mutableListOf<String>()
        var asked by mutableStateOf(false)
        compose.setContent {
            EphyraTheme {
                EphyraRoot(
                    state(ConnectionHealth.LocalNetworkDenied),
                    Actions(
                        localNetwork =
                            if (asked) {
                                AllowLocalNetwork(
                                    CoreR.string.connect_local_network_open_settings,
                                    CoreR.string.connect_local_network_open_settings,
                                ) {}
                            } else {
                                AllowLocalNetwork(
                                    CoreR.string.connect_local_network_allow,
                                    CoreR.string.connect_local_network_allow_short,
                                ) {
                                    calls +=
                                        "allow"
                                }
                            },
                    ),
                )
            }
        }
        compose.onNodeWithText("Local network access is off").assertExists()
        compose.onAllNodesWithText("Try again").assertCountEquals(0)
        compose.onNodeWithText("Edit connection").assertExists()
        compose.onNodeWithText("Allow local network").performClick()
        assertEquals(listOf("allow"), calls)
        asked = true
        compose.waitForIdle()
        compose.onNodeWithText("Open settings").assertExists()
        compose.onAllNodesWithText("Allow local network").assertCountEquals(0)
    }

    @Test
    fun `an unreachable saved server is asked about again on the shared backoff`() {
        var retries = 0
        compose.mainClock.autoAdvance = false
        compose.setContent { EphyraTheme { EphyraRoot(state(ConnectionHealth.Unreachable), Actions(onRefresh = { retries++ })) } }
        compose.mainClock.advanceTimeBy(1_500)
        assertEquals(0, retries)
        compose.mainClock.advanceTimeBy(1_000)
        assertEquals(1, retries)
    }

    @Test
    fun `a missing local-network permission is not retried on a timer`() {
        var retries = 0
        compose.mainClock.autoAdvance = false
        compose.setContent { EphyraTheme { EphyraRoot(state(ConnectionHealth.LocalNetworkDenied), Actions(onRefresh = { retries++ })) } }
        compose.mainClock.advanceTimeBy(60_000)
        assertEquals(0, retries)
    }
}
