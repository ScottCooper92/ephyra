package com.cooper.homelab.ephyra.ui.client

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.playback.AudioSwitchFailure
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews

/** What Play says where it cannot begin (the demo, and a server reached over plain http at a public address), and when the audio did not change. */
class PlayBlockedScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @EphyraSpanishFontScalePreviews
    @Composable
    fun demo() = PlayBlockedMessage(PlayBlock.Demo, onDismiss = {}, inWindow = false)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @EphyraSpanishFontScalePreviews
    @Composable
    fun publicHttp() = PlayBlockedMessage(PlayBlock.PublicHttp, onDismiss = {}, inWindow = false)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @EphyraSpanishFontScalePreviews
    @Composable
    fun audioUnreachable() = AudioSwitchMessage(AudioSwitchFailure.Unreachable, onDismiss = {}, inWindow = false)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @EphyraSpanishFontScalePreviews
    @Composable
    fun audioRefused() = AudioSwitchMessage(AudioSwitchFailure.Refused, onDismiss = {}, inWindow = false)
}
