package com.cooper.homelab.ephyra.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cooper.homelab.core.presentation.connection.AutoRetryPolicy
import com.cooper.homelab.core.presentation.connection.ConnectionProblem
import com.cooper.homelab.core.uiandroid.connect.AllowLocalNetwork
import com.cooper.homelab.core.uiandroid.connection.ConnectionAutoRetryEffect
import com.cooper.homelab.core.uiandroid.connection.ConnectionProblemScreen
import com.cooper.homelab.core.uiandroid.R as CoreR

/**
 * The saved server did not answer, answered with something the app could not use, or is on the local network and Android
 * has not let the app reach it: the home gives way to the shared library's problem screen, with its ways out. The words,
 * the retry rule and the spinner are the library's; the sign-out button is this app's, because its confirmation is.
 */
@Composable
internal fun ConnectionProblem(
    state: UiState.SignedIn,
    actions: Actions,
    modifier: Modifier = Modifier,
) {
    val problem = state.health.problem() ?: return
    ConnectionAutoRetryEffect(problem, state.refreshing, actions.onRefresh, rememberRetryPolicy())
    val allow = actions.localNetwork
    ConnectionProblemScreen(
        problem = problem,
        rechecking = state.refreshing,
        localNetworkAsked = allow?.asked == true,
        onRetry = actions.onRefresh,
        onAllowLocalNetwork = allow?.run ?: {},
        onEditConnection = actions.onChangeServer,
        signOut = { SignOutButton(actions) },
        modifier = modifier,
    )
}

/** The retry rule both apps share, built once per screen so its effect is not restarted by a recomposition. */
@Composable
internal fun rememberRetryPolicy(): AutoRetryPolicy = remember { AutoRetryPolicy() }

/** The library's name for what is wrong with a saved server, or null where nothing is: it is still being asked, or it is fine. */
internal fun ConnectionHealth.problem(): ConnectionProblem? =
    when (this) {
        ConnectionHealth.Unreachable -> ConnectionProblem.Unreachable
        ConnectionHealth.CouldNotLoad -> ConnectionProblem.CouldNotLoad
        ConnectionHealth.LocalNetworkDenied -> ConnectionProblem.LocalNetworkDenied
        ConnectionHealth.ClientCertificateNeeded -> ConnectionProblem.ClientCertificateNeeded
        ConnectionHealth.Checking, ConnectionHealth.Healthy -> null
    }

/**
 * Whether the permission has been asked for, so the way to it is now the app's settings. The library's helper says it by its
 * label, which is what the problem screen is told as `localNetworkAsked`.
 */
internal val AllowLocalNetwork.asked: Boolean get() = label == CoreR.string.connect_local_network_open_settings

/** What the problem is called on the television, in the library's words. */
@StringRes
internal fun connectionHeadline(health: ConnectionHealth): Int =
    when (health) {
        ConnectionHealth.CouldNotLoad -> CoreR.string.connection_couldnt_load_headline
        ConnectionHealth.LocalNetworkDenied -> CoreR.string.connection_local_network_headline
        ConnectionHealth.ClientCertificateNeeded -> CoreR.string.connection_client_certificate_headline
        else -> CoreR.string.connection_unreachable_headline
    }

/** What to do about it on the television, in the library's words. */
@StringRes
internal fun connectionBody(health: ConnectionHealth): Int =
    when (health) {
        ConnectionHealth.CouldNotLoad -> CoreR.string.connection_couldnt_load_body
        ConnectionHealth.LocalNetworkDenied -> CoreR.string.connection_local_network_body
        ConnectionHealth.ClientCertificateNeeded -> CoreR.string.connection_client_certificate_body
        else -> CoreR.string.connection_unreachable_body
    }
