package com.cooper.homelab.ephyra.ui.console

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.ListItem
import com.binge.designsystem.component.PageIntro
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.template.EmptyScreen
import com.binge.designsystem.template.FormScreen
import com.binge.designsystem.template.ScreenBar
import com.binge.designsystem.template.formSaveStatus
import com.cooper.homelab.ephyra.R
import com.cooper.homelab.ephyra.server.UserView
import com.cooper.homelab.ephyra.ui.search.SettingRows
import com.binge.designsystem.R as DesR

/** What the home screen page can ask of its view model, and of the console. */
data class HomeLayoutActions(
    /** Opens a library's own screen. */
    val onOpen: (String) -> Unit = {},
    val onInMyMedia: (String, Boolean) -> Unit = { _, _ -> },
    val onInLatest: (String, Boolean) -> Unit = { _, _ -> },
    val onGrouped: (String, Boolean) -> Unit = { _, _ -> },
    val onMove: (String, Int) -> Unit = { _, _ -> },
    val onHidePlayedInLatest: (Boolean) -> Unit = {},
    val onDisplayMissingEpisodes: (Boolean) -> Unit = {},
    val onDisplayCollectionsView: (Boolean) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onRetrySave: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/** Lines a switch's detail may wrap to, so it reads in full at large fonts. */
private const val HOME_DETAIL_LINES = 3

/** Lines the order note may wrap to: it is the one place that says what the Move rows move, so it is never cut. */
private const val ORDER_DETAIL_LINES = 8

/**
 * How Jellyfin's apps lay out the user's home: their libraries in order, each with its own screen
 * ([HomeLayoutLibraryScreen]) for where it shows and its place in the order, and the display switches. Each change
 * saves as it's made. The library showing, where this list is beside it, is marked.
 */
@Composable
fun HomeLayoutScreen(
    state: HomeLayoutUiState,
    actions: HomeLayoutActions,
    openLibrary: String? = LocalOpenItem.current,
) {
    val host = remember { SnackbarHostState() }
    val ready = state as? HomeLayoutUiState.Ready
    FormScreen(
        title = stringResource(R.string.home_layout_title),
        bar = ScreenBar.Small,
        onBack = actions.onBack,
        snackbarHostState = host,
        saveStatus = ready?.save?.let { formSaveStatus(it.status, it.failed, actions.onRetrySave) },
        notReady =
            when (state) {
                HomeLayoutUiState.Loading -> { padding ->
                    ConsoleLoading(padding, LOADING_ROWS, intro = stringResource(R.string.home_layout_body))
                }
                is HomeLayoutUiState.Failed -> { padding ->
                    ConsoleFailureScreen(state.failure, actions.onRetry, Modifier.padding(padding))
                }
                is HomeLayoutUiState.Ready -> null
            },
    ) { _ ->
        if (ready != null) {
            HomeLayoutBody(ready, actions, openLibrary)
            // A refusal is said by the screen of the library open, if one is, and by this list otherwise.
            if (openLibrary == null) RefusedMessage(ready.refused, host)
        }
    }
}

/**
 * One library's own screen: where it shows on the home, and its place in the order. Above the list on the stack, so Back
 * returns to it. A library the server no longer lists closes it.
 */
@Composable
fun HomeLayoutLibraryScreen(
    state: HomeLayoutUiState,
    libraryId: String,
    actions: HomeLayoutActions,
) {
    val ready = state as? HomeLayoutUiState.Ready
    val library = ready?.libraries?.firstOrNull { it.id == libraryId }
    val gone = ready != null && library == null
    LaunchedEffect(gone) { if (gone) actions.onBack() }
    val host = remember { SnackbarHostState() }
    FormScreen(
        title = library?.name ?: stringResource(R.string.home_layout_title),
        bar = ScreenBar.Small,
        onBack = actions.onBack,
        snackbarHostState = host,
        saveStatus = ready?.save?.let { formSaveStatus(it.status, it.failed, actions.onRetrySave) },
        notReady =
            when (state) {
                HomeLayoutUiState.Loading -> { padding -> ConsoleLoading(padding, LOADING_ROWS, formInset = true) }
                is HomeLayoutUiState.Failed -> { padding ->
                    ConsoleFailureScreen(state.failure, actions.onRetry, Modifier.padding(padding))
                }
                is HomeLayoutUiState.Ready -> null
            },
    ) { _ ->
        if (ready != null && library != null) {
            LibraryPage(ready, library, actions)
            RefusedMessage(ready.refused, host)
        }
    }
}

/** What fills a landscape tablet's third pane while no library is open. */
@Composable
internal fun HomeLayoutPlaceholder() {
    EmptyScreen(
        message = stringResource(R.string.home_layout_select),
        title = stringResource(R.string.home_layout_select_title),
        icon = Icons.Filled.VideoLibrary,
    )
}

@Composable
private fun HomeLayoutBody(
    state: HomeLayoutUiState.Ready,
    actions: HomeLayoutActions,
    openLibrary: String?,
) {
    val layout = state.layout
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        PageIntro(stringResource(R.string.home_layout_body))
        ItemGroup(
            title = stringResource(R.string.home_layout_libraries),
            modifier = Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
            rows =
                state.libraries
                    .map { library ->
                        ListItem(
                            icon = library.icon(),
                            label = library.name,
                            detail = stringResource(library.whereShown(state)),
                            selected = library.id == openLibrary,
                            onClick = { actions.onOpen(library.id) },
                        )
                    }.ifEmpty {
                        listOf(
                            ListItem(
                                icon = Icons.Filled.VideoLibrary,
                                label = stringResource(R.string.home_layout_no_libraries),
                                clickable = false,
                            ),
                        )
                    },
        )
        if (state.groupable.isNotEmpty()) {
            ItemGroup(
                title = stringResource(R.string.home_layout_grouping),
                rows =
                    state.groupable.map { option ->
                        val grouped = option.id in layout.grouped
                        ListItem(
                            icon = Icons.Filled.Workspaces,
                            label = option.name,
                            detail = stringResource(R.string.home_layout_grouped_detail),
                            supportingMaxLines = HOME_DETAIL_LINES,
                            toggled = grouped,
                            onClick = { actions.onGrouped(option.id, !grouped) },
                        )
                    },
            )
        }
        ItemGroup(
            title = stringResource(R.string.home_layout_display),
            rows =
                listOf(
                    ListItem(
                        icon = Icons.Filled.NewReleases,
                        label = stringResource(R.string.home_layout_hide_played),
                        toggled = layout.hidePlayedInLatest,
                        onClick = { actions.onHidePlayedInLatest(!layout.hidePlayedInLatest) },
                        key = SettingRows.HOME_HIDE_PLAYED,
                    ),
                    ListItem(
                        icon = Icons.Filled.HideSource,
                        label = stringResource(R.string.home_layout_missing),
                        detail = stringResource(R.string.home_layout_missing_detail),
                        supportingMaxLines = HOME_DETAIL_LINES,
                        toggled = layout.displayMissingEpisodes,
                        onClick = { actions.onDisplayMissingEpisodes(!layout.displayMissingEpisodes) },
                        key = SettingRows.HOME_MISSING_EPISODES,
                    ),
                    ListItem(
                        icon = Icons.Filled.CollectionsBookmark,
                        label = stringResource(R.string.home_layout_collections),
                        detail = stringResource(R.string.home_layout_collections_detail),
                        supportingMaxLines = HOME_DETAIL_LINES,
                        toggled = layout.displayCollectionsView,
                        onClick = { actions.onDisplayCollectionsView(!layout.displayCollectionsView) },
                        key = SettingRows.HOME_COLLECTIONS,
                    ),
                ),
        )
    }
}

