package com.cooper.homelab.ephyra.server

import com.cooper.homelab.core.server.connection.CallOutcome
import com.cooper.homelab.core.server.connection.CallResult
import org.junit.Assert.assertEquals
import org.junit.Test

class GatewayClassifierTest {
    private fun answered(
        status: Int,
        rejected: Boolean = false,
    ) = CallResult.Answered(status, sessionRejected = rejected)

    @Test
    fun `a call its caller cancelled says nothing about the server`() {
        assertEquals(CallOutcome.Ignore, GatewayClassifier.classify(CallResult.TransportFailure(cancelled = true)))
    }

    @Test
    fun `only a transport failure and 502 503 504 are no answer`() {
        assertEquals(CallOutcome.NoAnswer, GatewayClassifier.classify(CallResult.TransportFailure()))
        assertEquals(CallOutcome.NoAnswer, GatewayClassifier.classify(answered(502)))
        assertEquals(CallOutcome.NoAnswer, GatewayClassifier.classify(answered(503)))
        assertEquals(CallOutcome.NoAnswer, GatewayClassifier.classify(answered(504)))
    }

    @Test
    fun `any other answer means the server was reached`() {
        assertEquals(CallOutcome.Answered, GatewayClassifier.classify(answered(200)))
        assertEquals(CallOutcome.Answered, GatewayClassifier.classify(answered(404)))
        assertEquals(CallOutcome.Answered, GatewayClassifier.classify(answered(429)))
        assertEquals(CallOutcome.Answered, GatewayClassifier.classify(answered(500)))
    }

    @Test
    fun `it never reports a refused token, even for one the app found gone`() {
        assertEquals(CallOutcome.Answered, GatewayClassifier.classify(answered(401, rejected = true)))
        assertEquals(CallOutcome.Answered, GatewayClassifier.classify(answered(403, rejected = true)))
    }
}
