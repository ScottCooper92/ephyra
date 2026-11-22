package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStateLightPreview
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.JellyfinServerProfile
import com.cooper.homelab.ephyra.server.NetworkImpact
import com.cooper.homelab.ephyra.server.NetworkSettings
import com.cooper.homelab.ephyra.server.networkKeys

private val newest = networkKeys(JellyfinServerProfile.from("10.10.7"))
private val oldest = networkKeys(JellyfinServerProfile.from("10.8.13"))

private val everyKey =
    setOf(
        "InternalHttpPort",
        "InternalHttpsPort",
        "PublicHttpPort",
        "PublicHttpsPort",
        "BaseUrl",
        "LocalNetworkAddresses",
        "EnableHttps",
        "RequireHttps",
        "CertificatePath",
        "EnableRemoteAccess",
        "RemoteIPFilter",
        "IsRemoteIPFilterBlacklist",
        "EnableIPv4",
        "EnableIPv6",
        "KnownProxies",
        "LocalNetworkSubnets",
        "AutoDiscovery",
        "EnableUPnP",
        "EnablePublishedServerUriByRequest",
        "PublishedServerUriBySubnet",
        "IgnoreVirtualInterfaces",
        "VirtualInterfaceNames",
    )

private val preview =
    NetworkSettings(
        present = everyKey,
        baseUrl = "/jellyfin",
        enableHttps = true,
        certificatePath = "/config/cert.pfx",
        knownProxies = listOf("10.0.0.2"),
        localSubnets = listOf("192.168.1.0/24", "10.0.0.0/8"),
        virtualNames = listOf("veth*"),
    )

private val ready = NetworkUiState.Ready(preview, newest)

/** A page of one row, so what sits under it shows in a frame that doesn't scroll. */
private val short = NetworkUiState.Ready(NetworkSettings(present = setOf("InternalHttpPort")), newest)

/** Networking: the page across the matrix, an older server's, a change not yet saved, the confirm, each sheet, then the other states. */
class NetworkScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun network() = NetworkScreen(ready, NetworkActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun olderServer() = NetworkScreen(NetworkUiState.Ready(preview.copy(present = everyKey - "EnableUPnP"), oldest), NetworkActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun unsaved() =
        NetworkScreen(
            ready.copy(settings = preview.copy(httpPort = 9000, requireHttps = true), draft = setOf("InternalHttpPort", "RequireHttps")),
            NetworkActions(),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun committing() = NetworkScreen(ready.copy(draft = setOf("RequireHttps"), committing = true), NetworkActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun restart() = NetworkScreen(short.copy(restartNeeded = true), NetworkActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun restartReconnect() = NetworkScreen(short.copy(restartNeeded = true, reconnect = true), NetworkActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun passwordSheet() = Surface { NetworkPasswordSheet(NetworkActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraScreenStateLightPreview
    @EphyraFontScalePreviews
    @Composable
    fun confirmSevere() =
        Surface {
            NetworkConfirm(
                setOf(NetworkImpact.Port, NetworkImpact.BindAddress),
                "http://192.168.1.5:8096",
                NetworkActions(),
                inWindow = false,
            )
        }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun confirmSoft() =
        Surface { NetworkConfirm(setOf(NetworkImpact.RemoteAccess), "https://media.example", NetworkActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraScreenStateLightPreview
    @EphyraFontScalePreviews
    @Composable
    fun portSheet() = Surface { NetworkNumberSheet(NetworkNumber.HttpPort, ready, NetworkActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun baseUrlSheet() = Surface { NetworkTextSheet(NetworkText.BaseUrl, preview, NetworkActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun bindSheet() = Surface { NetworkItemsSheet(NetworkList.BindAddresses, preview, NetworkActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun subnetsSheet() = Surface { NetworkItemsSheet(NetworkList.LocalSubnets, preview, NetworkActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = NetworkScreen(NetworkUiState.Loading, NetworkActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = NetworkScreen(NetworkUiState.Failed(ConsoleFailure.Refused), NetworkActions())
}
