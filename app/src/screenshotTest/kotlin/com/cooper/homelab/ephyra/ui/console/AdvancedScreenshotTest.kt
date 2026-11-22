package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.SaveStatus
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStateLightPreview
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.AdvancedSettings
import com.cooper.homelab.ephyra.server.CastReceiver

private val everyKey =
    setOf(
        "ActivityLogRetentionDays",
        "LibraryMonitorDelay",
        "LibraryUpdateDuration",
        "LibraryScanFanoutConcurrency",
        "ParallelImageEncodingLimit",
        "ImageExtractionTimeoutMs",
        "EnableSlowResponseWarning",
        "SlowResponseThresholdMs",
        "EnableMetrics",
        "AllowClientLogUpload",
        "CorsHosts",
        "InactiveSessionThreshold",
        "CastReceiverApplications",
        "CacheSize",
        "EnableLegacyAuthorization",
    )

private val previewSettings =
    AdvancedSettings(
        present = everyKey,
        activityLogRetentionDays = 30,
        libraryMonitorDelay = 60,
        libraryUpdateDuration = 30,
        slowResponseWarning = true,
        slowResponseThresholdMs = 500,
        corsHosts = listOf("*"),
        castReceivers = listOf(CastReceiver("CC1AD845", "Default Media Receiver")),
        cacheSize = 800,
        legacyAuthorization = true,
    )

private val previewReady = AdvancedUiState.Ready(previewSettings)

/** Advanced: the page across the matrix, an older server's, each sheet, then each other state. */
class AdvancedScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun advanced() = AdvancedScreen(previewReady, AdvancedActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun olderServer() =
        AdvancedScreen(
            AdvancedUiState.Ready(previewSettings.copy(present = everyKey - "CacheSize" - "EnableLegacyAuthorization")),
            AdvancedActions(),
            AdvancedGates(cacheAndLegacyAuth = false),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun oldestServer() =
        AdvancedScreen(
            AdvancedUiState.Ready(previewSettings),
            AdvancedActions(),
            AdvancedGates(performance = false, cacheAndLegacyAuth = false),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraScreenStateLightPreview
    @EphyraFontScalePreviews
    @Composable
    fun number() = Surface { NumberForm(AdvancedNumber.RetentionDays, previewSettings, AdvancedActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun hosts() = Surface { ListForm(AdvancedList.CorsHosts, previewSettings, AdvancedActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun castReceivers() = Surface { ListForm(AdvancedList.CastReceivers, previewSettings, AdvancedActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun saving() = AdvancedScreen(previewReady.copy(save = SaveProgress(SaveStatus.Saving)), AdvancedActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun unsent() = AdvancedScreen(previewReady.copy(save = SaveProgress(SaveStatus.Idle, failed = true)), AdvancedActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = AdvancedScreen(AdvancedUiState.Loading, AdvancedActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = AdvancedScreen(AdvancedUiState.Failed(ConsoleFailure.Refused), AdvancedActions())
}
