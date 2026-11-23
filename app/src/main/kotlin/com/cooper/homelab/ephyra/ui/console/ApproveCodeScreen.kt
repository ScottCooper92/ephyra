package com.cooper.homelab.ephyra.ui.console

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.binge.designsystem.component.BingeTextField
import com.binge.designsystem.component.PageIntro
import com.binge.designsystem.template.FormAction
import com.binge.designsystem.template.FormScreen
import com.binge.designsystem.template.ScreenBar
import com.cooper.homelab.ephyra.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import com.binge.designsystem.R as DesR

/** What the approval page can ask of its view model, and of the console. */
data class ApproveActions(
    val onCode: (String) -> Unit = {},
    val onApprove: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * Approving a Quick Connect code: the six digits another device shows, and Approve in the footer. A code that is not waiting
 * shows on its field with what to do about it, and the rest of the outcome is a snackbar, once each from [events]: approving is a form whose submit is
 * the point, so there is nothing to save as made.
 */
@Composable
fun ApproveCodeScreen(
    state: ApproveUiState,
    actions: ApproveActions,
    events: Flow<ApproveOutcome> = emptyFlow(),
) {
    val host = remember { SnackbarHostState() }
    FormScreen(
        title = stringResource(R.string.approve_title),
        bar = ScreenBar.Small,
        onBack = actions.onBack,
        snackbarHostState = host,
        primaryAction =
            FormAction(
                label = stringResource(R.string.approve_action),
                onClick = actions.onApprove,
                enabled = state.complete,
                busy = state.approving,
            ),
    ) { _ ->
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l))) {
            PageIntro(stringResource(R.string.approve_body))
            BingeTextField(
                value = state.code,
                onValueChange = actions.onCode,
                label = stringResource(R.string.approve_field),
                isError = state.wrongCode,
                supporting = if (state.wrongCode) stringResource(R.string.approve_wrong) else null,
                keyboardType = KeyboardType.NumberPassword,
                onDone = actions.onApprove,
                imeAction = ImeAction.Done,
            )
        }
    }
    SnackbarEvents(events, host) { it.sentence() }
}

private fun ApproveOutcome.sentence(): Int =
    when (this) {
        ApproveOutcome.Approved -> R.string.approve_done
        ApproveOutcome.QuickConnectOff -> R.string.error_quick_connect_off
        ApproveOutcome.Unreachable -> R.string.error_unreachable
        ApproveOutcome.Failed -> R.string.error_generic
    }
