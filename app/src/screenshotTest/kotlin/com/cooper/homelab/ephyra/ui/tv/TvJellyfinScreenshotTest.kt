package com.cooper.homelab.ephyra.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.SaveStatus
import com.binge.designsystem.tv.component.TvButton
import com.cooper.homelab.core.flags.Flags
import com.cooper.homelab.core.flags.SharedFlags
import com.cooper.homelab.core.uiandroid.flags.LocalFlags
import com.cooper.homelab.ephyra.preview.EphyraTvScreenPreviews
import com.cooper.homelab.ephyra.preview.EphyraTvStressPreviews
import com.cooper.homelab.ephyra.server.Culture
import com.cooper.homelab.ephyra.server.ForgotPasswordAction
import com.cooper.homelab.ephyra.server.PlaybackPreferences
import com.cooper.homelab.ephyra.server.SubtitleMode
import com.cooper.homelab.ephyra.ui.Actions
import com.cooper.homelab.ephyra.ui.ForgotPasswordStep
import com.cooper.homelab.ephyra.ui.UiState
import com.cooper.homelab.ephyra.ui.console.ConsoleFailure
import com.cooper.homelab.ephyra.ui.console.PlaybackActions
import com.cooper.homelab.ephyra.ui.console.PlaybackUiState
import com.cooper.homelab.ephyra.ui.console.SaveProgress
import com.cooper.homelab.ephyra.ui.previewAllowLocalNetwork
import com.cooper.homelab.ephyra.ui.previewEnterServerCleartextRefused
import com.cooper.homelab.ephyra.ui.previewEnterServerClientCertificate
import com.cooper.homelab.ephyra.ui.previewEnterServerError
import com.cooper.homelab.ephyra.ui.previewEnterServerFound
import com.cooper.homelab.ephyra.ui.previewEnterServerInsecure
import com.cooper.homelab.ephyra.ui.previewEnterServerNotJellyfin
import com.cooper.homelab.ephyra.ui.previewEnterServerSearching
import com.cooper.homelab.ephyra.ui.previewQuickConnect
import com.cooper.homelab.ephyra.ui.previewQuickConnectExpired
import com.cooper.homelab.ephyra.ui.previewQuickConnectGetting
import com.cooper.homelab.ephyra.ui.previewQuickConnectNotOffered
import com.cooper.homelab.ephyra.ui.previewQuickConnectOff
import com.cooper.homelab.ephyra.ui.previewSetupIncomplete
import com.cooper.homelab.ephyra.ui.previewSignIn
import com.cooper.homelab.ephyra.ui.previewSignInUsers
import com.cooper.homelab.ephyra.ui.previewSignInUsersPictures
import com.cooper.homelab.ephyra.ui.previewSignInWrongPassword
import com.cooper.homelab.ephyra.ui.previewSignedIn
import com.cooper.homelab.ephyra.ui.previewSignedInCertificateRefused
import com.cooper.homelab.ephyra.ui.previewSignedInChecking
import com.cooper.homelab.ephyra.ui.previewSignedInClientCertificate
import com.cooper.homelab.ephyra.ui.previewSignedInCouldNotLoad
import com.cooper.homelab.ephyra.ui.previewSignedInFailed
import com.cooper.homelab.ephyra.ui.previewSignedInLocalNetworkDenied
import com.cooper.homelab.ephyra.ui.previewSignedInWaiting
import com.cooper.homelab.ephyra.ui.tv.ACCOUNT_KEY
import com.cooper.homelab.ephyra.ui.tv.AUDIO_KEY
import com.cooper.homelab.ephyra.ui.tv.AUTOPLAY_KEY
import com.cooper.homelab.ephyra.ui.tv.TvUserPicture

/** The television's screens: each step's main state in Spanish and at large fonts too, every other state once. */
class TvJellyfinScreenshotTest {
    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun serverFound() = TvEphyraRoot(previewEnterServerFound, Actions())

