package com.cooper.homelab.ephyra

import androidx.test.core.app.ApplicationProvider
import com.cooper.homelab.ephyra.auth.JellyfinSession
import com.cooper.homelab.ephyra.demo.DEMO_ORIGIN
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Analytics marks the events of a demo session, so they are not counted as use of a real server (#1013). */
@RunWith(RobolectricTestRunner::class)
class DemoAnalyticsWiringTest {
    private val sessions = testSessionStore()
    private val graph = AppGraph(ApplicationProvider.getApplicationContext(), sessionsForTest = sessions)

    private fun session(baseUrl: String) =
        JellyfinSession(
            baseUrl = baseUrl,
            serverName = "server",
            serverId = "server-1",
            serverVersion = "10.11.6",
            userId = "user-1",
            userName = "Claude",
            token = "token-1",
        )

    private fun awaitRead() = runBlocking { withTimeout(TIMEOUT_MILLIS) { graph.demoSession.awaitRead() } }

    @Test
    fun `nothing saved is not the demo, and the read still ends so analytics is not held`() {
        awaitRead()
        assertFalse(graph.demoSession.isDemo)
    }

    @Test
    fun `a saved demo session is the demo`() {
        runBlocking { sessions.save(session(DEMO_ORIGIN)) }
        awaitRead()
        runBlocking { withTimeout(TIMEOUT_MILLIS) { while (!graph.demoSession.isDemo) kotlinx.coroutines.delay(POLL_MILLIS) } }
        assertTrue(graph.demoSession.isDemo)
    }

    @Test
    fun `a saved session to a real server is not the demo`() {
        runBlocking { sessions.save(session("https://jellyfin.example.com")) }
        awaitRead()
        runBlocking { withTimeout(TIMEOUT_MILLIS) { kotlinx.coroutines.delay(SETTLE_MILLIS) } }
        assertFalse(graph.demoSession.isDemo)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000L
        const val POLL_MILLIS = 10L
        const val SETTLE_MILLIS = 200L
    }
}
