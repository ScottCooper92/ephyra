package com.cooper.homelab.ephyra.server

import com.cooper.homelab.core.testing.DiscardLogger
import com.cooper.homelab.ephyra.auth.JellyfinSession
import com.cooper.homelab.ephyra.auth.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient

private const val WATCHING_WAIT_MILLIS = 10_000L

/**
 * A [Connection] built the way the app builds it, with the session already saved. The connection's session watcher takes the first
 * session it reads as its baseline. A test that builds the connection first and saves the session after can have the watcher read
 * none, then see the saved session as a change and cancel the saves in flight: a write the test starts at once never reaches the
 * server and the test times out (#899). Here the session is saved first, and this returns once the watcher has read it. The caller
 * owns the connection and closes it, as with any other.
 */
suspend fun connectionWithSession(
    sessions: SessionStore,
    session: JellyfinSession,
    http: OkHttpClient = OkHttpClient(),
): Connection {
    sessions.save(session)
    val connection = Connection(http, sessions, "Jellyfin Companion", "Test", "1.0", Dispatchers.IO, DiscardLogger)
    withTimeout(WATCHING_WAIT_MILLIS) { connection.awaitWatching() }
    return connection
}
