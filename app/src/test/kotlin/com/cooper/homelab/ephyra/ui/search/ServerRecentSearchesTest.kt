package com.cooper.homelab.ephyra.ui.search

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cooper.homelab.core.foundation.ServerId
import com.cooper.homelab.core.presentation.search.MAX_RECENT
import com.cooper.homelab.core.testing.InMemoryDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerRecentSearchesTest {
    private val legacy = stringPreferencesKey("recent_entries")
    private val own = stringPreferencesKey("recent_entries.home")
    private var asked = 0

    private fun recents(file: InMemoryDataStore = InMemoryDataStore()) =
        ServerRecentSearches(file) {
            asked++
            ServerId("home")
        }

    @Test
    fun `it starts empty, and does not ask for the server id until it is used`() =
        runBlocking {
            val recent = recents()
            assertEquals(0, asked)
            assertEquals(emptyList<String>(), recent.ids.first())
            assertEquals(1, asked)
        }

    @Test
    fun `the newest is first, and one opened again moves up rather than repeating`() =
        runBlocking {
            val recent = recents()
            listOf("a", "b", "c", "a").forEach { recent.remember(it) }
            assertEquals(listOf("a", "c", "b"), recent.ids.first())
        }

    @Test
    fun `only the last few stay`() =
        runBlocking {
            val recent = recents()
            (1..MAX_RECENT + 3).forEach { recent.remember("e$it") }
            assertEquals((MAX_RECENT + 3 downTo 4).map { "e$it" }, recent.ids.first())
        }

    @Test
    fun `clear forgets them all`() =
        runBlocking {
            val recent = recents()
            recent.remember("a")
            recent.clear()
            assertEquals(emptyList<String>(), recent.ids.first())
        }

    @Test
    fun `the list kept before there was a per-server key moves to the server once, and the old key is deleted`() =
        runBlocking {
            val file = InMemoryDataStore(mutablePreferencesOf(legacy to "server.GENERAL\nlicences"))
            val recent = recents(file)
            assertEquals(listOf("server.GENERAL", "licences"), recent.ids.first())
            assertNull(file.data.value[legacy])
            assertEquals("server.GENERAL\nlicences", file.data.value[own])
        }

    @Test
    fun `the old key is not read again once it has moved`() =
        runBlocking {
            val file = InMemoryDataStore(mutablePreferencesOf(legacy to "server.GENERAL"))
            val first = recents(file)
            first.remember("this-app")
            assertEquals(listOf("this-app", "server.GENERAL"), first.ids.first())
            file.updateData { it.toMutablePreferences().apply { this[legacy] = "stale" } }
            first.remember("another")
            assertEquals(listOf("another", "this-app", "server.GENERAL"), first.ids.first())
        }

    @Test
    fun `a list the server already has is not overwritten by the old key`() =
        runBlocking {
            val file = InMemoryDataStore(mutablePreferencesOf(legacy to "old", own to "current"))
            val recent = recents(file)
            assertEquals(listOf("current"), recent.ids.first())
            assertNull(file.data.value[legacy])
        }
}
