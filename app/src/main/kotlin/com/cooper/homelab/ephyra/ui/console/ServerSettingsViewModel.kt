package com.cooper.homelab.ephyra.ui.console

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binge.companion.sdk.attempt
import com.cooper.homelab.ephyra.server.Branding
import com.cooper.homelab.ephyra.server.CUSTOM_CSS
import com.cooper.homelab.ephyra.server.Connection
import com.cooper.homelab.ephyra.server.Country
import com.cooper.homelab.ephyra.server.Culture
import com.cooper.homelab.ephyra.server.LOGIN_DISCLAIMER
import com.cooper.homelab.ephyra.server.METADATA_COUNTRY
import com.cooper.homelab.ephyra.server.METADATA_LANGUAGE
import com.cooper.homelab.ephyra.server.QUICK_CONNECT_AVAILABLE
import com.cooper.homelab.ephyra.server.SERVER_NAME
import com.cooper.homelab.ephyra.server.SPLASHSCREEN_ENABLED
import com.cooper.homelab.ephyra.server.SPLASHSCREEN_LOCATION
import com.cooper.homelab.ephyra.server.ServerSettings
import com.cooper.homelab.ephyra.server.branding
import com.cooper.homelab.ephyra.server.brandingConfiguration
import com.cooper.homelab.ephyra.server.countries
import com.cooper.homelab.ephyra.server.cultures
import com.cooper.homelab.ephyra.server.saveBranding
import com.cooper.homelab.ephyra.server.saveServerConfiguration
import com.cooper.homelab.ephyra.server.serverConfiguration
import com.cooper.homelab.ephyra.server.serverSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

/** A text setting edited in a dialog, rather than saved a keystroke at a time. */
enum class ServerText { Name, LoginDisclaimer, CustomCss }

/** The picker open over the page. */
enum class ServerPicker { MetadataLanguage, MetadataCountry }

sealed interface ServerSettingsUiState {
    data object Loading : ServerSettingsUiState

    data class Failed(
        val failure: ConsoleFailure,
    ) : ServerSettingsUiState

    /**
     * The settings as shown, what the pickers offer, and the last save that failed ([saveFailed]). [refused] keeps a page
     * refusal until the next edit, even if the other writer cannot reach the server.
     */
    data class Ready(
        val settings: ServerSettings,
        val branding: Branding,
        val cultures: List<Culture> = emptyList(),
        val countries: List<Country> = emptyList(),
        val editing: ServerText? = null,
        val saveFailed: ConsoleFailure? = null,
        val refused: ConsoleFailure? = null,
        val serverSave: SaveProgress = SaveProgress(),
        val brandSave: SaveProgress = SaveProgress(),
    ) : ServerSettingsUiState {
        /** Where the page's saves stand, the two writers' together: failed if either is, busy if either is. */
        val save: SaveProgress get() = combined(serverSave, brandSave)
    }
}

/**
 * The server's general settings and its login branding, each saved as it's made (see
 * [ConfigurationSaves]); a text setting saves when its dialog does. The language and country pickers
 * are niceties: if their lists can't be read, the rest of the page still works.
 */
