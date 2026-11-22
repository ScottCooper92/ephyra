package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.R
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews

/** A refusal whose server reason is too long for a snackbar: its lead as the title, the whole reason, and OK. */
class RefusedDialogScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun longReason() =
        Surface {
            RefusedDialog(
                title = stringResource(R.string.settings_not_saved, "").trim().trimEnd('.'),
                reason = "Library path /mnt/media/Movies does not exist or is not readable by the server.",
                onDismiss = {},
                inWindow = false,
            )
        }
}
