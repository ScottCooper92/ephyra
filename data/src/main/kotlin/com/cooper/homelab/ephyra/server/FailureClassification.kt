package com.cooper.homelab.ephyra.server

import com.cooper.homelab.core.foundation.ServerFailure
import com.cooper.homelab.core.server.network.FailureMapper
import com.cooper.homelab.core.server.network.JvmTransportFailures
import com.cooper.homelab.core.server.network.NoBody
import com.cooper.homelab.core.server.network.RequestFacts
import java.security.cert.CertificateException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLPeerUnverifiedException

/** A call that did not get an answer is read by the shared library: a cleartext block, a local-network denial, an unreadable answer, a client-certificate request, a timeout, or a plain connect failure. */
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
        mapper.fromThrowable(this).let { if (it == ServerFailure.Unknown && isCertificateFailure()) ServerFailure.Unreachable() else it }
    }

/**
 * A handshake that failed over the server's certificate: a peer the platform could not verify, or a TLS failure with a certificate
 * exception in its cause chain. The shared library cannot name it without the certificate, which this app has no trust manager to
 * read, so it would be `Unknown`; the user can only act on "couldn't reach it", as before.
 */
private fun Throwable.isCertificateFailure(): Boolean =
    this is SSLPeerUnverifiedException ||
        (this is SSLException && generateSequence(cause) { it.cause }.take(MAX_CAUSE_DEPTH).any { it is CertificateException })

private const val MAX_CAUSE_DEPTH = 8
