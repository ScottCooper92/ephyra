package com.cooper.homelab.ephyra.ui.console

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.component.BingePullToRefresh
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.PageIntro
import com.binge.designsystem.formatRelativeOrAbsolute
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchorIf
import com.binge.designsystem.template.BingeScreenScaffold
import com.binge.designsystem.template.EmptyScreen
import com.binge.designsystem.template.ScreenBar
import com.binge.designsystem.template.ScreenBody
import com.binge.designsystem.template.screenListPadding
import com.cooper.homelab.ephyra.R
import com.cooper.homelab.ephyra.server.ServerTask
import com.cooper.homelab.ephyra.server.TaskStatus
import com.cooper.homelab.ephyra.server.TaskTrigger
import com.binge.designsystem.R as DesR

/** What the tasks screens can ask of the view model, and of the console. */
data class TasksActions(
    val onRun: (ServerTask) -> Unit = {},
    val onStop: (ServerTask) -> Unit = {},
    /** Opens a task's own screen, with its triggers. */
    val onOpen: (ServerTask) -> Unit = {},
    val onAskAdd: (Boolean) -> Unit = {},
    val onAddTrigger: (TaskTrigger) -> Unit = {},
    val onRemoveTrigger: (Int) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * The server's scheduled tasks as job rows, grouped by the server's own categories in its order: each
 * run or stopped in place, with how far it has got while it runs, and when it last ran (or failed)
 * while it doesn't. A tap opens the task's own screen ([TaskScreen]), above this one on the stack. The task showing,
 * where this list is beside it, is marked.
 */
@Composable
fun TasksScreen(
    state: TasksUiState,
    actions: TasksActions,
    now: Long = System.currentTimeMillis(),
    openTask: String? = LocalOpenItem.current,
) {
    // Held above the loading, failed and ready branches, so a reload does not drop where the user was.
    val listState = rememberLazyListState()
    val ready = state as? TasksUiState.Ready
    val host = remember { SnackbarHostState() }
    BingeScreenScaffold(
        title = stringResource(R.string.tasks_title),
        bar = ScreenBar.Small,
        onBack = actions.onBack,
        snackbarHostState = host,
    ) { padding ->
        ScreenBody(padding) { _ ->
            val inner = padding.screenListPadding()
            when {
                state is TasksUiState.Loading ->
                    ConsoleLoading(
                        inner,
                        LOADING_ROWS,
                        intro = stringResource(R.string.tasks_intro),
                        formInset = false,
                    )
                state is TasksUiState.Failed -> ConsoleFailureScreen(state.failure, actions.onRetry, Modifier.padding(inner))
                ready != null ->
                    BingePullToRefresh(
                        isRefreshing = ready.refreshing,
                        onRefresh = actions.onRefresh,
                        indicatorTopInset = padding.calculateTopPadding(),
                    ) { Tasks(ready, actions, inner, now, openTask, listState) }
            }
            // A change or run the server didn't take is said in a message, not in a space kept in the list for it.
            RefusedMessage(ready?.refreshFailed, host, R.string.console_not_refreshed)
            RefusedMessage(ready?.actionFailed, host, R.string.tasks_not_sent)
        }
    }
}

/**
 * One task: its description and its triggers, with Add trigger in the footer. A screen of its own above the list on the
 * stack, so Back returns to the list. A task the server no longer lists has nothing to show, and closes.
 */
@Composable
fun TaskScreen(
    state: TasksUiState,
    taskId: String,
    actions: TasksActions,
) {
    val ready = state as? TasksUiState.Ready
    val task = ready?.board?.tasks?.firstOrNull { it.id == taskId }
    val gone = ready != null && task == null
    LaunchedEffect(gone) { if (gone) actions.onBack() }
    // Held above the loading, failed and ready branches, so a reload does not drop where the user was.
    val listState = rememberLazyListState()
    val host = remember { SnackbarHostState() }
    BingeScreenScaffold(
        title = task?.name ?: stringResource(R.string.tasks_title),
        bar = ScreenBar.Small,
        onBack = actions.onBack,
        snackbarHostState = host,
        bottomBar = {
            // A task's triggers page creates triggers.
            CreateFooter(
                label = stringResource(R.string.tasks_trigger_add),
                icon = Icons.Filled.Add,
                onClick = { actions.onAskAdd(true) },
                visible = ready != null && task != null,
                loading = ready?.saving == true,
            )
        },
    ) { padding ->
        ScreenBody(padding) { _ ->
            val inner = padding.screenListPadding()
            when {
                state is TasksUiState.Loading -> ConsoleLoading(inner, LOADING_ROWS)
                state is TasksUiState.Failed -> ConsoleFailureScreen(state.failure, actions.onRetry, Modifier.padding(inner))
                ready != null && task != null -> TaskTriggers(task, ready, actions, inner, listState)
            }
            RefusedMessage(ready?.triggerFailed, host, R.string.tasks_trigger_not_saved)
        }
    }
}

/** What fills a landscape tablet's third pane while no task is open. */
@Composable
internal fun TaskPlaceholder() {
    EmptyScreen(
        message = stringResource(R.string.tasks_select),
        title = stringResource(R.string.tasks_select_title),
        icon = Icons.Filled.Schedule,
    )
}

@Composable
private fun Tasks(
    state: TasksUiState.Ready,
    actions: TasksActions,
    inner: PaddingValues,
    now: Long,
    openTask: String?,
    listState: LazyListState,
) {
    val board = state.board
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = inner,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        if (board.tasks.isEmpty()) {
            item { ConsoleEmptyScreen(stringResource(R.string.tasks_empty), Icons.Filled.Schedule) }
        } else {
            item { PageIntro(stringResource(R.string.tasks_intro)) }
        }
        board.tasks.groupBy { it.category }.entries.forEachIndexed { index, (category, tasks) ->
            item(key = category) {
                ItemGroup(
                    title = category.ifBlank { null },
                    modifier = Modifier.layoutAnchorIf(index == 0, LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
                    rows =
                        tasks.map { task ->
                            task.row(task.id == openTask, board, actions, now).item()
                        },
                )
            }
        }
    }
}

@Composable
private fun ServerTask.row(
    selected: Boolean,
    board: TaskBoard,
    actions: TasksActions,
    now: Long,
): JobRow {
    val last = lastRun
    val idle =
        if (last?.status == TaskStatus.Failed || last?.status == TaskStatus.Aborted) {
            formatRelativeOrAbsolute(last.endedMillis, now)?.let { stringResource(R.string.job_last_failed, it) }
                ?: stringResource(R.string.job_failed)
        } else {
            lastRun(last?.endedMillis, now)
        }
    return JobRow(
        icon = taskIcon(key),
        label = name,
        idleDetail = idle,
        running = board.running(this),
        percent = percent,
        busy = id in board.busy,
        outcome = board.outcomes[id],
        onRun = { actions.onRun(this) },
        onStop = { actions.onStop(this) },
        onOpen = { actions.onOpen(this) },
        selected = selected,
    )
}

/** A task's icon, from its key; a clock for a task this app doesn't know. */
internal fun taskIcon(key: String): ImageVector =
    when {
        key == "RefreshLibrary" -> Icons.Filled.VideoLibrary
        key.contains("Audio") -> Icons.Filled.GraphicEq
        key.contains("Subtitle") -> Icons.Filled.Subtitles
        key.contains("Lyric") -> Icons.Filled.Lyrics
        key.contains("People") -> Icons.Filled.People
        key.contains("Chapter") || key.contains("Trickplay") || key.contains("Image") -> Icons.Filled.Image
        key.contains("Clean") || key.contains("Delete") -> Icons.Filled.CleaningServices
        key.contains("Plugin") -> Icons.Filled.Extension
        key.contains("Guide") || key.contains("Channel") -> Icons.Filled.LiveTv
        key.contains("Database") || key.contains("Optimi") -> Icons.Filled.Storage
        else -> Icons.Filled.Schedule
    }

private const val LOADING_ROWS = 8
