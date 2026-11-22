package com.cooper.homelab.ephyra.ui.console

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews

/** Approving a Quick Connect code: the page across the matrix, then each result on the phone cell. */
class ApproveCodeScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun typed() = ApproveCodeScreen(ApproveUiState(code = "482913"), ApproveActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun empty() = ApproveCodeScreen(ApproveUiState(), ApproveActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun approved() = ApproveCodeScreen(ApproveUiState(result = ApproveResult.Approved), ApproveActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun wrongCode() = ApproveCodeScreen(ApproveUiState(code = "111111", result = ApproveResult.WrongCode), ApproveActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = ApproveCodeScreen(ApproveUiState(code = "111111", result = ApproveResult.Failed), ApproveActions())
}
