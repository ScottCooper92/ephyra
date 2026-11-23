package com.cooper.homelab.ephyra.server

import com.cooper.homelab.core.server.connection.AuthFailureClassifier
import com.cooper.homelab.core.server.connection.CallOutcome
import com.cooper.homelab.core.server.connection.CallResult

private const val HTTP_BAD_GATEWAY = 502
private const val HTTP_SERVICE_UNAVAILABLE = 503
private const val HTTP_GATEWAY_TIMEOUT = 504

/**
 * This app's rules for what a call says about the server. Only a transport failure, or a gateway that says the server behind it is
 * down (502, 503 or 504, as a reverse proxy answers when the server is stopped), is no answer. Any other response, an error
 * included, means the server was reached: a server that answers with a failure is "couldn't load", not "can't reach". A refused
 * token is handled elsewhere, so this never reports one.
 */
internal object GatewayClassifier : AuthFailureClassifier {
    override fun classify(result: CallResult): CallOutcome =
        when (result) {
            is CallResult.TransportFailure -> if (result.cancelled) CallOutcome.Ignore else CallOutcome.NoAnswer
            is CallResult.Answered ->
                when (result.status) {
                    HTTP_BAD_GATEWAY, HTTP_SERVICE_UNAVAILABLE, HTTP_GATEWAY_TIMEOUT -> CallOutcome.NoAnswer
                    else -> CallOutcome.Answered
                }
        }
}
