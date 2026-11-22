package com.cooper.homelab.ephyra.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownAndroidComposeRule
import com.cooper.homelab.core.uiandroid.connect.AllowLocalNetwork
import com.cooper.homelab.ephyra.server.PublicServerInfo
import com.cooper.homelab.ephyra.theme.EphyraTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.cooper.homelab.core.uiandroid.R as CoreR

/** The setup steps name a missing local-network permission and offer the way to grant it, rather than blaming the server. */
@RunWith(RobolectricTestRunner::class)
class SetupLocalNetworkTest {
    @get:Rule
    val compose = createTakeDownAndroidComposeRule<ComponentActivity>()

    private val message = "Local network access is off. Allow it, then try again."

    private val server =
        PublicServerInfo(
            serverName = "Living room",
            version = "10.11.6",
            serverId = "a1",
            baseUrl =
                okhttp3.HttpUrl
                    .Builder()
                    .scheme("http")
                    .host("192.168.1.20")
                    .port(8096)
                    .build(),
        )

    @Test
    fun `the server step says so and offers to allow it`() {
        val calls = mutableListOf<String>()
        compose.setContent {
            EphyraTheme {
                EphyraRoot(
                    UiState.EnterServer(address = "192.168.1.20:8096", error = SetupError.LocalNetworkDenied),
                    Actions(
                        localNetwork =
                            AllowLocalNetwork(CoreR.string.connect_local_network_allow, CoreR.string.connect_local_network_allow_short) {
                                calls +=
                                    "allow"
                            },
                    ),
                )
            }
        }
        compose.onNodeWithText(message).assertExists()
        compose.onNodeWithText("Allow local network access").performClick()
        assertEquals(listOf("allow"), calls)
    }

    @Test
    fun `once it has been asked the server step offers the app's settings`() {
        compose.setContent {
            EphyraTheme {
                EphyraRoot(
                    UiState.EnterServer(address = "192.168.1.20:8096", error = SetupError.LocalNetworkDenied),
                    Actions(
                        localNetwork =
                            AllowLocalNetwork(
                                CoreR.string.connect_local_network_open_settings,
                                CoreR.string.connect_local_network_open_settings,
                            ) {
                            },
                    ),
                )
            }
        }
        compose.onNodeWithText("Open settings").assertExists()
        compose.onAllNodesWithText("Allow local network access").assertCountEquals(0)
    }

    @Test
    fun `an ordinary unreachable server offers no such button`() {
        compose.setContent {
            EphyraTheme { EphyraRoot(UiState.EnterServer(address = "192.168.1.20:8096", error = SetupError.Unreachable), Actions()) }
        }
        compose.onAllNodesWithText("Allow local network access").assertCountEquals(0)
        compose.onAllNodesWithText("Open settings").assertCountEquals(0)
    }

    @Test
    fun `sign-in says so under a password failure and under a Quick Connect failure`() {
        compose.setContent {
            EphyraTheme {
                EphyraRoot(
                    UiState.SignIn(baseUrl = "http://192.168.1.20:8096", server = server, passwordError = SetupError.LocalNetworkDenied),
                    Actions(
                        localNetwork =
                            AllowLocalNetwork(CoreR.string.connect_local_network_allow, CoreR.string.connect_local_network_allow_short) {},
                    ),
                )
            }
        }
        compose.onNodeWithText(message).assertExists()
        compose.onNodeWithText("Allow local network access").assertExists()
    }

    @Test
    fun `a server that has not finished its setup says so in place of the sign-in, and can be checked again`() {
        val calls = mutableListOf<String>()
        compose.setContent {
            EphyraTheme {
                EphyraRoot(
                    UiState.SignIn(baseUrl = "http://192.168.1.20:8096", server = server.copy(startupWizardCompleted = false)),
                    Actions(onRecheckServer = { calls += "recheck" }),
                )
            }
        }
        compose.onNodeWithText("This server isn’t set up yet").assertExists()
        compose.onAllNodesWithText("Sign in").assertCountEquals(0)
        compose.onNodeWithText("Check again").performClick()
        assertEquals(listOf("recheck"), calls)
    }
}
