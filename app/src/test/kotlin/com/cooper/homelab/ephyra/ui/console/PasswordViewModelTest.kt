package com.cooper.homelab.ephyra.ui.console

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cooper.homelab.core.testing.DiscardLogger
import com.cooper.homelab.ephyra.FakeJellyfin
import com.cooper.homelab.ephyra.server.Connection
import com.cooper.homelab.ephyra.session
import com.cooper.homelab.ephyra.testSessionStore
import com.cooper.homelab.ephyra.ui.ViewModelTeardown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PasswordViewModelTest {
    private val jellyfin = FakeJellyfin()
    private val sessions = testSessionStore()
    private lateinit var connection: Connection

    private val teardown = ViewModelTeardown()

    private fun <T : ViewModel> made(viewModel: T): T = teardown.made(viewModel)

    @Before
    fun main() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        runBlocking { sessions.save(jellyfin.session()) }
        connection = watching(Connection(OkHttpClient(), sessions, "Jellyfin Companion", "Test", "1.0", Dispatchers.IO, DiscardLogger))
    }

    /** Closed with the test, and started watching before the test saves anything (#165, #502). */
    private fun watching(built: Connection): Connection {
        teardown.made(built)
        runBlocking { withTimeout(TIMEOUT_MILLIS) { built.awaitWatching() } }
        return built
    }

    @After
    fun close() = teardown.close(jellyfin)

    private fun PasswordViewModel.fill(
        current: String,
        new: String,
        confirm: String = new,
    ): PasswordViewModel {
        onCurrent(current)
        onNew(new)
        onConfirm(confirm)
        return this
    }

    /** What a change left: the form, and the snackbar's one-shot outcome (none where a field says what was wrong). */
    private data class Result(
        val state: PasswordUiState,
        val outcome: PasswordOutcome?,
    )

    private fun PasswordViewModel.resultOfChange(): Result {
        change()
        return runBlocking {
            withTimeout(TIMEOUT_MILLIS) {
                val done = state.first { !it.changing }
                Result(done, if (done.fieldError != null) null else events.first())
            }
        }
    }

    @Test
    fun `a change works, for this user, and empties the form`() {
        val after = made(PasswordViewModel(connection)).fill("old-secret", "new-secret").resultOfChange()
        assertEquals(PasswordUiState(), after.state)
        assertEquals(PasswordOutcome.Changed, after.outcome)
        assertEquals("new-secret", jellyfin.password)
        assertEquals(
            "user-1",
            jellyfin.requests
                .last { it.url.encodedPath == "/Users/Password" }
                .url
                .queryParameter("userId"),
        )
    }

    @Test
    fun `a wrong current password is refused in place, the form kept`() {
        val after = made(PasswordViewModel(connection)).fill("guess", "new-secret").resultOfChange()
        assertEquals(PasswordFieldError.WrongCurrent, after.state.fieldError)
        assertNull(after.outcome)
        assertEquals("guess", after.state.current)
        assertFalse(after.state.changing)
        assertEquals("old-secret", jellyfin.password)
    }

    @Test
    fun `new passwords that don't match never reach the server`() {
        val after = made(PasswordViewModel(connection)).fill("old-secret", "new-secret", "new-secert").resultOfChange()
        assertEquals(PasswordFieldError.Mismatch, after.state.fieldError)
        assertEquals(0, jellyfin.hits("/Users/Password"))
    }

    @Test
    fun `Change waits for the new password twice, and an edit clears the result`() {
        val vm = made(PasswordViewModel(connection)).fill("", "new-secret", "")
        assertFalse(vm.state.value.complete)
        vm.change()
        assertEquals(0, jellyfin.hits("/Users/Password"))
        vm.onConfirm("other")
        assertEquals(PasswordFieldError.Mismatch, vm.resultOfChange().state.fieldError)
        vm.onConfirm("new-secret")
        assertNull(vm.state.value.fieldError)
    }

    @Test
    fun `a server failure and an unreachable one say which`() {
        jellyfin.status["/Users/Password"] = 500
        assertEquals(PasswordOutcome.Refused, made(PasswordViewModel(connection)).fill("old-secret", "n").resultOfChange().outcome)
        runBlocking { sessions.clear() }
        val signedOut = watching(Connection(OkHttpClient(), sessions, "Jellyfin Companion", "Test", "1.0", Dispatchers.IO, DiscardLogger))
        assertEquals(PasswordOutcome.Unreachable, made(PasswordViewModel(signedOut)).fill("old-secret", "n").resultOfChange().outcome)
    }

    @Test
    fun `closing the page right after submitting still sends the change`() {
        val store = ViewModelStore()
        val vm =
            ViewModelProvider(
                object : ViewModelStoreOwner {
                    override val viewModelStore = store
                },
                viewModelFactory { initializer { made(PasswordViewModel(connection)) } },
            )[PasswordViewModel::class.java]
        vm.fill("old-secret", "new-secret").change()
        store.clear()
        runBlocking { connection.awaitSaves() }
        assertEquals("new-secret", jellyfin.password)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000L
    }
}
