package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStateLightPreview
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.ApiKey

private const val NOW = 1_793_534_400_000L // 31 Oct 2026, 12:00 UTC
private const val DAY = 24 * 60 * 60 * 1000L

private val previewKeys =
    listOf(
        ApiKey("0f3a9c2e7b1d4a68b5c0e91d2f7a3b4c", "Home Assistant", NOW - 2 * DAY),
        ApiKey("9d81e4a7c3f2406bb1d5e8a07c92f6d3", "Sonarr", NOW - 20 * DAY),
        ApiKey("c47b0e91a2d3485fa6e1d90b3c58f27a", "", NOW - 90 * DAY),
    )

private val previewReady = ApiKeysUiState.Ready(previewKeys)

/** API keys: the list across the matrix, then the dialogs (new, shown once, revoke), a change failed, empty and the read states. */
class ApiKeysScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun apiKeys() = ApiKeysScreen(previewReady, ApiKeysActions(), now = NOW)

    /** A pull in progress: the list stays, with the pull spinner over it. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun refreshing() = ApiKeysScreen(previewReady.copy(refreshing = true), ApiKeysActions(), now = NOW)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraScreenStateLightPreview
    @EphyraFontScalePreviews
    @Composable
    fun naming() = Surface { NewKeySheet(ApiKeysActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun namingSubmitting() = Surface { NewKeySheet(ApiKeysActions(), submitting = true, inWindow = false) }

    /** The form stays open with what was typed, and says why in its own error slot. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraScreenStateLightPreview
    @EphyraFontScalePreviews
    @Composable
    fun namingFailed() =
        Surface { NewKeySheet(ApiKeysActions(), error = "Not changed. Something went wrong. Try again.", inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun created() = Surface { CreatedKeyDialog(previewKeys.first(), ApiKeysActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun revoking() = Surface { RevokeDialog(previewKeys[1], ApiKeysActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraScreenStateLightPreview
    @EphyraSpanishPreviews
    @Composable
    fun empty() = ApiKeysScreen(ApiKeysUiState.Ready(emptyList()), ApiKeysActions(), now = NOW)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = ApiKeysScreen(ApiKeysUiState.Loading, ApiKeysActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun readFailed() = ApiKeysScreen(ApiKeysUiState.Failed(ConsoleFailure.Refused), ApiKeysActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun readFailedWithReason() =
        ApiKeysScreen(ApiKeysUiState.Failed(ConsoleFailure.refused("The database is locked by another process.")), ApiKeysActions())
}