class ServerSettingsViewModel(
    private val connection: Connection,
    private val work: CoroutineDispatcher,
    private val io: CoroutineDispatcher,
) : ViewModel() {
    private val _state = MutableStateFlow<ServerSettingsUiState>(ServerSettingsUiState.Loading)
    val state: StateFlow<ServerSettingsUiState> = _state.asStateFlow()

    private val server =
        ConfigurationSaves(
            connection,
            viewModelScope,
            TAG,
            { _, client, json -> client.saveServerConfiguration(json) },
            onProgress = { progress -> update { copy(serverSave = progress) } },
        ) { shown, failure ->
            update { copy(settings = shown.serverSettings(), saveFailed = failure, refused = refusedAfter(refused, failure)) }
        }

    private val brand =
        ConfigurationSaves(
            connection,
            viewModelScope,
            TAG,
            { _, client, json -> client.saveBranding(json) },
            onProgress = { progress -> update { copy(brandSave = progress) } },
        ) { shown, failure ->
            update { copy(branding = shown.branding(), saveFailed = failure, refused = refusedAfter(refused, failure)) }
        }

    /** The login splash image's sheet, which reads the branding configuration again after it changes the image. */
    internal val splashImage =
        SplashImageController(
            connection,
            viewModelScope,
            adoptBranding = { json ->
                // Only the image's location: the rest of the page's copy, and what it has waiting, stays.
                brand.adoptKey(SPLASHSCREEN_LOCATION, json[SPLASHSCREEN_LOCATION])
                update { copy(branding = brand.shown.branding()) }
            },
            work = work,
            io = io,
        )

    init {
        load()
    }

    fun load() {
        _state.value = ServerSettingsUiState.Loading
        viewModelScope.launch {
            connection.awaitSaves()
            _state.value =
                connection
                    .consoleCall(TAG) { _, client ->
                        coroutineScope {
                            val cultures = async { attempt { client.cultures() }.getOrDefault(emptyList()) }
                            val countries = async { attempt { client.countries() }.getOrDefault(emptyList()) }
                            val branding = async { client.brandingConfiguration() }
                            val configuration = client.serverConfiguration()
                            server.loaded(configuration)
                            brand.loaded(branding.await())
                            ServerSettingsUiState.Ready(
                                configuration.serverSettings(),
                                branding.await().branding(),
                                cultures.await().sortedBy { it.displayName },
                                countries.await().sortedBy { it.displayName },
                            )
                        }
                    }.getOrElse { ServerSettingsUiState.Failed(it.consoleFailure()) }
        }
    }

    /** Opens the sheet for [text], or closes it for null. */
    fun edit(text: ServerText?) = update { copy(editing = text) }

    /**
     * Saves the text the sheet for [text] holds, as made: the sheet closes at once and the page shows the change. A
     * refusal puts it back and a server that can't be reached keeps it unsent, each reported on the page. A text that is
     * unchanged, or a blank server name, only closes the sheet.
     */
    fun saveText(
        text: ServerText,
        value: String,
    ) {
        val (saves, key, json) =
            when (text) {
                ServerText.Name -> Triple(server, SERVER_NAME, JsonPrimitive(value.trim()))
                ServerText.LoginDisclaimer -> Triple(brand, LOGIN_DISCLAIMER, JsonPrimitive(value.trim()))
                ServerText.CustomCss -> Triple(brand, CUSTOM_CSS, JsonPrimitive(value))
            }
        val unsendable = text == ServerText.Name && json.content.isEmpty()
        if (unsendable || saves.shown[key] == json) {
            update { copy(editing = null) }
            return
        }
        update { copy(editing = null, saveFailed = null, refused = null) }
        saves.set(key, json)
    }

    fun setMetadataLanguage(code: String) = server.set(METADATA_LANGUAGE, JsonPrimitive(code))

    fun setMetadataCountry(code: String) = server.set(METADATA_COUNTRY, JsonPrimitive(code))

    fun setQuickConnect(on: Boolean) = server.set(QUICK_CONNECT_AVAILABLE, JsonPrimitive(on))

    fun setSplashscreen(on: Boolean) = brand.set(SPLASHSCREEN_ENABLED, JsonPrimitive(on))

    /** Sends the changes a failed write left unsent. */
    fun retrySave() {
        server.retry()
        brand.retry()
    }

    /** A change still waiting out its delay is sent now, where this page's scope can no longer cancel it. */
    override fun onCleared() {
        server.cleared()
        brand.cleared()
    }

    private fun update(change: ServerSettingsUiState.Ready.() -> ServerSettingsUiState.Ready) =
        _state.update { (it as? ServerSettingsUiState.Ready)?.change() ?: it }

    private companion object {
        const val TAG = "JellyfinServerSettings"
    }
}
