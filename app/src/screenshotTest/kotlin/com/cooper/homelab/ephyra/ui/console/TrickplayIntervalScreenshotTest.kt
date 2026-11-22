package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.R
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews

@Composable
private fun IntervalSheet(millis: Int) {
    val resources = LocalContext.current.resources
    Surface {
        NumberSliderContent(
            title = stringResource(R.string.trickplay_interval),
            value = millis,
            range = 1_000..60_000,
            format = { intervalText(resources, it) },
            openEndLabel = null,
            onSave = {},
            step = 1_000,
        )
    }
}

/** The trickplay interval's sheet: a whole number of seconds, one between two seconds, and one beyond a minute. */
class TrickplayIntervalScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun intervalTen() = IntervalSheet(10_000)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun intervalBetweenSteps() = IntervalSheet(2_500)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun intervalBeyondScale() = IntervalSheet(90_000)
}
