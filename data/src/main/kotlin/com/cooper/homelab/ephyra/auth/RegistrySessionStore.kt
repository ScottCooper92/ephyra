package com.cooper.homelab.ephyra.auth

import com.cooper.homelab.core.server.address.ServerAddress
import com.cooper.homelab.core.server.connection.DeviceIdentity
import com.cooper.homelab.core.server.connection.SavedServer
import com.cooper.homelab.core.server.connection.ServerRegistry
import com.cooper.homelab.core.server.connection.SingleServerStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.cooper.homelab.core.server.connection.CredentialStore as LoginStore

/** The word this app gives the saved server's `kind`. The library reads nothing into it. */
internal const val JELLYFIN_KIND = "jellyfin"

/**
 * The saved session as one server and one user, over the shared library's [SingleServerStore]: the active server of the registry and
 * its login. This app has no switcher, so [save] replaces that server and [clear] removes it. The store keeps writes one at a time,
 * writes the login before the server, runs [importer] once before anything reads or writes, and decrypts on [io]; this adds the
 * mapping to a [JellyfinSession] and the device id, which the import moves.
 */
class RegistrySessionStore(
    registry: ServerRegistry,
    logins: LoginStore<JellyfinLogin>,
    importer: LegacySessionImport,
    serverIdentity: ServerIdentity,
    private val device: DeviceIdentity,
    io: CoroutineDispatcher,
) : SessionStore {
    private val store = SingleServerStore(registry, logins, JELLYFIN_KIND, serverIdentity::currentServerId, io, importer::importOnce)

    override val session: Flow<JellyfinSession?> =
        store.active.map { active -> active?.login?.let { sessionOf(active.server, it) } }.distinctUntilChanged()

    override suspend fun current(): JellyfinSession? = session.first()

    override suspend fun save(session: JellyfinSession): Boolean {
        val address = ServerAddress.parse(session.baseUrl) ?: return false
        val login = JellyfinLogin(session.serverId, session.serverVersion, session.userId, session.userName, session.token)
        return store.save(address, session.serverName.ifBlank { address.host }, login)
    }

    override suspend fun clear() = store.clear()

    override suspend fun deviceId(): String {
        store.awaitImport()
        return device.id()
    }

    private fun sessionOf(
        server: SavedServer,
        login: JellyfinLogin,
    ) = JellyfinSession(
        // The app keeps its base URL without the trailing slash the library's canonical form ends in.
        baseUrl = server.address.url.trimEnd('/'),
        serverName = server.name,
        serverId = login.serverId,
        serverVersion = login.serverVersion,
        userId = login.userId,
        userName = login.userName,
        token = login.token,
    )
}
