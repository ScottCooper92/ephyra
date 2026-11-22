package com.cooper.homelab.ephyra.ui.client

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.core.foundation.ServerFailure
import com.cooper.homelab.ephyra.browse.BrowseItem
import com.cooper.homelab.ephyra.browse.ItemKind
import com.cooper.homelab.ephyra.browse.ItemUserData
import com.cooper.homelab.ephyra.browse.PersonResult
import com.cooper.homelab.ephyra.client.search.SearchActions
import com.cooper.homelab.ephyra.client.search.SearchBody
import com.cooper.homelab.ephyra.client.search.SearchHit
import com.cooper.homelab.ephyra.client.search.SearchTabScreen
import com.cooper.homelab.ephyra.client.search.SearchType
import com.cooper.homelab.ephyra.client.search.SearchUiState
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.preview.EphyraWidePreview

/**
 * The Search tab (`:client`) in each state: the recent queries, loading, results of every kind with their marks, narrowed to one type,
 * more on the way and failing, nothing found, and a failed search. No picture is loaded, so every run is the same.
 */
class ClientSearchScreenshotTest {
    private val hits =
        listOf(
            SearchHit.Title(BrowseItem("m1", "Heat", ItemKind.Movie, year = 1995, userData = ItemUserData(played = true))),
            SearchHit.Title(BrowseItem("s1", "Heat Lightning", ItemKind.Series, year = 2017, userData = ItemUserData(favourite = true))),
            SearchHit.Title(
                BrowseItem(
                    "e1",
                    "The Heat of the Night",
                    ItemKind.Episode,
                    seriesName = "Dark",
                    seasonNumber = 1,
                    episodeNumber = 4,
                ),
            ),
            SearchHit.Person(PersonResult("p1", "Heather Graham", null)),
        )

    /** What the model sends: the chosen chip's list, and a page for it beside All's (the other chips are not read). */
    private fun ready(
        list: List<SearchHit> = hits,
        hasMore: Boolean = false,
        appending: Boolean = false,
        appendFailed: Boolean = false,
        type: SearchType = SearchType.All,
    ) = SearchBody.Ready(
        list,
        loaded = list.size,
        hasMore = hasMore,
        appending = appending,
        appendFailed = appendFailed,
        query = "heat",
        pages = mapOf(SearchType.All to hits, type to list),
    )

    @Composable
    private fun Search(
        body: SearchBody,
        text: String = "heat",
        type: SearchType = SearchType.All,
    ) = SearchTabScreen(
        state = SearchUiState(text, type, body),
        imageUrl = { _, _ -> null },
        actions = SearchActions({}, {}, {}, { _, _ -> }, {}, {}, {}, {}, {}),
    )

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @EphyraWidePreview
    @Composable
    fun results() = Search(ready())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun recents() = Search(SearchBody.Idle(listOf("alien", "heat", "the wire")), text = "")

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun idleWithNoRecents() = Search(SearchBody.Idle(emptyList()), text = "")

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = Search(SearchBody.Loading)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun people() = Search(ready(hits.filterIsInstance<SearchHit.Person>(), type = SearchType.People), type = SearchType.People)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun chipLoading() =
        Search(
            SearchBody.Ready(
                emptyList(),
                loaded = 0,
                hasMore = false,
                appending = false,
                appendFailed = false,
                query = "heat",
                pages = mapOf(SearchType.All to hits),
            ),
            type = SearchType.Series,
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun appending() = Search(ready(hasMore = true, appending = true))

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun appendFailed() = Search(ready(hasMore = true, appendFailed = true))

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun noResults() = Search(SearchBody.NoResults("heat"))

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = Search(SearchBody.Failed(ServerFailure.Unreachable()))
}
