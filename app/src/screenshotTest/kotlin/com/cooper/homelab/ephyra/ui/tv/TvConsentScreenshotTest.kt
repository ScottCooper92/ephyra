package com.cooper.homelab.ephyra.ui.tv

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraTvScreenPreviews
import com.cooper.homelab.ephyra.preview.EphyraTvStressPreviews
import com.cooper.homelab.ephyra.ui.Actions
import com.cooper.homelab.ephyra.ui.previewSignedIn

/** The same question on the television, its main state at large fonts and in Spanish too. */
class TvConsentScreenshotTest {
    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun consent() = TvConsentScreen(onChoice = {})

    /** The board with Report a bug focused: its pane is the code the phone scans. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun reportBug() = TvEphyraRoot(previewSignedIn, Actions(), describing = REPORT_BUG_KEY)

    /** The board with Share usage data focused, both switches in their default off state. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun thisApp() = TvEphyraRoot(previewSignedIn, Actions(), describing = USAGE_DATA_KEY)
}
