package com.cooper.homelab.ephyra.ui.console

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.component.BingeTextField
import com.cooper.homelab.ephyra.preview.JellyfinFontScalePreviews
import com.cooper.homelab.ephyra.preview.JellyfinScreenStatePreview
import com.cooper.homelab.ephyra.preview.JellyfinSpanishPreviews
import com.binge.designsystem.R as DesR

/** The console's text field in each shape its call sites use: plain, with a hint, in error, multi-line and secret. */
class ConsoleTextFieldScreenshotTest {
    @Composable
    private fun Fields(content: @Composable () -> Unit) =
        Surface {
            Column(
                modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            ) { content() }
        }

    @PreviewTest
    @JellyfinScreenStatePreview
    @JellyfinFontScalePreviews
    @Composable
    fun plain() =
        Fields {
            BingeTextField(value = "Living room", onValueChange = {}, label = "Device name", imeAction = ImeAction.Done)
            BingeTextField(value = "", onValueChange = {}, label = "Device name", placeholder = "Pixel 10", imeAction = ImeAction.Done)
            BingeTextField(
                value = "9",
                onValueChange = {},
                label = "Threads",
                isError = true,
                supporting = "Enter 1 to 16",
                imeAction = ImeAction.Done,
            )
            BingeTextField(value = "body { color: red; }", onValueChange = {
            }, label = "CSS", singleLine = false, minLines = 3, imeAction = ImeAction.Done)
        }

    @PreviewTest
    @JellyfinScreenStatePreview
    @JellyfinSpanishPreviews
    @Composable
    fun secretMasked() =
        Fields {
            BingeTextField(value = "hunter2", onValueChange = {}, label = "Password", secret = true, imeAction = ImeAction.Done)
        }

    @PreviewTest
    @JellyfinScreenStatePreview
    @JellyfinSpanishPreviews
    @Composable
    fun secretRevealed() =
        Fields {
            BingeTextField(
                value = "hunter2",
                onValueChange = {},
                label = "Password",
                secret = true,
                revealed = true,
                onToggleReveal = {},
                imeAction = ImeAction.Done,
            )
        }
}
