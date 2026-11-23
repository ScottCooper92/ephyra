package com.cooper.homelab.ephyra.ui.console

import androidx.lifecycle.ViewModel
import com.cooper.homelab.ephyra.server.Connection
import com.cooper.homelab.ephyra.server.JellyfinHttpException
import com.cooper.homelab.ephyra.server.changePassword
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/** What is wrong with a field: it says so under itself, until the next edit. */
enum class PasswordFieldError { WrongCurrent, Mismatch }

/** How a change went when it did not leave a field to say it: each is a snackbar, once. */
enum class PasswordOutcome { Changed, Refused, Unreachable }

data class PasswordUiState(
    val current: String = "",
    val new: String = "",
    val confirm: String = "",
    val changing: Boolean = false,
    val fieldError: PasswordFieldError? = null,
) {
    /** Change waits for the new password, twice. The current one may be empty: Jellyfin allows no password. */
    val complete: Boolean get() = new.isNotEmpty() && confirm.isNotEmpty()
}

/**
 * Changing the user's own password: the current one, the new one twice. The one console form with an
 * explicit submit, since a password is never saved a keystroke at a time.
 */
class PasswordViewModel(
    private val connection: Connection,
) : ViewModel() {
    private val _state = MutableStateFlow(PasswordUiState())
    val state: StateFlow<PasswordUiState> = _state.asStateFlow()

    private val _events = Channel<PasswordOutcome>(Channel.BUFFERED)

    /** What the snackbar says, once each: a recreated screen collects only what comes next. */
    val events: Flow<PasswordOutcome> = _events.receiveAsFlow()

    fun onCurrent(value: String) = _state.update { it.copy(current = value, fieldError = null) }

    fun onNew(value: String) = _state.update { it.copy(new = value, fieldError = null) }

    fun onConfirm(value: String) = _state.update { it.copy(confirm = value, fieldError = null) }

    fun change() {
        val form = _state.value
        if (!form.complete || form.changing) return
        if (form.new != form.confirm) {
            _state.value = form.copy(fieldError = PasswordFieldError.Mismatch)
            return
        }
        _state.value = form.copy(changing = true, fieldError = null)
        // On the connection's scope: a request already sent must finish if the page closes, so the result isn't lost.
        connection.launchSave {
            val outcome =
                connection
                    .consoleCall(TAG) { session, client -> client.changePassword(session.userId, form.current, form.new) }
                    .fold(onSuccess = { PasswordOutcome.Changed }, onFailure = { it.passwordOutcome() })
            _state.update {
                // A changed password leaves nothing in the form; a refused one keeps it to correct, and a wrong current one says so.
                when (outcome) {
                    PasswordOutcome.Changed -> PasswordUiState()
                    null -> it.copy(changing = false, fieldError = PasswordFieldError.WrongCurrent)
                    else -> it.copy(changing = false)
                }
            }
            if (outcome != null) _events.trySend(outcome)
        }
    }

    private companion object {
        const val TAG = "JellyfinPassword"
    }
}

private const val HTTP_FORBIDDEN = 403

/** A 403 is the current password being wrong, which the field says and no outcome does (null). */
private fun Throwable.passwordOutcome(): PasswordOutcome? =
    when {
        this is JellyfinHttpException && code == HTTP_FORBIDDEN -> null
        consoleFailure() == ConsoleFailure.Refused -> PasswordOutcome.Refused
        else -> PasswordOutcome.Unreachable
    }
