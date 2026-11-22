package com.cooper.homelab.ephyra.ui.search

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cooper.homelab.core.foundation.ServerId
import com.cooper.homelab.core.presentation.search.DataStoreRecentSearches
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.cooper.homelab.core.presentation.search.RecentSearches as SharedRecentSearches

/** The key (and the prefix of every per-server key) this app has always written. An on-disk name: it never changes. */
private const val KEY_PREFIX = "recent_entries"

/**
 * The entries the user last opened from the settings search, newest first, kept on this device alone: the shared library's
 * [DataStoreRecentSearches] for the one server this install is signed in to.
 *
 * The server's id is only known inside a suspend call, so the store is built on first use rather than at construction, and nothing
 * here blocks. That first use also moves the list this app kept before it had a per-server key (`recent_entries`) to the server's own
 * key, once, and deletes the old key, so the old key is never read again.
 */
class ServerRecentSearches(
    private val dataStore: DataStore<Preferences>,
    private val currentServerId: suspend () -> ServerId,
) : SharedRecentSearches {
    private val opening = Mutex()
    private var store: SharedRecentSearches? = null

    override val ids: Flow<List<String>> = flow { emitAll(store().ids) }

    override suspend fun remember(id: String) = store().remember(id)

    override suspend fun clear() = store().clear()

    private suspend fun store(): SharedRecentSearches =
        store ?: opening.withLock {
            store ?: open().also { store = it }
        }

    private suspend fun open(): SharedRecentSearches {
        val serverId = currentServerId()
        val legacy = stringPreferencesKey(KEY_PREFIX)
        val own = stringPreferencesKey("$KEY_PREFIX.${serverId.value}")
        dataStore.edit { prefs ->
            val old = prefs[legacy] ?: return@edit
            if (prefs[own] == null) prefs[own] = old
            prefs.remove(legacy)
        }
        return DataStoreRecentSearches(dataStore, serverId, KEY_PREFIX)
    }
}
