package com.cooper.homelab.ephyra.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStateLightPreview
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews

/** The About group: the app's version, the privacy policy, the licences and the beta, as the home and the account show it. */
class AboutGroupScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraScreenStateLightPreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun about() = Surface { AboutGroup(Actions()) }
}
