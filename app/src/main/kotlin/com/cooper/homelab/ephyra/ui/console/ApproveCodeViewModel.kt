package com.cooper.homelab.ephyra.ui.console

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooper.homelab.core.review.NoReviewSignals
import com.cooper.homelab.core.review.ReviewSignalSink
import com.cooper.homelab.ephyra.server.Connection
import com.cooper.homelab.ephyra.server.JellyfinHttpException
import com.cooper.homelab.ephyra.server.authorizeQuickConnect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** How an approval went, when it did not leave the field to say it: each is a snackbar, once. */
enum class ApproveOutcome { Approved, QuickConnectOff, Unreachable, Failed }

data class ApproveUiState(
    val code: String = "",
    val approving: Boolean = false,
    /** The server is not waiting on this code: the field says so, until the code changes. */
    val wrongCode: Boolean = false,
) {
    /** Jellyfin's codes are six digits; Approve waits for all six. */
    val complete: Boolean get() = code.length == CODE_LENGTH
}

internal const val CODE_LENGTH = 6

/**
 * Approving a Quick Connect code another device shows (a TV, a browser), which signs that device in
 * as the user. For everyone: Jellyfin only asks that the caller is signed in.
 */
class ApproveCodeViewModel(
    private val connection: Connection,
    private val reviewSignals: ReviewSignalSink = NoReviewSignals,
) : ViewModel() {
    private val _state = MutableStateFlow(ApproveUiState())
    val state: StateFlow<ApproveUiState> = _state.asStateFlow()

    private val _events = Channel<ApproveOutcome>(Channel.BUFFERED)

    /** What the snackbar says, once each: a recreated screen collects only what comes next. */
    val events: Flow<ApproveOutcome> = _events.receiveAsFlow()

    /** Keeps digits only, at most six; a new code clears the field's error. */
    fun onCode(value: String) = _state.update { it.copy(code = value.filter(Char::isDigit).take(CODE_LENGTH), wrongCode = false) }

    fun approve() {
        val current = _state.value
        if (!current.complete || current.approving) return
        _state.value = current.copy(approving = true, wrongCode = false)
        viewModelScope.launch {
            val outcome =
                connection
                    .consoleCall(TAG) { _, client -> client.authorizeQuickConnect(current.code) }
                    .fold(onSuccess = { ApproveOutcome.Approved }, onFailure = { it.approveOutcome() })
            if (outcome == ApproveOutcome.Approved) reviewSignals.recordPositiveAction()
            // An approved code is spent, so the field empties for the next one.
            _state.update {
                it.copy(approving = false, wrongCode = outcome == null, code = if (outcome == ApproveOutcome.Approved) "" else it.code)
            }
            if (outcome != null) _events.trySend(outcome)
        }
    }

    private companion object {
        const val TAG = "JellyfinApprove"
    }
}

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_NOT_FOUND = 404

/**
 * A 404 is a code the server isn't waiting on, which the field says and no outcome does (null), and a 401 with the session still good is Quick
 * Connect turned off. A 401 that is the token's was reported by [consoleCall], and the page closes.
 */
private fun Throwable.approveOutcome(): ApproveOutcome? =
    when {
        this !is JellyfinHttpException -> ApproveOutcome.Unreachable
        code == HTTP_NOT_FOUND -> null
        code == HTTP_UNAUTHORIZED -> ApproveOutcome.QuickConnectOff
        else -> ApproveOutcome.Failed
    }
