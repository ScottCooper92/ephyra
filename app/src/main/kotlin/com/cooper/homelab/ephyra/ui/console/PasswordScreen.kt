package com.cooper.homelab.ephyra.ui.console

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.binge.designsystem.component.BingeTextField
import com.binge.designsystem.component.PageIntro
import com.binge.designsystem.template.FormAction
import com.binge.designsystem.template.FormScreen
import com.binge.designsystem.template.ScreenBar
import com.cooper.homelab.ephyra.R
import com.binge.designsystem.R as DesR

/** What the password page can ask of its view model, and of the console. */
data class PasswordActions(
    val onCurrent: (String) -> Unit = {},
    val onNew: (String) -> Unit = {},
    val onConfirm: (String) -> Unit = {},
    val onChange: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * Changing the user's password: the current one, the new one twice, and Change in the footer. It says up front that other
 * devices are signed out. A wrong current password or two that don't match show on their field, and the rest of the
 * outcome is a snackbar: a password is never saved a keystroke at a time, so this is a form whose submit is the point.
 */
@Composable
fun PasswordScreen(
    state: PasswordUiState,
    actions: PasswordActions,
) {
    val host = remember { SnackbarHostState() }
    FormScreen(
        title = stringResource(R.string.password_title),
        bar = ScreenBar.Small,
        onBack = actions.onBack,
        snackbarHostState = host,
        primaryAction =
            FormAction(
                label = stringResource(R.string.password_action),
                onClick = actions.onChange,
                enabled = state.complete,
                busy = state.changing,
            ),
    ) { _ ->
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l))) {
            PageIntro(stringResource(R.string.password_body))
            BingeTextField(
                value = state.current,
                onValueChange = actions.onCurrent,
                label = stringResource(R.string.password_current),
                isError = state.result == PasswordResult.WrongCurrent,
                supporting = if (state.result == PasswordResult.WrongCurrent) stringResource(R.string.password_wrong) else null,
                secret = true,
                contentType = ContentType.Password,
                imeAction = ImeAction.Next,
            )
            BingeTextField(
                value = state.new,
                onValueChange = actions.onNew,
                label = stringResource(R.string.password_new),
                secret = true,
                contentType = ContentType.NewPassword,
                imeAction = ImeAction.Next,
            )
            BingeTextField(
                value = state.confirm,
                onValueChange = actions.onConfirm,
                label = stringResource(R.string.password_confirm),
                isError = state.result == PasswordResult.Mismatch,
                supporting = if (state.result == PasswordResult.Mismatch) stringResource(R.string.password_mismatch) else null,
                secret = true,
                contentType = ContentType.NewPassword,
                onDone = actions.onChange,
                imeAction = ImeAction.Done,
            )
        }
    }
    SnackbarMessage(state.result?.snackbar()?.let { stringResource(it) }, host)
}

/** The outcomes a snackbar reports: the two a field shows are not among them. */
private fun PasswordResult.snackbar(): Int? =
    when (this) {
        PasswordResult.Changed -> R.string.password_changed
        PasswordResult.Refused -> R.string.error_generic
        PasswordResult.Unreachable -> R.string.error_unreachable
        PasswordResult.WrongCurrent, PasswordResult.Mismatch -> null
    }
