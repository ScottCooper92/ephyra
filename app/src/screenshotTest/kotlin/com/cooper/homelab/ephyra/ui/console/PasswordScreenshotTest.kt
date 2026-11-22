package com.cooper.homelab.ephyra.ui.console

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews

private val previewFilled = PasswordUiState(current = "old-secret", new = "new-secret", confirm = "new-secret")

/** Changing the password: the filled form across the matrix, then the two results a field shows on the phone cell. */
class PasswordScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun filled() = PasswordScreen(previewFilled, PasswordActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun empty() = PasswordScreen(PasswordUiState(), PasswordActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun wrongCurrent() = PasswordScreen(previewFilled.copy(result = PasswordResult.WrongCurrent), PasswordActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun mismatch() = PasswordScreen(previewFilled.copy(confirm = "new-secert", result = PasswordResult.Mismatch), PasswordActions())
}
