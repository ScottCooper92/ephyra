package com.cooper.homelab.ephyra.server

import com.cooper.homelab.core.foundation.ServerFailure
import com.cooper.homelab.core.server.network.FailureMapper
import com.cooper.homelab.core.server.network.JvmTransportFailures
import com.cooper.homelab.core.server.network.NoBody
import com.cooper.homelab.core.server.network.RequestFacts

/** A call that did not get an answer is read by the shared library: a cleartext block, a local-network denial, an untrusted certificate, an unreadable answer, a client-certificate request, a timeout, or a plain connect failure. */
private val mapper = FailureMapper(JvmTransportFailures)

/**
 * Why a call to the server failed, by the shared library's rules: a 5xx is the server failing (worth another try), a 401 or
 * 403 is not permitted, a 404 is gone, any other answer is a refusal, and anything that is not an answer is unreachable. The
 * server's own words stay in [JellyfinHttpException.reason]; this keeps no text. A cancellation is rethrown.
 */
suspend fun Throwable.serverFailure(): ServerFailure =
    if (this is JellyfinHttpException) {
        mapper.fromStatus(code, NoBody, RequestFacts(method = "PUT", path = ""))
    } else {
        mapper.fromThrowable(this)
    }
