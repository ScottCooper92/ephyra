package com.cooper.homelab.ephyra.ui

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.Culture
import com.cooper.homelab.ephyra.server.PlaybackPreferences
import com.cooper.homelab.ephyra.server.SubtitleMode
import com.cooper.homelab.ephyra.ui.console.ConsoleFailure
import com.cooper.homelab.ephyra.ui.console.PlaybackUiState

private val previewCultures = listOf(Culture("English", "en", listOf("eng")), Culture("Japanese", "ja", listOf("jpn")))

/** The This app page with the client on, and the device's two streaming limits under Playback. */
class ThisAppQualityScreenshotTest {
    private val clientOn = Actions(appSettings = AppSettings(client = true))

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun limits() =
        ThisAppScreen(
            clientOn,
            onBack = {},
            playback =
                PlaybackUiState.Ready(
                    PlaybackPreferences(audio = "jpn", subtitles = "eng", subtitleMode = SubtitleMode.Smart),
                    previewCultures,
                ),
            quality = StreamingQualitySettings(wifiMbps = 20, mobileMbps = 4),
        )

    /** The server's preferences could not be read: the device's own limits are still there, and nothing else is in the group. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun auto() =
        ThisAppScreen(
            clientOn,
            onBack = {},
            playback = PlaybackUiState.Failed(ConsoleFailure.Unreachable),
            quality = StreamingQualitySettings(wifiMbps = 0, mobileMbps = 0),
        )
}
