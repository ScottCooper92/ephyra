package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.SaveStatus
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStateLightPreview
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.TrickplaySettings

private val previewSettings =
    TrickplaySettings(
        hwAcceleration = true,
        keyFrameOnly = true,
        processPriority = "Normal",
        widths = listOf(320, 640),
        threads = 0,
    )

private val previewReady = TrickplayUiState.Ready(previewSettings)

/** Trickplay: the page across the matrix, without the 10.10 key frames switch, each dialog, then each other state. */
class TrickplayScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun trickplay() = TrickplayScreen(previewReady, TrickplayActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun beforeKeyFrames() = TrickplayScreen(TrickplayUiState.Ready(TrickplaySettings()), TrickplayActions(), keyFrameOnlyAvailable = false)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraScreenStateLightPreview
    @EphyraFontScalePreviews
    @Composable
    fun number() = Surface { FieldForm(TrickplayField.JpegQuality, previewSettings, TrickplayActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraScreenStateLightPreview
    @EphyraFontScalePreviews
    @Composable
    fun widths() = Surface { FieldForm(TrickplayField.Widths, previewSettings, TrickplayActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun saving() = TrickplayScreen(previewReady.copy(save = SaveProgress(SaveStatus.Saving)), TrickplayActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = TrickplayScreen(TrickplayUiState.Loading, TrickplayActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = TrickplayScreen(TrickplayUiState.Failed(ConsoleFailure.Refused), TrickplayActions())
}
