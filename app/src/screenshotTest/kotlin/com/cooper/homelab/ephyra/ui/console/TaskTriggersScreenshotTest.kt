package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.LIBRARY_SCAN_TASK
import com.cooper.homelab.ephyra.server.ServerTask
import com.cooper.homelab.ephyra.server.TICKS_PER_HOUR
import com.cooper.homelab.ephyra.server.TICKS_PER_MINUTE
import com.cooper.homelab.ephyra.server.TaskTrigger
import com.cooper.homelab.ephyra.server.TriggerKind

private val previewTriggers =
    listOf(
        TaskTrigger.daily(3 * TICKS_PER_HOUR, maxRuntimeTicks = 2 * TICKS_PER_HOUR),
        TaskTrigger.weekly("Sunday", 4 * TICKS_PER_HOUR + 30 * TICKS_PER_MINUTE),
        TaskTrigger.interval(6 * TICKS_PER_HOUR),
        TaskTrigger.interval(45 * TICKS_PER_MINUTE),
        TaskTrigger.startup(),
        TaskTrigger("SystemEventTrigger"),
    )

private fun scan(triggers: List<TaskTrigger>) =
    ServerTask(
        "scan",
        LIBRARY_SCAN_TASK,
        "Scan Media Library",
        "Scans your media library for new files and refreshes metadata.",
        "Library",
        false,
        false,
        null,
        null,
        triggers,
    )

private fun open(
    triggers: List<TaskTrigger> = previewTriggers,
    adding: Boolean = false,
    saving: Boolean = false,
    failed: ConsoleFailure? = null,
) = TasksUiState.Ready(TaskBoard(listOf(scan(triggers))), triggerFailed = failed, adding = adding, saving = saving)

/** One task's triggers across the matrix, with none, saving and failed, then the new trigger form for each kind. */
class TaskTriggersScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun triggers() = TaskScreen(open(), "scan", TasksActions())

    /** A task open after a process restore, while the view model loads the board again under its screen. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun taskLoading() = TaskScreen(TasksUiState.Loading, "scan", TasksActions())

    /** The same reload, failed: the error screen with Retry under the task's own bar. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun taskFailed() = TaskScreen(TasksUiState.Failed(ConsoleFailure.Unreachable), "scan", TasksActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun none() = TaskScreen(open(emptyList()), "scan", TasksActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun saving() = TaskScreen(open(saving = true), "scan", TasksActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun newTrigger() = Surface { AddTriggerForm(TasksActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @Composable
    fun newWeeklyTrigger() = Surface { AddTriggerForm(TasksActions(), TriggerKind.Weekly) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @Composable
    fun newIntervalTrigger() = Surface { AddTriggerForm(TasksActions(), TriggerKind.Interval) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @Composable
    fun newStartupTrigger() = Surface { AddTriggerForm(TasksActions(), TriggerKind.Startup) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @Composable
    fun newTriggerTwentyFourHour() = Surface { AddTriggerForm(TasksActions(), is24Hour = true) }
}
