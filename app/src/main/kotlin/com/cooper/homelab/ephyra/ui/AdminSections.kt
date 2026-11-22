package com.cooper.homelab.ephyra.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.ListItem
import com.cooper.homelab.ephyra.R
import com.cooper.homelab.ephyra.ui.console.ConsolePage

/** One of the administrator's pages as the home lists it: its icon, name and the line under it. */
private class AdminPage(
    val page: ConsolePage,
    val icon: ImageVector,
    @StringRes val label: Int,
    @StringRes val detail: Int,
)

/** A titled run of the administrator's pages, as the home lists them. */
private class AdminSection(
    @StringRes val title: Int,
    val pages: List<AdminPage>,
)

/**
 * The administrator's pages in the sections Jellyfin's own dashboard has (Server, Devices, Advanced), so a page is where its
 * web counterpart is. Inside a section the pages the most are opened for come first: who can sign in and what is in the
 * libraries, then how playback is set up, then the rest.
 */
private val adminSections =
    listOf(
        AdminSection(
            R.string.settings_server,
            listOf(
                AdminPage(ConsolePage.Users, Icons.Filled.Group, R.string.users_title, R.string.console_users_detail),
                AdminPage(ConsolePage.Libraries, Icons.Filled.Folder, R.string.libraries_title, R.string.console_libraries_detail),
                AdminPage(ConsolePage.Transcoding, Icons.Filled.Memory, R.string.transcoding_title, R.string.console_transcoding_detail),
                AdminPage(
                    ConsolePage.ServerSettings,
                    Icons.Filled.Tune,
                    R.string.server_settings_title,
                    R.string.console_server_settings_detail,
                ),
                AdminPage(ConsolePage.LibraryDisplay, Icons.Filled.ViewModule, R.string.display_title, R.string.console_display_detail),
                AdminPage(ConsolePage.MetadataDefaults, Icons.Filled.Category, R.string.metadata_title, R.string.console_metadata_detail),
                AdminPage(ConsolePage.Nfo, Icons.Filled.Description, R.string.nfo_title, R.string.console_nfo_detail),
                AdminPage(ConsolePage.Trickplay, Icons.Filled.Image, R.string.trickplay_title, R.string.console_trickplay_detail),
                AdminPage(ConsolePage.Plugins, Icons.Filled.Extension, R.string.plugins_title, R.string.console_plugins_detail),
            ),
        ),
        AdminSection(
            R.string.devices_title,
            listOf(
                AdminPage(ConsolePage.Devices, Icons.Filled.Devices, R.string.devices_title, R.string.console_devices_detail),
                AdminPage(ConsolePage.Activity, Icons.Filled.History, R.string.activity_title, R.string.console_activity_detail),
            ),
        ),
        AdminSection(
            R.string.advanced_title,
            listOf(
                AdminPage(ConsolePage.Tasks, Icons.Filled.Schedule, R.string.tasks_title, R.string.console_tasks_detail),
                AdminPage(ConsolePage.Logs, Icons.Filled.Description, R.string.logs_title, R.string.console_logs_detail),
                AdminPage(ConsolePage.Network, Icons.Filled.Router, R.string.network_title, R.string.console_network_detail),
                AdminPage(ConsolePage.ApiKeys, Icons.Filled.Key, R.string.api_keys_title, R.string.console_api_keys_detail),
                AdminPage(ConsolePage.Backups, Icons.Filled.Backup, R.string.backups_title, R.string.console_backups_detail),
                AdminPage(ConsolePage.Advanced, Icons.Filled.Build, R.string.advanced_title, R.string.console_advanced_detail),
                AdminPage(ConsolePage.Server, Icons.Filled.SettingsApplications, R.string.server_title, R.string.console_server_detail),
            ),
        ),
    )

/** Every page the home lists for an administrator, in the order the sections give them: for a test that none is left out or listed twice. */
internal fun adminSectionPages(): List<ConsolePage> = adminSections.flatMap { section -> section.pages.map { it.page } }

/** The console's pages for an administrator, a group to a section, shown only to one: the server would refuse anyone else. */
internal fun LazyListScope.adminSections(
    state: UiState.SignedIn,
    actions: Actions,
) {
    adminSections.forEach { section ->
        val pages = section.pages.filter { it.page.available(state.profile, state.account) }
        if (pages.isEmpty()) return@forEach
        item {
            ItemGroup(
                title = stringResource(section.title),
                rows =
                    pages.map { entry ->
                        ListItem(
                            icon = entry.icon,
                            label = stringResource(entry.label),
                            detail = stringResource(entry.detail),
                            selected = actions.isOpenBeside(entry.page),
                            onClick = { actions.onOpenPage(entry.page) },
                        )
                    },
            )
        }
    }
}
