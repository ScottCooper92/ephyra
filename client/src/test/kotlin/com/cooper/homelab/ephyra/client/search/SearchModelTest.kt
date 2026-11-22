package com.cooper.homelab.ephyra.client.search

import com.cooper.homelab.core.foundation.ServerFailure
import com.cooper.homelab.core.presentation.search.QueryHistory
import com.cooper.homelab.ephyra.browse.BrowseItem
import com.cooper.homelab.ephyra.browse.ItemKind
import com.cooper.homelab.ephyra.browse.Page
import com.cooper.homelab.ephyra.browse.PersonResult
import com.cooper.homelab.ephyra.browse.SearchRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/** The Search tab's state: a query searched, the types narrowing what has loaded, and what each answer of the server turns into. */
class SearchModelTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    private class FakeHistory(
        initial: List<String> = emptyList(),
    ) : QueryHistory {
        val kept = MutableStateFlow(initial)
        override val recents get() = kept

        override suspend fun remember(query: String) {
            kept.value = listOf(query) + kept.value.filter { it != query }
        }

        override suspend fun clear() {
            kept.value = emptyList()
        }
    }

    /** Answers with [titles] by start index and [people], or throws; every read is recorded. */
    private class FakeSearch(
        var titles: List<BrowseItem> = emptyList(),
        var people: List<PersonResult> = emptyList(),
        var failure: Throwable? = null,
        var peopleFailure: Throwable? = null,
    ) : SearchRepository {
        /** When set, a read of a single type waits for it, as a slow server would. */
        var gate: CompletableDeferred<Unit>? = null
        val titleReads = mutableListOf<Triple<String, Set<ItemKind>, Int>>()
        val peopleReads = mutableListOf<Pair<String, Int>>()

        override suspend fun titles(
            term: String,
            kinds: Set<ItemKind>,
            startIndex: Int,
            limit: Int,
            withTotal: Boolean,
        ): Page<BrowseItem> {
            titleReads += Triple(term, kinds, startIndex)
            if (kinds.size == 1) gate?.await()
            failure?.let { throw it }
            val found = titles.filter { it.kind in kinds }
            return Page(found.drop(startIndex).take(limit), total = found.size, startIndex = startIndex)
        }

        override suspend fun people(
            term: String,
            startIndex: Int,
            limit: Int,
        ): Page<PersonResult> {
            peopleReads += term to startIndex
            peopleFailure?.let { throw it }
            return Page(people.drop(startIndex).take(limit), total = people.size, startIndex = startIndex)
        }
    }

    private val movie = BrowseItem("m1", "Heat", ItemKind.Movie, year = 1995)
    private val series = BrowseItem("s1", "Heat Wave", ItemKind.Series, year = 2010)
    private val episode = BrowseItem("e1", "Pilot", ItemKind.Episode, seriesName = "Heat Wave")
    private val person = PersonResult("p1", "Al Pacino", null)
    private val search = FakeSearch(titles = listOf(movie, series, episode), people = listOf(person))
    private val history = FakeHistory(listOf("alien"))

    @After
    fun close() = scope.cancel()

    private fun model(initialQuery: String = "") =
        SearchModel(scope, search, history, { ServerFailure.Unreachable() }, initialQuery, pageSize = PAGE, debounceMs = 0L)
            .also { it.state.onEach { }.launchIn(scope) }

    private fun SearchModel.body() = state.value.body

    private fun SearchModel.ready() = body() as SearchBody.Ready

    @Test
    fun `before anything is typed the recent queries are offered`() {
        val model = model()
        assertEquals(SearchBody.Idle(listOf("alien")), model.body())
        assertEquals(SearchType.All, model.state.value.type)
        assertTrue(search.titleReads.isEmpty())
    }

    @Test
    fun `a query too short to search stays idle and a longer one searches`() {
        val model = model()
        model.setText("h")
        assertTrue(model.body() is SearchBody.Idle)
        assertTrue(search.titleReads.isEmpty())
        model.setText("heat")
        assertEquals("heat", model.state.value.text)
        assertTrue(model.body() is SearchBody.Ready)
        assertEquals("heat", search.titleReads.single().first)
    }

    @Test
    fun `titles come first and people join the first page only`() {
        val model = model()
        model.setText("heat")
        val ready = model.ready()
        assertEquals(listOf("Movie:m1", "Series:s1", "Person:p1"), ready.hits.map(::searchHitKey))
        assertTrue(ready.hasMore)
        model.loadMore()
        assertEquals(listOf(0, 2), search.titleReads.map { it.third })
        assertEquals(1, search.peopleReads.size)
        assertEquals(listOf("Movie:m1", "Series:s1", "Person:p1", "Episode:e1"), model.ready().hits.map(::searchHitKey))
        assertFalse(model.ready().hasMore)
    }

    @Test
    fun `the titles are asked for as movies, series and episodes, with no count`() {
        val model = model()
        model.setText("heat")
        assertEquals(setOf(ItemKind.Movie, ItemKind.Series, ItemKind.Episode), search.titleReads.single().second)
    }

    @Test
    fun `a chip reads its own type from the server, once, and a revisit reads nothing`() {
        val model = model()
        model.setText("heat")
        assertEquals(1, search.titleReads.size)
        model.selectType(SearchType.Movies)
        assertEquals(SearchType.Movies, model.state.value.type)
        assertEquals(listOf("Movie:m1"), model.ready().hits.map(::searchHitKey))
        assertEquals(setOf(ItemKind.Movie), search.titleReads.last().second)
        assertEquals(2, search.titleReads.size)
        model.selectType(SearchType.All)
        model.selectType(SearchType.Movies)
        assertEquals(2, search.titleReads.size)
    }

    @Test
    fun `a chip nobody opens reads nothing`() {
        val model = model()
        model.setText("heat")
        model.selectType(SearchType.Series)
        assertEquals(setOf(ItemKind.Movie, ItemKind.Series, ItemKind.Episode), search.titleReads.first().second)
        assertEquals(setOf(ItemKind.Series), search.titleReads.last().second)
        assertEquals(2, search.titleReads.size)
        assertEquals(1, search.peopleReads.size)
    }

    @Test
    fun `the people chip reads people from the server`() {
        val model = model()
        model.setText("heat")
        model.selectType(SearchType.People)
        assertEquals(listOf("Person:p1"), model.ready().hits.map(::searchHitKey))
        assertEquals(listOf("heat" to 0, "heat" to 0), search.peopleReads)
    }

    @Test
    fun `a new query starts on All, and a chip reads the new query only when it is next shown`() {
        val model = model()
        model.setText("heat")
        model.selectType(SearchType.Movies)
        val reads = search.titleReads.size
        model.setText("heat wave")
        assertEquals(SearchType.All, model.state.value.type)
        assertEquals(reads + 1, search.titleReads.size)
        model.selectType(SearchType.Movies)
        assertEquals("heat wave", search.titleReads.last().first)
        assertEquals(setOf(ItemKind.Movie), search.titleReads.last().second)
    }

    @Test
    fun `a chip pages by itself`() {
        search.titles = List(5) { BrowseItem("m$it", "Movie $it", ItemKind.Movie) } + series
        val model = model()
        model.setText("movie")
        model.selectType(SearchType.Movies)
        assertEquals(PAGE, model.ready().hits.size)
        assertTrue(model.ready().hasMore)
        model.loadMore()
        assertEquals(2 * PAGE, model.ready().hits.size)
        assertEquals(setOf(ItemKind.Movie), search.titleReads.last().second)
        assertEquals(2 * PAGE, search.titleReads.last().third + PAGE)
        model.loadMore()
        assertEquals(5, model.ready().hits.size)
        assertFalse(model.ready().hasMore)
    }

    @Test
    fun `the pages hold each chip's answer to the query, and a chip nobody has read has none`() {
        val model = model()
        model.setText("heat")
        assertEquals(setOf(SearchType.All), model.ready().pages.keys)
        assertEquals("heat", model.ready().query)
        model.selectType(SearchType.Movies)
        val ready = model.ready()
        assertEquals(setOf(SearchType.All, SearchType.Movies), ready.pages.keys)
        assertEquals(listOf("Movie:m1"), ready.pages.getValue(SearchType.Movies).map(::searchHitKey))
        assertEquals(listOf("Movie:m1"), ready.hits.map(::searchHitKey))
    }

    @Test
    fun `a chip's answer to an older query is not a page of the new one`() {
        val model = model()
        model.setText("heat")
        model.selectType(SearchType.Movies)
        model.setText("heat wave")
        assertEquals(setOf(SearchType.All), model.ready().pages.keys)
        assertEquals("heat wave", model.ready().query)
        model.selectType(SearchType.Movies)
        assertEquals(setOf(SearchType.All, SearchType.Movies), model.ready().pages.keys)
    }

    @Test
    fun `a chip being read has no page yet, with the chips and the other pages kept`() {
        val model = model()
        model.setText("heat")
        val gate = CompletableDeferred<Unit>()
        search.gate = gate
        model.selectType(SearchType.Series)
        val ready = model.ready()
        assertEquals(setOf(SearchType.All), ready.pages.keys)
        assertEquals(SearchType.Series, model.state.value.type)
        assertTrue(ready.hits.isEmpty())
        gate.complete(Unit)
        assertEquals(setOf(SearchType.All, SearchType.Series), model.ready().pages.keys)
    }

    @Test
    fun `a chip with nothing of its type keeps the chips and says nothing is here`() {
        search.titles = listOf(movie)
        val model = model()
        model.setText("heat")
        model.selectType(SearchType.Episodes)
        val ready = model.ready()
        assertTrue(ready.hits.isEmpty())
        assertFalse(ready.hasMore)
    }

    @Test
    fun `a failed chip is a failure, and retry reads that chip again`() {
        val model = model()
        model.setText("heat")
        search.failure = IOException("down")
        model.selectType(SearchType.Series)
        assertEquals(SearchBody.Failed(ServerFailure.Unreachable()), model.body())
        search.failure = null
        model.retry()
        assertEquals(listOf("Series:s1"), model.ready().hits.map(::searchHitKey))
    }

    @Test
    fun `editing the text without changing the query keeps the chosen type`() {
        val model = model()
        model.setText("heat")
        model.selectType(SearchType.Movies)
        model.setText("heat ")
        assertEquals(SearchType.Movies, model.state.value.type)
    }

    @Test
    fun `a search that finds nothing says so`() {
        search.titles = emptyList()
        search.people = emptyList()
        val model = model()
        model.setText("zzz")
        assertEquals(SearchBody.NoResults("zzz"), model.body())
    }

    @Test
    fun `people that cannot be searched leave the titles`() {
        search.peopleFailure = IOException("down")
        val model = model()
        model.setText("heat")
        assertEquals(listOf("Movie:m1", "Series:s1"), model.ready().hits.map(::searchHitKey))
    }

    @Test
    fun `a failed search is a failure, and retry reads it again`() {
        search.failure = IOException("down")
        val model = model()
        model.setText("heat")
        assertEquals(SearchBody.Failed(ServerFailure.Unreachable()), model.body())
        search.failure = null
        model.retry()
        assertTrue(model.body() is SearchBody.Ready)
    }

    @Test
    fun `refreshing is a retry and a search has no refresh state`() {
        search.failure = IOException("down")
        val model = model()
        model.setText("heat")
        search.failure = null
        model.refresh()
        assertTrue(model.body() is SearchBody.Ready)
        assertFalse(model.refreshing.value)
    }

    @Test
    fun `clearing the field goes back to the recents and to All`() {
        val model = model()
        model.setText("heat")
        model.selectType(SearchType.Movies)
        model.clear()
        assertEquals("", model.state.value.text)
        assertEquals(SearchType.All, model.state.value.type)
        assertTrue(model.body() is SearchBody.Idle)
    }

    @Test
    fun `a query that found something is remembered, and picking a recent searches it now`() {
        val model = model()
        model.setText("heat")
        assertEquals(listOf("heat", "alien"), history.kept.value)
        model.setText("")
        model.pickRecent("alien")
        assertEquals("alien", model.state.value.text)
        assertEquals("alien", search.titleReads.last().first)
    }

    @Test
    fun `the recents can be forgotten`() {
        val model = model()
        model.clearRecents()
        assertEquals(SearchBody.Idle(emptyList()), model.body())
    }

    @Test
    fun `what the field held before a process death is searched when the tab starts`() {
        val model = model(initialQuery = "heat")
        assertTrue(search.titleReads.isEmpty())
        model.start()
        assertEquals("heat", model.state.value.text)
        assertTrue(model.body() is SearchBody.Ready)
    }

    @Test
    fun `starting with no saved query searches nothing`() {
        val model = model()
        model.start()
        assertTrue(model.body() is SearchBody.Idle)
    }

    @Test
    fun `a result's key names its kind and id, and a key it did not make is not read`() {
        assertEquals(SearchKind.Series to "s1", parseSearchHitKey(searchHitKey(SearchHit.Title(series))))
        assertEquals(SearchKind.Person to "p1", parseSearchHitKey(searchHitKey(SearchHit.Person(person))))
        assertEquals(SearchKind.Episode, SearchHit.Title(episode).kind)
        assertEquals(SearchKind.Movie, SearchHit.Title(movie.copy(kind = ItemKind.BoxSet)).kind)
        assertEquals(null, parseSearchHitKey("nonsense"))
        assertEquals(SearchType.All, SearchType.of(null))
        assertEquals(SearchType.Episodes, SearchType.of(SearchKind.Episode))
    }

    private companion object {
        const val PAGE = 2
    }
}
