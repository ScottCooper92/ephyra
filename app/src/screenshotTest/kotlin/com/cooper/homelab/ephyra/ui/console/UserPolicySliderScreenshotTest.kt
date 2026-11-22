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
private fun SessionsSheet(sessions: Int?) {
    val resources = LocalContext.current.resources
    Surface {
        NumberSliderContent(
            title = stringResource(R.string.users_max_sessions),
            value = sessions,
            range = 1..20,
            format = { resources.getQuantityString(R.plurals.users_sessions_at_once, it, it) },
            openEndLabel = stringResource(R.string.users_no_limit),
            onSave = {},
        )
    }
}

@Composable
private fun BitrateSheet(kilobits: Int?) {
    val resources = LocalContext.current.resources
    Surface {
        NumberSliderContent(
            title = stringResource(R.string.users_bitrate),
            value = kilobits,
            range = 1_000..100_000,
            format = { bitrateText(resources, it * 1_000) },
            openEndLabel = stringResource(R.string.users_no_limit),
            onSave = {},
            step = 1_000,
        )
    }
}

@Composable
private fun LockoutSheet(attempts: Int?) {
    val resources = LocalContext.current.resources
    Surface {
        NumberSliderContent(
            title = stringResource(R.string.users_lockout),
            value = attempts,
            range = 1..20,
            format = { lockoutText(resources, it) },
            openEndLabel = stringResource(R.string.users_lockout_never),
            onSave = {},
        )
    }
}

/** The three sliders of a user's policy: sessions at once, the bitrate limit away from home, and the sign-in lockout. */
class UserPolicySliderScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun sessionsThree() = SessionsSheet(3)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun sessionsNoLimit() = SessionsSheet(null)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun bitrateEight() = BitrateSheet(8_000)

    /** A stored 2.5 Mbps sits between two steps and reads as stored. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun bitrateBetweenSteps() = BitrateSheet(2_500)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun bitrateNoLimit() = BitrateSheet(null)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun lockoutFive() = LockoutSheet(5)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun lockoutNever() = LockoutSheet(null)
}
