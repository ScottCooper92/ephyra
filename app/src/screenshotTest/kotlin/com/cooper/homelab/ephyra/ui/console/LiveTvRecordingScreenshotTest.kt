package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.ServerFolder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

private val config: JsonObject =
    Json
        .parseToJsonElement(
            """{"GuideDays":7,"RecordingPath":"/media/recordings","MovieRecordingPath":"","SeriesRecordingPath":"/media/tv/recorded",""" +
                """"PrePaddingSeconds":60,"PostPaddingSeconds":300,"EnableRecordingSubfolders":true,""" +
                """"EnableOriginalAudioWithEncodedRecordings":false,"SaveRecordingNFO":true,"SaveRecordingImages":false}""",
        ).jsonObject

/** Release 10.8 sends neither the metadata files nor the images switch. */
private val older = JsonObject(config - "SaveRecordingNFO" - "SaveRecordingImages")

private fun ready(shown: JsonObject = config) = LiveTvRecordingUiState.Ready(shown)

/** Live TV recording: the page across the matrix, a release without two switches, and the sheets' content. */
class LiveTvRecordingScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun recording() = LiveTvRecordingScreen(ready(), LiveTvRecordingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun olderRelease() = LiveTvRecordingScreen(ready(older), LiveTvRecordingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun guideAutomatic() = LiveTvRecordingScreen(ready(JsonObject(config + ("GuideDays" to JsonNull))), LiveTvRecordingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = LiveTvRecordingScreen(LiveTvRecordingUiState.Loading, LiveTvRecordingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = LiveTvRecordingScreen(LiveTvRecordingUiState.Failed(ConsoleFailure.Unreachable), LiveTvRecordingActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun padding() = Surface { RecordingNumberForm(RecordingNumber.PostPadding, "5", LiveTvRecordingActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @Composable
    fun paddingOffStep() = Surface { RecordingNumberForm(RecordingNumber.PrePadding, "7", LiveTvRecordingActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun paddingOutOfRange() = Surface { RecordingNumberForm(RecordingNumber.PostPadding, "9000", LiveTvRecordingActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun guideDays() = Surface { RecordingNumberForm(RecordingNumber.GuideDays, "7", LiveTvRecordingActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun guideDaysAutomatic() = Surface { RecordingNumberForm(RecordingNumber.GuideDays, "", LiveTvRecordingActions()) }

    /** A stored length beyond the scale reads as stored. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun guideDaysBeyondScale() = Surface { RecordingNumberForm(RecordingNumber.GuideDays, "20", LiveTvRecordingActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun folder() =
        Surface {
            RecordingFolderForm(
                RecordingFolder.Series,
                "/media/tv/recorded",
                FolderBrowseState.Ready(
                    listOf(ServerFolder("media", "/media")),
                    listOf(ServerFolder("recorded", "/media/tv/recorded")),
                ),
                FolderActions(),
                LiveTvRecordingActions(),
            )
        }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun folderTyped() =
        Surface {
            RecordingFolderForm(
                RecordingFolder.Movies,
                "",
                FolderBrowseState.Loading,
                FolderActions(),
                LiveTvRecordingActions(),
                typePath = TypePath.On,
            )
        }
}
