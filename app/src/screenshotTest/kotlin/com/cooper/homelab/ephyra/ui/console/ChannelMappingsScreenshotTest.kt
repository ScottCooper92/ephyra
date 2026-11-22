package com.cooper.homelab.ephyra.ui.console

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.preview.EphyraWidePreview
import com.cooper.homelab.ephyra.server.ChannelMappingOptions
import com.cooper.homelab.ephyra.server.ProviderChannel
import com.cooper.homelab.ephyra.server.TunerChannel

private val options =
    ChannelMappingOptions(
        providerName = "Freeview XMLTV",
        providerChannels =
            listOf(
                ProviderChannel("p1", "BBC One"),
                ProviderChannel("p2", "BBC Two"),
                ProviderChannel("p3", "ITV"),
                ProviderChannel("p4", "Channel 4"),
            ),
        tunerChannels =
            listOf(
                TunerChannel("c1", "101 BBC One HD", "p1"),
                TunerChannel("c2", "102 BBC Two HD", "p2"),
                TunerChannel("c3", "103 ITV HD", null),
                TunerChannel("c4", "104 ITV2", "p1"),
                TunerChannel("c5", "105 Channel 4 HD", null),
            ),
    )

private fun ready(selected: String? = null) = ChannelMappingsUiState.Ready(options, selected)

/** Channel mappings: the list across the matrix, the tuner channels in the phone's sheet and beside the list on a wide window. */
class ChannelMappingsScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun list() = ChannelMappingsScreen(ready(), ChannelMappingsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = ChannelMappingsScreen(ChannelMappingsUiState.Loading, ChannelMappingsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = ChannelMappingsScreen(ChannelMappingsUiState.Failed(ConsoleFailure.Unreachable), ChannelMappingsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun noChannels() =
        ChannelMappingsScreen(ChannelMappingsUiState.Ready(options.copy(providerChannels = emptyList())), ChannelMappingsActions())

    /** The tuner channels as the pane draws them (the sheet's list): the filter, the channel it is paired with ticked, and one paired with another channel. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun tunerChannels() =
        Surface { TunerChannelPane(ready("p1"), options.providerChannels.first(), ChannelMappingsActions(), PaddingValues()) }

    /** A filter that matches no tuner channel: the search-off line where the rows were. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun tunerChannelsNoMatch() =
        Surface {
            TunerChannelPane(
                ready("p1"),
                options.providerChannels.first(),
                ChannelMappingsActions(),
                PaddingValues(),
                initialQuery = "zzz",
            )
        }

    /** The same, beside the list from 600 dp wide. */
    @PreviewTest
    @EphyraWidePreview
    @Composable
    fun twoPanesNoMatch() =
        Surface {
            TunerChannelPane(
                ready("p1"),
                options.providerChannels.first(),
                ChannelMappingsActions(),
                PaddingValues(),
                initialQuery = "zzz",
            )
        }

    /** From 600 dp wide: the list and the tuner channels of the open channel, side by side. */
    @PreviewTest
    @EphyraWidePreview
    @Composable
    fun twoPanes() = ChannelMappingsScreen(ready("p3"), ChannelMappingsActions())

    @PreviewTest
    @EphyraWidePreview
    @Composable
    fun twoPanesNothingOpen() = ChannelMappingsScreen(ready(), ChannelMappingsActions())
}