    /** The address error is one line on TV, so its longest wording is checked in Spanish and at large fonts. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun serverNotJellyfin() = TvEphyraRoot(previewEnterServerNotJellyfin, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun serverUnreachable() = TvEphyraRoot(previewEnterServerError, Actions())

    /** A public plain-HTTP address: the agreement under the field, with its reason beside it. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun serverInsecure() = TvEphyraRoot(previewEnterServerInsecure, Actions())

    /** The refusal's wording is the longest address error on TV, so it is checked in Spanish and at large fonts. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun serverCleartextRefused() = TvEphyraRoot(previewEnterServerCleartextRefused, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun serverClientCertificate() = TvEphyraRoot(previewEnterServerClientCertificate, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun loading() = TvEphyraRoot(UiState.Loading, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun serverSearching() = TvEphyraRoot(previewEnterServerSearching, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun quickConnectCode() = TvEphyraRoot(previewQuickConnect, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun setupIncomplete() = TvEphyraRoot(previewSetupIncomplete, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun quickConnectGetting() = TvEphyraRoot(previewQuickConnectGetting, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun quickConnectAfterWrongPassword() = TvEphyraRoot(previewSignInWrongPassword, Actions())

    /** A code the server let go of: its own wording, with Try again where the code was. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun quickConnectExpired() = TvEphyraRoot(previewQuickConnectExpired, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun quickConnectOff() = TvEphyraRoot(previewQuickConnectOff, Actions())

    /** Quick Connect is off on the server, so the step opens on the password with no code behind it. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun quickConnectNotOffered() = TvEphyraRoot(previewQuickConnectNotOffered, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun password() = TvSignInPage(previewSignIn, Actions(), startWithPassword = true)

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun passwordWithUsers() = TvSignInPage(previewSignInUsers, Actions(), startWithPassword = true)

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun passwordWithPictures() = TvSignInPage(previewSignInUsersPictures, Actions(), startWithPassword = true)

    /**
     * A listed account's button focused, which fills the pill: with a picture and with the initials inside it, the two things its
     * leading slot holds, and at large fonts too.
     */
    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun accountButtonFocused() =
        Column(modifier = Modifier.padding(48.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            val picture = previewSignInUsersPictures.publicPictures.values.first()
            TvButton(label = "Mum", onClick = {}, initiallyFocused = true, leadingContent = { TvUserPicture("Mum", picture) })
            TvButton(label = "Dad", onClick = {}, initiallyFocused = true, leadingContent = { TvUserPicture("Dad", null) })
            TvButton(label = "Kid", onClick = {}, leadingContent = { TvUserPicture("Kid", picture) })
        }

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun passwordWrong() = TvSignInPage(previewSignInWrongPassword, Actions(), startWithPassword = true)

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun forgotContactAdmin() =
        TvSignInPage(
            previewSignIn.copy(forgot = ForgotPasswordStep.Told(ForgotPasswordAction.ContactAdmin)),
            Actions(),
            startWithPassword = true,
        )

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun forgotInNetworkRequired() =
        TvSignInPage(
            previewSignIn.copy(forgot = ForgotPasswordStep.Told(ForgotPasswordAction.InNetworkRequired)),
            Actions(),
            startWithPassword = true,
        )

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun forgotPin() =
        TvSignInPage(
            previewSignIn.copy(forgot = ForgotPasswordStep.EnterPin("/config/data/passwordreset.json")),
            Actions(),
            startWithPassword = true,
        )

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun forgotPinWrong() =
        TvSignInPage(
            previewSignIn.copy(forgot = ForgotPasswordStep.EnterPin("/config/data/passwordreset.json", wrongPin = true)),
            Actions(),
            startWithPassword = true,
        )

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun forgotFailed() = TvSignInPage(previewSignIn.copy(forgot = ForgotPasswordStep.Failed), Actions(), startWithPassword = true)

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun forgotDone() =
        TvSignInPage(previewSignIn.copy(forgot = ForgotPasswordStep.Done, password = "A1B2-C3D4"), Actions(), startWithPassword = true)

    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun signedIn() = TvEphyraRoot(previewSignedIn, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun signedInChecking() =
        CompositionLocalProvider(LocalFlags provides Flags.of(SharedFlags.BingePromotion to true)) {
            TvEphyraRoot(previewSignedInChecking, Actions())
        }

    /** The saved server has not answered yet: its row says it is checking, and only the way out is offered. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun signedInWaiting() =
        CompositionLocalProvider(LocalFlags provides Flags.of(SharedFlags.BingePromotion to true)) {
            TvEphyraRoot(previewSignedInWaiting, Actions())
        }

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun signedInFailed() =
        CompositionLocalProvider(LocalFlags provides Flags.of(SharedFlags.BingePromotion to true)) {
            TvEphyraRoot(previewSignedInFailed, Actions())
        }

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun signedInCouldNotLoad() = TvEphyraRoot(previewSignedInCouldNotLoad, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun signedInClientCertificate() = TvEphyraRoot(previewSignedInClientCertificate, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun signedInCertificateRefused() = TvEphyraRoot(previewSignedInCertificateRefused, Actions())

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun signedInLocalNetworkDenied() = TvEphyraRoot(previewSignedInLocalNetworkDenied, Actions(localNetwork = previewAllowLocalNetwork))

    /** With the Binge flag on: the library check, with what Binge can see, leads the page. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun signedInPromoted() =
        CompositionLocalProvider(LocalFlags provides Flags.of(SharedFlags.BingePromotion to true)) {
            TvEphyraRoot(previewSignedIn, Actions())
        }

    private val cultures = listOf(Culture("English", "en", listOf("eng")), Culture("French", "fr", listOf("fre", "fra")))
    private val prefs = PlaybackPreferences(audio = "eng", subtitles = "", subtitleMode = SubtitleMode.Default)

    @Composable
    private fun withPlayback(
        playback: PlaybackUiState,
        describing: String = ACCOUNT_KEY,
        changed: String? = null,
    ) = TvEphyraRoot(previewSignedIn, Actions(), playback = TvPlayback(playback, PlaybackActions(), changed), describing = describing)

    /** The user's own playback settings follow the account on the signed-in page. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun playback() = withPlayback(PlaybackUiState.Ready(prefs, cultures))

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun playbackSaved() = withPlayback(PlaybackUiState.Ready(prefs, cultures, SaveProgress(SaveStatus.Saved)), AUDIO_KEY, AUDIO_KEY)

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun playbackSaveFailed() = withPlayback(PlaybackUiState.Ready(prefs, cultures, SaveProgress(failed = true)), AUDIO_KEY, AUDIO_KEY)

    /** A language row's pane: Any language, then the languages, the one in use ticked. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @EphyraTvStressPreviews
    @Composable
    fun playbackLanguages() = withPlayback(PlaybackUiState.Ready(prefs, cultures), AUDIO_KEY)

    /** A switch's pane: its one option, ticked while it is on. */
    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun playbackSwitch() = withPlayback(PlaybackUiState.Ready(prefs, cultures), AUTOPLAY_KEY)

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun playbackLoading() = withPlayback(PlaybackUiState.Loading)

    @PreviewTest
    @EphyraTvScreenPreviews
    @Composable
    fun playbackFailed() = withPlayback(PlaybackUiState.Failed(ConsoleFailure.Unreachable))
}
