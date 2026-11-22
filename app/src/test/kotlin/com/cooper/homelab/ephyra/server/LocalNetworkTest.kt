package com.cooper.homelab.ephyra.server

import com.cooper.homelab.core.server.network.LocalNetworkDeniedException
import com.cooper.homelab.core.server.network.LocalNetworkPermission
import com.cooper.homelab.core.server.network.isLocalNetworkHost
import com.cooper.homelab.ephyra.ui.ConnectionHealth
import com.cooper.homelab.ephyra.ui.SetupError
import com.cooper.homelab.ephyra.ui.monitoredUnreachable
import com.cooper.homelab.ephyra.ui.refreshHealth
import com.cooper.homelab.ephyra.ui.unreachableSetup
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLPeerUnverifiedException

/** Which servers need the local-network permission, and what a failed connection to each says about it. */
class LocalNetworkTest {
    private val denied = LocalNetworkPermission { false }
    private val granted = LocalNetworkPermission { true }

    @Test
    fun `private, link-local and unique-local addresses, home names and single-label names need the permission`() {
        listOf(
            "10.0.0.5",
            "172.16.4.1",
            "172.31.255.255",
            "192.168.1.20",
            "169.254.3.4",
            "fd12:3456::1",
            "nas.local",
            "nas.lan",
            "router.home.arpa",
            "box.internal",
            "nas",
        ).forEach { assertTrue("$it should need it", isLocalNetworkHost(it)) }
    }

    @Test
    fun `public hosts, loopback and tailnet addresses do not`() {
        listOf(
            "203.0.113.9",
            "172.32.0.1",
            "jellyfin.example.com",
            "localhost",
            "127.0.0.1",
            "100.101.102.103",
            "fd7a:115c:a1e0::1",
            "nas.tail1234.ts.net",
        ).forEach { assertFalse("$it should not need it", isLocalNetworkHost(it)) }
    }

    @Test
    fun `a literal the kit reads strictly is a name, not an address, so a leading zero or a sign does not make a host local`() {
        // "010.0.0.1" and "+10.0.0.1" are not canonical literals: a resolver may read the zero as octal, so they are names, and a
        // name with a dot and no home suffix does not need the permission.
        listOf("010.0.0.1", "+10.0.0.1", "192.168.01.1", "fe80::1%eth0").forEach { assertFalse("$it is a name", isLocalNetworkHost(it)) }
        // A trailing dot on a real literal is still the literal.
        assertTrue(isLocalNetworkHost("10.0.0.1."))
    }

    @Test
    fun `a server address says its host only when reaching it needs the permission`() {
        assertEquals("192.168.1.2", "http://192.168.1.2:8096".localNetworkHostOrNull())
        assertEquals("nas.local", "http://nas.local:8096".localNetworkHostOrNull())
        assertNull("https://jellyfin.example.com".localNetworkHostOrNull())
        assertNull("not a url".localNetworkHostOrNull())
        assertNotNull("http://[fd12:3456::1]:8096".localNetworkHostOrNull())
    }

    @Test
    fun `a local server that did not answer, without the permission, is the permission's fault`() {
        assertEquals(ConnectionHealth.LocalNetworkDenied, monitoredUnreachable("http://192.168.1.2:8096", denied))
        assertEquals(ConnectionHealth.LocalNetworkDenied, monitoredUnreachable("http://nas.local:8096", denied))
    }

    @Test
    fun `with the permission, or for a server it does not cover, a silent server is just unreachable`() {
        assertEquals(ConnectionHealth.Unreachable, monitoredUnreachable("http://192.168.1.2:8096", granted))
        assertEquals(ConnectionHealth.Unreachable, monitoredUnreachable("https://jellyfin.example.com", denied))
        assertEquals(ConnectionHealth.Unreachable, monitoredUnreachable("http://100.101.102.103:8096", denied))
    }

    @Test
    fun `a refresh that failed names the local network only when the interceptor did`() =
        runBlocking {
            assertEquals(ConnectionHealth.LocalNetworkDenied, LocalNetworkDeniedException("192.168.1.2").refreshHealth())
            assertEquals(ConnectionHealth.Unreachable, IOException("connection reset").refreshHealth())
        }

    @Test
    fun `a certificate the device refuses is told apart from a server that is down`() =
        runBlocking {
            assertEquals(ConnectionHealth.CertificateRefused, SSLPeerUnverifiedException("Hostname jf.lan not verified").refreshHealth())
            assertEquals(SetupError.CertificateRefused, SSLPeerUnverifiedException("Hostname jf.lan not verified").unreachableSetup())
            assertEquals(ConnectionHealth.Unreachable, SSLException("connection closed by peer").refreshHealth())
        }

    @Test
    fun `a setup call that failed names the local network only when the interceptor did`() =
        runBlocking {
            assertEquals(SetupError.LocalNetworkDenied, LocalNetworkDeniedException("192.168.1.2").unreachableSetup())
            assertEquals(SetupError.Unreachable, IOException("connection reset").unreachableSetup())
        }
}
