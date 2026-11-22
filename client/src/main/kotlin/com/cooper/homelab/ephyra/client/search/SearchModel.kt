package com.cooper.homelab.ephyra.client.search

import com.binge.companion.sdk.attempt
import com.cooper.homelab.core.foundation.ServerFailure
import com.cooper.homelab.core.presentation.paging.Answered
import com.cooper.homelab.core.presentation.paging.PagedList
import com.cooper.homelab.core.presentation.paging.PagedListState
import com.cooper.homelab.core.presentation.paging.ResultPage
import com.cooper.homelab.core.presentation.search.PagedQuerySearch
import com.cooper.homelab.core.presentation.search.QUERY_DEBOUNCE_MS
import com.cooper.homelab.core.presentation.search.QueryHistory
import com.cooper.homelab.core.presentation.search.QuerySearchState
import com.cooper.homelab.core.presentation.search.TypeFilter
import com.cooper.homelab.ephyra.browse.ItemKind
import com.cooper.homelab.ephyra.browse.ItemsRepository
import com.cooper.homelab.ephyra.browse.SearchRepository
import com.cooper.homelab.ephyra.client.TabModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The Search tab over the user's own server. Each chip is its own list, read from the server when the chip is first shown and kept when
 * the user leaves it: All is the movies, series and episodes by name with the first page of people alongside (the server cannot page
 * them before 12.0), and each other chip asks the server for that one type, so Movies is every movie that matches and not the movies among
 * what All has loaded. A list is read again only for a new query, and only when its chip is next shown.
 *
 * The shared [PagedQuerySearch] holds the text, the debounce, the history and the All list; a [TypeFilter] holds the chosen chip, and a new
 * query starts on All again. It uses no Android type, and [scope] is the caller's, with no default. [initialQuery] is what the field held
 * before a process death, searched when the tab starts. No count is asked for: the chips carry none.
 */
