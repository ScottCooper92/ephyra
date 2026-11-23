package com.cooper.homelab.ephyra.ui.console

import androidx.lifecycle.ViewModel
import com.cooper.homelab.core.testing.DiscardLogger
import com.cooper.homelab.ephyra.FakeJellyfin
import com.cooper.homelab.ephyra.server.Connection
import com.cooper.homelab.ephyra.session
import com.cooper.homelab.ephyra.testSessionStore
import com.cooper.homelab.ephyra.ui.ViewModelTeardown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CopyOnWriteArrayList

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ApproveCodeViewModelTest {
    private val jellyfin = FakeJellyfin()
    private val sessions = testSessionStore()

    private val teardown = ViewModelTeardown()
    private val connection =
        teardown.made(
            Connection(OkHttpClient(), sessions, "Jellyfin Companion", "Test", "1.0", Dispatchers.IO, DiscardLogger),
        )

    private fun <T : ViewModel> made(viewModel: T): T = teardown.made(viewModel)

    @Before
    fun main() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        runBlocking { sessions.save(jellyfin.session()) }
    }

    /** Listens to [Connection.refused] for a test, and stops when it ends. */
    private val listening = CoroutineScope(Dispatchers.Unconfined)

    @After
    fun close() {
        listening.cancel()
        teardown.close(jellyfin)
    }

    /** What an approval left: the form, and the snackbar's one-shot outcome (none for a code the field says is not waiting). */
    private data class Result(
        val state: ApproveUiState,
        val outcome: ApproveOutcome?,
    )

    private fun ApproveCodeViewModel.resultAfter(code: String): Result {
        onCode(code)
        approve()
        return runBlocking {
            withTimeout(TIMEOUT_MILLIS) {
                val done = state.first { !it.approving }
                Result(done, if (done.wrongCode) null else events.first())
            }
        }
    }

    @Test
    fun `the field keeps six digits, and Approve waits for all six`() {
        val vm = made(ApproveCodeViewModel(connection))
        vm.onCode("65-43 21 9")
        assertEquals("654321", vm.state.value.code)
        vm.onCode("123")
        assertFalse(vm.state.value.complete)
        vm.approve()
        assertFalse(vm.state.value.wrongCode)
        assertEquals(0, jellyfin.hits("/QuickConnect/Authorize"))
    }

    @Test
    fun `a waiting code is approved and the field empties for the next`() {
        val result = made(ApproveCodeViewModel(connection)).resultAfter("654321")
        assertEquals(ApproveOutcome.Approved, result.outcome)
        assertEquals("", result.state.code)
    }

    @Test
    fun `only an approved code counts towards the review prompt`() {
        var counted = 0
        made(ApproveCodeViewModel(connection) { counted++ }).resultAfter("111111")
        assertEquals(0, counted)
        made(ApproveCodeViewModel(connection) { counted++ }).resultAfter("654321")
        assertEquals(1, counted)
    }

    @Test
    fun `a code the server is not waiting on says so and stays to be corrected`() {
        val result = made(ApproveCodeViewModel(connection)).resultAfter("111111")
        assertTrue(result.state.wrongCode)
        assertNull(result.outcome)
        assertEquals("111111", result.state.code)
    }

    @Test
    fun `Quick Connect turned off, another failure, an unreachable server and no session each say so`() {
        jellyfin.status["/QuickConnect/Authorize"] = 401
        assertEquals(ApproveOutcome.QuickConnectOff, made(ApproveCodeViewModel(connection)).resultAfter("654321").outcome)
        jellyfin.status["/QuickConnect/Authorize"] = 500
        assertEquals(ApproveOutcome.Failed, made(ApproveCodeViewModel(connection)).resultAfter("654321").outcome)
        jellyfin.close()
        assertEquals(ApproveOutcome.Unreachable, made(ApproveCodeViewModel(connection)).resultAfter("654321").outcome)
        runBlocking { sessions.clear() }
        assertEquals(ApproveOutcome.Unreachable, made(ApproveCodeViewModel(connection)).resultAfter("654321").outcome)
    }

    @Test
    fun `a 401 with the session still good is Quick Connect turned off, and nothing is reported`() {
        jellyfin.status["/QuickConnect/Authorize"] = 401
        val refused = refusals()
        assertEquals(ApproveOutcome.QuickConnectOff, made(ApproveCodeViewModel(connection)).resultAfter("654321").outcome)
        assertEquals(1, jellyfin.hits("/Users/Me"))
        assertTrue(refused.isEmpty())
    }

    @Test
    fun `a 401 the session can't pass is a refused token, reported for the home to sign in again`() {
        jellyfin.status["/QuickConnect/Authorize"] = 401
        jellyfin.status["/Users/Me"] = 401
        val refused = refusals()
        made(ApproveCodeViewModel(connection)).resultAfter("654321")
        runBlocking { withTimeout(TIMEOUT_MILLIS) { while (refused.isEmpty()) delay(POLL_MILLIS) } }
        assertEquals(1, refused.size)
    }

    /** What [Connection.refused] reports from here on. */
    private fun refusals(): MutableList<Unit> {
        val seen = CopyOnWriteArrayList<Unit>()
        listening.launch { connection.refused.collect { seen += it } }
        return seen
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000L
        const val POLL_MILLIS = 10L
    }
}
