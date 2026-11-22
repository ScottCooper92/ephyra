package com.cooper.homelab.ephyra.server

import com.cooper.homelab.core.foundation.ServerFailure
import com.cooper.homelab.core.server.network.CleartextRefusedException
import com.cooper.homelab.core.server.network.LocalNetworkDeniedException
import com.cooper.homelab.core.server.network.UnreadableAnswerException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownServiceException

/** A call that got no answer is read by the shared library, so a signed-in call says what went wrong and not only "Unreachable". */
class ServerFailureTest {
    private fun failure(error: Throwable) = runBlocking { error.serverFailure() }

    @Test
    fun `a cleartext block is told apart from a server that is down`() {
        assertEquals(ServerFailure.CleartextBlocked, failure(CleartextRefusedException("nas.example.com")))
        assertEquals(ServerFailure.CleartextBlocked, failure(UnknownServiceException("CLEARTEXT communication not permitted")))
    }

    @Test
    fun `a refusal of the local network names itself`() {
        assertEquals(ServerFailure.LocalNetworkDenied, failure(LocalNetworkDeniedException("192.168.1.2")))
    }

    @Test
    fun `an answer that is not this kind of server is unreadable`() {
        assertEquals(ServerFailure.Unreadable, failure(UnreadableAnswerException()))
        assertEquals(ServerFailure.Unreadable, failure(UnreadableAnswerException(IllegalStateException("not json"))))
    }

    @Test
    fun `a timeout and a plain connect failure are unreachable`() {
        assertEquals(ServerFailure.Unreachable(timedOut = true), failure(SocketTimeoutException("timeout")))
        assertEquals(ServerFailure.Unreachable(), failure(IOException("connection reset")))
    }

    @Test
    fun `the server's own refusal is a status and not a transport failure`() {
        assertEquals(ServerFailure.SessionRejected, failure(JellyfinHttpException(401, "/x")))
    }
}