/** One library's page: where it shows on the home and its place in the order. */
@Composable
private fun LibraryPage(
    state: HomeLayoutUiState.Ready,
    library: UserView,
    actions: HomeLayoutActions,
) {
    val layout = state.layout
    val inMyMedia = library.id !in layout.myMediaExcludes
    val inLatest = library.id !in layout.latestExcludes
    val place = state.libraries.indexOf(library)
    AnnounceMove(place, state.libraries.size)
    OrderedSettingsPage(
        title = stringResource(R.string.home_layout_where),
        switches =
            listOf(
                ListItem(
                    icon = Icons.Filled.ViewModule,
                    label = stringResource(R.string.home_layout_in_my_media),
                    toggled = inMyMedia,
                    onClick = { actions.onInMyMedia(library.id, !inMyMedia) },
                ),
                ListItem(
                    icon = Icons.Filled.NewReleases,
                    label = stringResource(R.string.home_layout_in_latest),
                    toggled = inLatest,
                    onClick = { actions.onInLatest(library.id, !inLatest) },
                ),
            ),
        place = place,
        size = state.libraries.size,
        onMove = { actions.onMove(library.id, it) },
        position =
            ListItem(
                icon = Icons.Filled.FormatListNumbered,
                label = stringResource(R.string.home_layout_position, place + 1, state.libraries.size),
                detail = stringResource(R.string.home_layout_order_detail),
                supportingMaxLines = ORDER_DETAIL_LINES,
                clickable = false,
            ),
        firstDetail = stringResource(R.string.home_layout_already_first),
        lastDetail = stringResource(R.string.home_layout_already_last),
    )
}

/**
 * Says the library's new place aloud once it has moved, as TalkBack: the position row is plain text, so a move reads
 * nothing until focus returns to it. The first composition says nothing, only a change of [place] does. A
 * `ListItem` carries no live region, so this announces from the view.
 */
@Composable
private fun AnnounceMove(
    place: Int,
    count: Int,
) {
    val view = LocalView.current
    val message = stringResource(R.string.home_layout_moved, place + 1, count)
    var shown by remember { mutableIntStateOf(place) }
    LaunchedEffect(place) {
        if (place != shown) {
            shown = place
            view.announceForAccessibility(message)
        }
    }
}

/** Where a library shows on the home, for its row. */
private fun UserView.whereShown(state: HomeLayoutUiState.Ready): Int {
    val myMedia = id !in state.layout.myMediaExcludes
    val latest = id !in state.layout.latestExcludes
    return when {
        myMedia && latest -> R.string.home_layout_shown_both
        myMedia -> R.string.home_layout_shown_my_media
        latest -> R.string.home_layout_shown_latest
        else -> R.string.home_layout_shown_none
    }
}

/** The icon for a library's kind, as its collection type names it. */
internal fun UserView.icon(): ImageVector =
    when (collectionType) {
        "movies" -> Icons.Filled.Movie
        "tvshows" -> Icons.Filled.Tv
        "music", "musicvideos" -> Icons.Filled.MusicNote
        "books" -> Icons.Filled.AutoStories
        "photos", "homevideos" -> Icons.Filled.PhotoLibrary
        "boxsets" -> Icons.Filled.CollectionsBookmark
        "playlists" -> Icons.AutoMirrored.Filled.PlaylistPlay
        "livetv" -> Icons.Filled.LiveTv
        "folders" -> Icons.Filled.Folder
        else -> Icons.Filled.GridView
    }

private const val LOADING_ROWS = 10
