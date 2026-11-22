package com.cooper.homelab.ephyra.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.core.flags.Flags
import com.cooper.homelab.core.flags.SharedFlags
import com.cooper.homelab.core.presentation.search.SettingsSearchState
import com.cooper.homelab.core.presentation.tip.TipState
import com.cooper.homelab.core.tip.TIP_STEPS
import com.cooper.homelab.core.uiandroid.flags.LocalFlags
import com.cooper.homelab.core.uiandroid.search.SettingsSearchScreen
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.preview.JellyfinHomeLayoutPreviews
import com.cooper.homelab.ephyra.server.ForgotPasswordAction
import com.cooper.homelab.ephyra.ui.console.ConsolePage
import com.cooper.homelab.ephyra.ui.search.available
import com.cooper.homelab.ephyra.ui.search.settingsIndex

/** The phone screens: each step's layout across the matrix, then every other state on the phone cell alone. */
class JellyfinScreenshotTest {
    // The server step.

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun serverFound() = EphyraRoot(previewEnterServerFound, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun serverEmpty() = EphyraRoot(previewEnterServerEmpty, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun serverSearching() = EphyraRoot(previewEnterServerSearching, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun serverUnreachable() = EphyraRoot(previewEnterServerError, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun serverLocalNetworkDenied() = EphyraRoot(previewEnterServerLocalNetwork, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun serverInvalidAddress() = EphyraRoot(previewEnterServerInvalid, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun serverNotJellyfin() = EphyraRoot(previewEnterServerNotJellyfin, Actions())

    /** A public plain-HTTP address: the warning and the agreement beside it, with Connect waiting. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun serverInsecure() = EphyraRoot(previewEnterServerInsecure, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun serverInsecureAgreed() = EphyraRoot(previewEnterServerInsecureAgreed, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun serverCleartextRefused() = EphyraRoot(previewEnterServerCleartextRefused, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun serverClientCertificate() = EphyraRoot(previewEnterServerClientCertificate, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun serverGenericError() = EphyraRoot(previewEnterServerGeneric, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = EphyraRoot(UiState.Loading, Actions())

    // Sign-in.

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @EphyraSpanishFontScalePreviews
    @Composable
    fun signIn() = EphyraRoot(previewSignIn, Actions())

    /** A server that has not finished its first-run setup: the notice stands where the sign-in would. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun setupIncomplete() = EphyraRoot(previewSetupIncomplete, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun signInUsers() = EphyraRoot(previewSignInUsers, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun signInUsersPictures() = EphyraRoot(previewSignInUsersPictures, Actions())

    // The setup wizard of a server that has not finished its first-run setup.

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun startup() = EphyraRoot(previewStartup, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun startupAccount() = EphyraRoot(previewStartupAccountFilled, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupAccountEmpty() = EphyraRoot(previewStartupAccount, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupAccountMismatch() = EphyraRoot(previewStartupAccountMismatch, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupAccountSaving() = EphyraRoot(previewStartupAccountSaving, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupAccountRefused() = EphyraRoot(previewStartupAccountRefused, Actions())

    /** The server refused the account and said why: its own words stand in place of the generic sentence. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupAccountReason() = EphyraRoot(previewStartupAccountReason, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupNoName() = EphyraRoot(previewStartupNoName, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupSaving() = EphyraRoot(previewStartupSaving, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupRefused() = EphyraRoot(previewStartupRefused, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupLoading() = EphyraRoot(previewStartupLoading, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupFailed() = EphyraRoot(previewStartupFailed, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun startupFinish() = EphyraRoot(previewStartupFinish, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupFinishPorts() = EphyraRoot(previewStartupFinishPorts, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupFinishOff() = EphyraRoot(previewStartupFinishOff, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupFinishSaving() = EphyraRoot(previewStartupFinishSaving, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun startupFinishRefused() = EphyraRoot(previewStartupFinishRefused, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signInHttps() = EphyraRoot(previewSignInHttps, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signInWrongPassword() = EphyraRoot(previewSignInWrongPassword, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun quickConnectCode() = QuickConnectCodeBody(requireNotNull(previewQuickConnect.quickConnectCode), Actions())

    /** The sign-in step on its Quick Connect tab, before a code is asked for, with the tab labels at large font and in Spanish. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @EphyraSpanishFontScalePreviews
    @Composable
    fun quickConnectTab() = EphyraRoot(previewQuickConnectTab, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun quickConnectOff() = EphyraRoot(previewQuickConnectOff, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun quickConnectNotOffered() = EphyraRoot(previewQuickConnectNotOffered, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun quickConnectExpired() = EphyraRoot(previewQuickConnectExpired, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun quickConnectGetting() = EphyraRoot(previewQuickConnectGetting, Actions())

    /** A failure that isn't the password's is said under Sign in, and the field stays unmarked. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun signInUnreachable() = EphyraRoot(previewSignInUnreachable, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signInLocalNetworkDenied() = EphyraRoot(previewSignInLocalNetwork, Actions(localNetworkAsked = true))

    // Signed in.

    @PreviewTest
    @JellyfinHomeLayoutPreviews
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun signedIn() = EphyraRoot(previewSignedIn, Actions())

    /** An administrator of a server with no libraries: the card above the server group. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun signedInNoLibraries() = EphyraRoot(previewSignedInNoLibraries, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signedInChecking() =
        CompositionLocalProvider(LocalFlags provides Flags.of(SharedFlags.BingePromotion to true)) {
            EphyraRoot(previewSignedInChecking, Actions())
        }

    /** The saved server has not answered yet: nothing of the home, only the wait, until it does or its problem replaces it. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signedInWaiting() = EphyraRoot(previewSignedInWaiting, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signedInFailed() =
        CompositionLocalProvider(LocalFlags provides Flags.of(SharedFlags.BingePromotion to true)) {
            EphyraRoot(previewSignedInFailed, Actions())
        }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun signedInCouldNotLoad() = EphyraRoot(previewSignedInCouldNotLoad, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun signedInClientCertificate() = EphyraRoot(previewSignedInClientCertificate, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun signedInLocalNetworkDenied() = EphyraRoot(previewSignedInLocalNetworkDenied, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signedInLocalNetworkAsked() = EphyraRoot(previewSignedInLocalNetworkDenied, Actions(localNetworkAsked = true))

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signedInRetrying() = EphyraRoot(previewSignedInRetrying, Actions())

    @PreviewTest
    @EphyraFontScalePreviews
    @Composable
    fun signedInFailedLarge() = EphyraRoot(previewSignedInFailed, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signedInHttps() = EphyraRoot(previewSignedInHttps, Actions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun signedInAdmin() = EphyraRoot(previewSignedInAdmin, Actions())

    /** The Live TV row, for an administrator whose policy lets them manage it. A user who is not an administrator is offered none. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signedInAdminLiveTv() = EphyraRoot(previewSignedInAdminLiveTv, Actions())

    /** The server card with three pills at once, on an unencrypted address: an administrator's, at every size and in Spanish. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun serverCardAlerts() = EphyraRoot(previewSignedInAdminAlerts, Actions())

    /** The note under the server card for the titles with no TMDB id: plain, at every size and in Spanish. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun noTmdbIds() = EphyraRoot(previewSignedInNoTmdb, Actions())

    /** The same note with the Binge flag on, which names Binge. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun noTmdbIdsPromoted() =
        CompositionLocalProvider(LocalFlags provides Flags.of(SharedFlags.BingePromotion to true)) {
            EphyraRoot(previewSignedInNoTmdb, Actions())
        }

    /** The card before its figures arrive: a placeholder in each, and no Connected. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun serverCardLoading() = EphyraRoot(previewSignedInCardLoading, Actions())

    /** An administrator's account, behind the avatar: the rows everyone else has inline on their home. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun account() = AccountScreen(previewSignedInAdmin, Actions(), onBack = {})

    /** The This app page, behind the home's This app row: what the app reports, feedback, and About. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun thisApp() = ThisAppScreen(Actions(), onBack = {})

    /** An administrator's This app page with the notifications switch off: only the switch. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun thisAppNotificationsOff() = ThisAppScreen(Actions(notifications = NotificationSettings()), onBack = {})

    /** With the switch on: the cadence, and the system warnings that apply, both here. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun thisAppNotificationsOn() =
        ThisAppScreen(
            Actions(notifications = NotificationSettings(enabled = true, intervalMinutes = 30, blocked = true, unrestricted = false)),
            onBack = {},
        )

    /** On, with nothing in Android's way: the switch and the cadence alone. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun thisAppNotificationsClear() = ThisAppScreen(Actions(notifications = NotificationSettings(enabled = true)), onBack = {})

    /** The Check every choices, drawn in place because a frame can't capture the sheet's own window. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun notificationsInterval() =
        Surface { IntervalChoices(NotificationSettings(enabled = true, intervalMinutes = 120), Actions(), close = {}) }

    /** The settings search before a query, with results (each with where it is) and with none. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun searchPrompt() = SearchPage()

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun searchResults() = SearchPage(initialQuery = "port")

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun searchNoMatch() = SearchPage(initialQuery = "zzz")

    /** The search before a query, with what the user opened last and its Clear. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun searchRecent() =
        SearchPage(
            recentPages = listOf(ConsolePage.Network, ConsolePage.Transcoding, ConsolePage.Playback),
        )

    /** The This app page with the tip jar priced, so its row shows. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun thisAppWithTipJar() = ThisAppScreen(Actions(), onBack = {}, tip = TipState.Ready(TIP_STEPS.associateWith { "£$it" }))

    /** The sign-out confirm's body: a screen frame can't capture the dialog's own window. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun signOutConfirm() = Surface { SignOutConfirm(Actions(), inWindow = false) }

    /** The confirm for taking back the agreement to plain HTTP, whose server is named in its body. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun takeBackConfirm() = Surface { TakeBackConfirm("media.example.com", Actions(), inWindow = false) }

    /** With the Binge flag on: the Library group shows what Binge can see. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun signedInPromoted() =
        CompositionLocalProvider(LocalFlags provides Flags.of(SharedFlags.BingePromotion to true)) {
            EphyraRoot(previewSignedIn, Actions())
        }

    /** With the Binge flag on, signing out says what Binge stops doing. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun signOutConfirmPromoted() =
        CompositionLocalProvider(LocalFlags provides Flags.of(SharedFlags.BingePromotion to true)) {
            Surface { SignOutConfirm(Actions(), inWindow = false) }
        }

    // Forgot password: each step of the reset.

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun forgotContactAdmin() =
        Surface { ForgotPasswordOverlay(ForgotPasswordStep.Told(ForgotPasswordAction.ContactAdmin), null, inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun forgotInNetworkRequired() =
        Surface { ForgotPasswordOverlay(ForgotPasswordStep.Told(ForgotPasswordAction.InNetworkRequired), null, inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun forgotPin() =
        Surface { ForgotPasswordOverlay(ForgotPasswordStep.EnterPin("/config/data/passwordreset.json"), null, inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun forgotPinWrong() =
        Surface {
            ForgotPasswordOverlay(
                ForgotPasswordStep.EnterPin("/config/data/passwordreset.json", wrongPin = true),
                null,
                inWindow = false,
            )
        }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun forgotFailed() = Surface { ForgotPasswordOverlay(ForgotPasswordStep.Failed, null, inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun forgotDone() = Surface { ForgotPasswordOverlay(ForgotPasswordStep.Done, null, inWindow = false) }
}

/** The shared search page over this app's real index, on the pinned bar the app opens it with, as an administrator on a current server sees it. */
@Composable
private fun SearchPage(
    initialQuery: String = "",
    recentPages: List<ConsolePage> = emptyList(),
) {
    val resources = LocalContext.current.resources
    val entries = remember(resources) { settingsIndex(resources) }
    val profile = previewSignedInAdmin.profile
    val account = previewSignedInAdmin.account
    SettingsSearchScreen(
        state =
            SettingsSearchState.Ready(
                available = { it.available(profile, account) },
                recent = entries.filter { it.target.page in recentPages && it.pageLabel == null }.distinctBy { it.target.page },
                viewer = Unit,
            ),
        entries = entries,
        icon = { it.target.icon },
        onOpen = { _, _ -> },
        onBack = {},
        onRetry = {},
        onClearRecent = {},
        initialQuery = initialQuery,
        pinnedBar = true,
    )
}