class SearchModel(
    private val scope: CoroutineScope,
    private val search: SearchRepository,
    history: QueryHistory,
    private val classify: suspend (Throwable) -> ServerFailure,
    private val initialQuery: String = "",
    private val pageSize: Int = ItemsRepository.DEFAULT_PAGE_SIZE,
    debounceMs: Long = QUERY_DEBOUNCE_MS,
) : TabModel<SearchUiState> {
    private val query =
        PagedQuerySearch<SearchHit, Int>(
            scope = scope,
            history = history,
            first = 0,
            fetch = { text, start ->
                val titles = search.titles(text, TITLE_KINDS, start, pageSize)
                // People come with the first page only, and a search for them that fails leaves the titles as they are.
                val people = if (start == 0) attempt { search.people(text, 0, pageSize).items }.getOrDefault(emptyList()) else emptyList()
                ResultPage(titles.items.map(SearchHit::Title) + people.map(SearchHit::Person), next = titles.nextStart(pageSize))
            },
            classify = classify,
            debounceMs = debounceMs,
            keyOf = { it.kind to it.id },
        )
    private val kinds = TypeFilter<SearchKind, SearchHit>(SearchKind.entries, SearchHit::kind)

    /** The query the All list answers, as it settled; blank while the field holds too little to search. */
    private val settled =
        query.state
            .map { it.settledQuery() }
            .distinctUntilChanged()

    /**
     * A list for one chip over the settled query. It takes a query only while its chip is the chosen one, so a chip nobody has opened reads
     * nothing, and one the user has left keeps what it holds until it is shown again.
     */
    private fun chipList(
        kind: SearchKind,
        fetch: suspend (text: String, start: Int) -> ResultPage<SearchHit, Int>,
    ) = PagedList<String, SearchHit, Int>(
        scope = scope,
        query = combine(settled, kinds.selected) { text, selected -> text to selected }.filter { it.second == kind }.map { it.first },
        first = 0,
        fetch = { text, start -> if (text.isBlank()) ResultPage(emptyList(), next = null) else fetch(text, start) },
        classify = classify,
        keyOf = { it.kind to it.id },
    )

    private val typed: Map<SearchKind, PagedList<String, SearchHit, Int>> =
        mapOf(
            SearchKind.Movie to titlesOf(SearchKind.Movie, ItemKind.Movie),
            SearchKind.Series to titlesOf(SearchKind.Series, ItemKind.Series),
            SearchKind.Episode to titlesOf(SearchKind.Episode, ItemKind.Episode),
            SearchKind.Person to
                chipList(SearchKind.Person) { text, start ->
                    val page = search.people(text, start, pageSize)
                    ResultPage(page.items.map(SearchHit::Person), next = page.nextStart(pageSize))
                },
        )

    private fun titlesOf(
        kind: SearchKind,
        item: ItemKind,
    ) = chipList(kind) { text, start ->
        val page = search.titles(text, setOf(item), start, pageSize)
        ResultPage(page.items.map(SearchHit::Title), next = page.nextStart(pageSize))
    }

    private fun QuerySearchState<SearchHit>.settledQuery(): String =
        when (this) {
            is QuerySearchState.Idle -> ""
            is QuerySearchState.Loading -> query
            is QuerySearchState.Results -> query
            is QuerySearchState.NoResults -> query
            is QuerySearchState.Failed -> query
        }

    private val answers: Flow<Map<SearchKind, Answered<String, SearchHit>?>> =
        combine(typed.map { (kind, list) -> list.answered.map { kind to it } }) { it.toMap() }

    override val state: StateFlow<SearchUiState> =
        combine(kinds.selected, query.query, query.state, answers) { selected, text, all, answered ->
            SearchUiState(
                text,
                if (all is QuerySearchState.Results) SearchType.of(selected) else SearchType.All,
                body(selected, all, answered),
            )
        }.stateIn(scope, SharingStarted.Lazily, SearchUiState("", SearchType.All, SearchBody.Idle(emptyList())))

    /** A search has no pull-to-refresh: a retry reads the query again. */
    override val refreshing: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    private fun body(
        selected: SearchKind?,
        all: QuerySearchState<SearchHit>,
        answered: Map<SearchKind, Answered<String, SearchHit>?>,
    ): SearchBody =
        when (all) {
            is QuerySearchState.Idle -> SearchBody.Idle(all.recents)
            is QuerySearchState.Loading -> SearchBody.Loading
            is QuerySearchState.NoResults -> SearchBody.NoResults(all.query)
            is QuerySearchState.Failed -> SearchBody.Failed(all.failure)
            is QuerySearchState.Results -> {
                // A chip's answer counts only for the query All answers now: one that is older is not shown, so a chip chosen again after a new query loads.
                val current =
                    answered
                        .mapNotNull { (kind, found) ->
                            found?.takeIf { it.query == all.query }?.let { kind to it.state }
                        }.toMap()
                val failed = selected?.let { current[it] } as? PagedListState.Failed
                if (failed != null) SearchBody.Failed(failed.failure) else paged(selected, all, current)
            }
        }

    private fun paged(
        selected: SearchKind?,
        all: QuerySearchState.Results<SearchHit>,
        current: Map<SearchKind, PagedListState<SearchHit>>,
    ): SearchBody.Ready {
        val pages =
            buildMap {
                put(SearchType.All, all.items)
                current.forEach { (kind, state) ->
                    when (state) {
                        is PagedListState.Ready -> put(SearchType.of(kind), state.items)
                        PagedListState.Empty -> put(SearchType.of(kind), emptyList())
                        else -> Unit
                    }
                }
            }
        val shown = selected?.let { current[it] }
        val items = pages[SearchType.of(selected)].orEmpty()
        return when {
            selected == null -> SearchBody.Ready(items, items.size, all.hasMore, all.appending, all.appendFailed, all.query, pages)
            shown is PagedListState.Ready ->
                SearchBody.Ready(
                    items,
                    items.size,
                    shown.hasMore,
                    shown.appending,
                    shown.appendFailed,
                    all.query,
                    pages,
                )
            else ->
                SearchBody.Ready(
                    items,
                    items.size,
                    hasMore = false,
                    appending = false,
                    appendFailed = false,
                    query = all.query,
                    pages = pages,
                )
        }
    }

    /** Searches what the field held before the process ended, if it held anything. */
    override fun start() {
        if (initialQuery.isNotBlank()) setText(initialQuery)
    }

    override fun refresh() = retry()

    /** The field was edited. A text that is not the query the chip was chosen for puts the chips back to All. */
    fun setText(text: String) {
        kinds.resetForQuery(text)
        query.setQuery(text)
    }

    fun clear() {
        kinds.reset()
        query.clear()
    }

    fun submit() = query.submit()

    /** Reads the chosen chip's first page again. */
    fun retry() {
        val list = kinds.selected.value?.let(typed::get)
        if (list == null) query.retry() else list.retry()
    }

    /** Asks for more of the chosen chip. */
    fun loadMore() {
        val list = kinds.selected.value?.let(typed::get)
        if (list == null) query.loadMore() else list.loadMore()
    }

    /** A recent query was picked: it is searched now, without the debounce. */
    fun pickRecent(text: String) {
        setText(text)
        query.submit()
    }

    fun clearRecents() {
        scope.launch { query.forgetRecents() }
    }

    fun selectType(type: SearchType) = kinds.select(type.kind)

    private companion object {
        val TITLE_KINDS = setOf(ItemKind.Movie, ItemKind.Series, ItemKind.Episode)
    }
}
