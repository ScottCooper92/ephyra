package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStateLightPreview
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.preview.EphyraWidePreview
import com.cooper.homelab.ephyra.preview.EphyraWideSpanishPreview
import com.cooper.homelab.ephyra.server.NowPlaying
import com.cooper.homelab.ephyra.server.PlayMethod
import com.cooper.homelab.ephyra.server.SessionControls
import com.cooper.homelab.ephyra.server.TICKS_PER_MINUTE

private val previewEpisode =
    NowPlaying(
        sessionId = "tv",
        userName = "Ana",
        deviceName = "Living room",
        client = "Jellyfin Android TV",
        title = "Severance",
        episode = "S2:E3 · Who Is Alive?",
        positionTicks = 31 * TICKS_PER_MINUTE,
        runtimeTicks = 52 * TICKS_PER_MINUTE,
        paused = false,
        method = PlayMethod.Transcode,
        transcodeReasons = listOf("AudioCodecNotSupported"),
    )

private val previewMovie =
    NowPlaying(
        sessionId = "phone",
        userName = "Claude",
        deviceName = "Pixel 10 Pro XL",
        client = "Binge",
        title = "Sintel",
        episode = null,
        positionTicks = 4 * TICKS_PER_MINUTE,
        runtimeTicks = 15 * TICKS_PER_MINUTE,
        paused = true,
        method = PlayMethod.DirectPlay,
        transcodeReasons = emptyList(),
    )

private val fullControls = SessionControls(playPause = true, seek = true, skip = true, stop = true)

private val previewReady =
    NowPlayingUiState.Ready(
        listOf(previewEpisode, previewMovie),
        controls = mapOf(previewEpisode.sessionId to fullControls, previewMovie.sessionId to fullControls),
    )

private val remoteControls =
    SessionControls(playPause = true, seek = true, skip = true, stop = true, volume = true, mute = true, message = true)

private fun page(
    session: NowPlaying,
    controls: SessionControls,
    sending: Boolean = false,
) = NowPlayingUiState.Ready(
    listOf(session),
    controls = mapOf(session.sessionId to controls),
    sending = sending,
)

/** Now playing: two streams across the matrix for an admin, then each other state on the phone cell. */
class NowPlayingScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun streams() = NowPlayingScreen(previewReady, administrator = true, NowPlayingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun ownStreams() = NowPlayingScreen(NowPlayingUiState.Ready(listOf(previewMovie)), administrator = false, NowPlayingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraScreenStateLightPreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun sessionPage() = NowPlayingSessionScreen(page(previewEpisode, fullControls), previewEpisode.sessionId, NowPlayingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraScreenStateLightPreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun sessionPageRemote() =
        NowPlayingSessionScreen(
            page(previewEpisode.copy(volumeLevel = 40), remoteControls),
            previewEpisode.copy(volumeLevel = 40).sessionId,
            NowPlayingActions(),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun sessionPageMuted() =
        NowPlayingSessionScreen(
            page(previewEpisode.copy(volumeLevel = 0, muted = true), remoteControls),
            previewEpisode.copy(volumeLevel = 0, muted = true).sessionId,
            NowPlayingActions(),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun messageSheet() =
        Surface { MessageSheet(previewEpisode, page(previewEpisode, remoteControls), NowPlayingActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun messageSheetSending() =
        Surface {
            MessageSheet(
                previewEpisode,
                page(previewEpisode, remoteControls).copy(messageSending = true),
                NowPlayingActions(),
                inWindow = false,
            )
        }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun messageSheetFailed() =
        Surface {
            MessageSheet(
                previewEpisode,
                page(previewEpisode, remoteControls).copy(messageFailed = ConsoleFailure.Unreachable),
                NowPlayingActions(),
                inWindow = false,
            )
        }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun sessionPagePaused() = NowPlayingSessionScreen(page(previewMovie, fullControls), previewMovie.sessionId, NowPlayingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun sessionPageSending() =
        NowPlayingSessionScreen(page(previewEpisode, fullControls, sending = true), previewEpisode.sessionId, NowPlayingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun sessionPageNoSeek() =
        NowPlayingSessionScreen(
            page(previewEpisode, SessionControls(playPause = true, seek = false, skip = true, stop = true)),
            previewEpisode.sessionId,
            NowPlayingActions(),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun sessionPageNoStop() =
        NowPlayingSessionScreen(
            page(previewEpisode, SessionControls(playPause = true, seek = true, skip = true, stop = false)),
            previewEpisode.sessionId,
            NowPlayingActions(),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = NowPlayingScreen(NowPlayingUiState.Loading, administrator = true, NowPlayingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraScreenStateLightPreview
    @EphyraSpanishPreviews
    @Composable
    fun nothingPlaying() = NowPlayingScreen(NowPlayingUiState.Ready(emptyList()), administrator = true, NowPlayingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun confirmingStop() = Surface { StopDialog(previewEpisode, NowPlayingActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun stopping() = NowPlayingScreen(previewReady.copy(stopping = "tv"), administrator = true, NowPlayingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = NowPlayingScreen(NowPlayingUiState.Failed(ConsoleFailure.Refused), administrator = false, NowPlayingActions())
}

/** The page on a landscape tablet, where a stream opens in a third pane: the invitation to pick one while none is. */
class NowPlayingPaneScreenshotTest {
    @PreviewTest
    @EphyraWidePreview
    @EphyraWideSpanishPreview
    @Composable
    fun placeholder() = Surface { NowPlayingPlaceholder() }
}
