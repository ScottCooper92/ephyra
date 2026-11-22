package com.cooper.homelab.ephyra.ui.search

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.ui.graphics.vector.ImageVector
import com.cooper.homelab.core.presentation.search.SettingEntry
import com.cooper.homelab.ephyra.R
import com.cooper.homelab.ephyra.server.ENCODING_AV1
import com.cooper.homelab.ephyra.server.ENCODING_HEVC
import com.cooper.homelab.ephyra.server.JellyfinAccount
import com.cooper.homelab.ephyra.server.JellyfinServerProfile
import com.cooper.homelab.ephyra.server.NETWORK_BASE_URL
import com.cooper.homelab.ephyra.server.NETWORK_ENABLE_HTTPS
import com.cooper.homelab.ephyra.server.NETWORK_REMOTE_ACCESS
import com.cooper.homelab.ephyra.server.NFO_EXTRA_THUMBS
import com.cooper.homelab.ephyra.server.NFO_PATH_SUBSTITUTION
import com.cooper.homelab.ephyra.server.NFO_SAVE_IMAGE_PATHS
import com.cooper.homelab.ephyra.ui.console.ConsolePage
import com.cooper.homelab.core.uiandroid.R as CoreR

/**
 * An entry's stable name, to remember it by: its page and its label's resource name. A resource's number changes from build to build, its
 * name does not. These are the ids the recents have always held, so nobody loses theirs.
 */
private fun Resources.entryId(
    page: ConsolePage,
    @StringRes label: Int,
) = "${page.name}:${getResourceEntryName(label)}"

private fun Resources.page(
    page: ConsolePage,
    @StringRes label: Int,
    icon: ImageVector,
    @StringRes section: Int,
    @StringRes keywords: Int? = null,
) = SettingEntry(
    id = entryId(page, label),
    label = text(label),
    section = text(section),
    target = SearchTarget(page, icon),
    keywords = keywords?.let(::text),
)

internal fun Resources.setting(
    page: ConsolePage,
    @StringRes label: Int,
    icon: ImageVector,
    @StringRes section: Int,
    @StringRes pageLabel: Int,
    @StringRes keywords: Int? = null,
    shown: (JellyfinServerProfile, JellyfinAccount) -> Boolean = { _, _ -> true },
    row: String? = null,
) = SettingEntry(
    id = entryId(page, label),
    label = text(label),
    section = text(section),
    target = SearchTarget(page, icon, shown),
    pageLabel = text(pageLabel),
    keywords = keywords?.let(::text),
    row = row,
)

/**
 * Everything the search knows. A page of the console, and each setting worth finding by name, is listed here: a new setting adds its
 * line, and a test holds that every page the home lists has one. An entry shows only where [ConsolePage.available] says the page does.
 * The list is in order and in parts, only so that no one function is longer than detekt allows.
 */
internal fun settingsIndex(resources: Resources): List<SettingEntry<SearchTarget>> =
    with(resources) {
        indexPart1() +
            indexPart2() +
            indexPart3() +
            indexPart4() +
            indexPart5() +
            indexSwitches() +
            indexServerSwitches() +
            indexEncodingSwitches() +
            indexNetworkSwitches() +
            indexLiveTvSwitches() +
            indexBrandingSwitches()
    }

private fun Resources.indexPart1(): List<SettingEntry<SearchTarget>> =
    listOf(
        // The user's own.
        page(
            ConsolePage.ApproveCode,
            R.string.approve_title,
            Icons.Filled.QrCode2,
            R.string.console_account_title,
            R.string.kw_quick_connect,
        ),
        page(
            ConsolePage.Playback,
            R.string.playback_title,
            Icons.Filled.Translate,
            R.string.console_account_title,
            R.string.kw_playback,
        ),
        page(ConsolePage.HomeLayout, R.string.home_layout_title, Icons.Filled.Dashboard, R.string.console_account_title),
        page(
            ConsolePage.ProfilePicture,
            R.string.profile_picture_title,
            Icons.Filled.Image,
            R.string.console_account_title,
            R.string.kw_picture,
        ),
        page(
            ConsolePage.Password,
            R.string.password_title,
            Icons.Filled.Password,
            R.string.console_account_title,
            R.string.kw_password,
        ),
        setting(
            ConsolePage.Playback,
            R.string.playback_subtitles,
            Icons.Filled.Translate,
            R.string.console_account_title,
            R.string.playback_title,
        ),
        // The server's pages, in the sections the home has.
        page(
            ConsolePage.NowPlaying,
            R.string.now_playing_title,
            Icons.Filled.PlayCircle,
            R.string.settings_server,
            R.string.kw_now_playing,
        ),
        page(ConsolePage.LiveTv, R.string.live_tv_title, Icons.Filled.LiveTv, R.string.settings_server, R.string.kw_live_tv),
        page(ConsolePage.Users, R.string.users_title, Icons.Filled.Group, R.string.settings_server, R.string.kw_users),
        page(ConsolePage.Libraries, R.string.libraries_title, Icons.Filled.Folder, R.string.settings_server, R.string.kw_libraries),
        page(
            ConsolePage.Transcoding,
            R.string.transcoding_title,
            Icons.Filled.Memory,
            R.string.settings_server,
            R.string.kw_transcoding,
        ),
        page(ConsolePage.ServerSettings, R.string.server_settings_title, Icons.Filled.Tune, R.string.settings_server),
        page(
            ConsolePage.LibraryDisplay,
            R.string.display_title,
            Icons.Filled.ViewModule,
            R.string.settings_server,
            R.string.kw_metadata,
        ),
        page(
            ConsolePage.MetadataDefaults,
            R.string.metadata_title,
            Icons.Filled.Category,
            R.string.settings_server,
            R.string.kw_metadata,
        ),
    )

private fun Resources.indexPart2(): List<SettingEntry<SearchTarget>> =
    listOf(
        page(ConsolePage.Nfo, R.string.nfo_title, Icons.Filled.Description, R.string.settings_server, R.string.kw_nfo),
        setting(
            ConsolePage.Nfo,
            R.string.nfo_user,
            Icons.Filled.Person,
            R.string.settings_server,
            R.string.nfo_title,
            R.string.kw_nfo_user,
        ),
        setting(
            ConsolePage.Nfo,
            R.string.nfo_date_format,
            Icons.Filled.CalendarMonth,
            R.string.settings_server,
            R.string.nfo_title,
            R.string.kw_nfo_date,
        ),
        setting(
            ConsolePage.Nfo,
            R.string.nfo_image_paths,
            Icons.Filled.Image,
            R.string.settings_server,
            R.string.nfo_title,
            R.string.kw_nfo_images,
            row = SettingRows.nfo(NFO_SAVE_IMAGE_PATHS),
        ),
        setting(
            ConsolePage.Nfo,
            R.string.nfo_path_substitution,
            Icons.Filled.SwapHoriz,
            R.string.settings_server,
            R.string.nfo_title,
            R.string.kw_nfo_paths,
            row = SettingRows.nfo(NFO_PATH_SUBSTITUTION),
        ),
        setting(
            ConsolePage.Nfo,
            R.string.nfo_extra_thumbs,
            Icons.Filled.ContentCopy,
            R.string.settings_server,
            R.string.nfo_title,
            R.string.kw_nfo_thumbs,
            row = SettingRows.nfo(NFO_EXTRA_THUMBS),
        ),
        page(ConsolePage.Trickplay, R.string.trickplay_title, Icons.Filled.Image, R.string.settings_server, R.string.kw_trickplay),
        page(ConsolePage.Plugins, R.string.plugins_title, Icons.Filled.Extension, R.string.settings_server, R.string.kw_plugins),
        page(ConsolePage.Devices, R.string.devices_title, Icons.Filled.Devices, R.string.devices_title),
        page(ConsolePage.Activity, R.string.activity_title, Icons.Filled.History, R.string.devices_title, R.string.kw_activity),
        page(ConsolePage.Tasks, R.string.tasks_title, Icons.Filled.Schedule, R.string.advanced_title, R.string.kw_tasks),
        page(ConsolePage.Logs, R.string.logs_title, Icons.Filled.Description, R.string.advanced_title),
        page(ConsolePage.Network, R.string.network_title, Icons.Filled.Router, R.string.advanced_title, R.string.kw_network),
        page(ConsolePage.ApiKeys, R.string.api_keys_title, Icons.Filled.Key, R.string.advanced_title),
        page(ConsolePage.Backups, R.string.backups_title, Icons.Filled.Backup, R.string.advanced_title),
        page(ConsolePage.Advanced, R.string.advanced_title, Icons.Filled.Build, R.string.advanced_title),
        page(
            ConsolePage.Server,
            R.string.server_title,
            Icons.Filled.SettingsApplications,
            R.string.advanced_title,
            R.string.kw_restart,
        ),
        setting(
            ConsolePage.Server,
            R.string.server_alerts,
            Icons.Filled.WarningAmber,
            R.string.advanced_title,
            R.string.server_title,
            R.string.kw_alerts,
        ),
    )

private fun Resources.indexPart3(): List<SettingEntry<SearchTarget>> =
    listOf(
        setting(
            ConsolePage.Server,
            R.string.server_storage,
            Icons.Filled.Storage,
            R.string.advanced_title,
            R.string.server_title,
            R.string.kw_storage,
            shown = { profile, _ -> profile.hasStorageInfo },
        ),
        setting(
            ConsolePage.Server,
            R.string.server_paths,
            Icons.Filled.Folder,
            R.string.advanced_title,
            R.string.server_title,
            R.string.kw_paths,
        ),
        // Settings on them.
        setting(
            ConsolePage.Plugins,
            R.string.catalog_versions_search,
            Icons.Filled.History,
            R.string.settings_server,
            R.string.plugins_title,
            R.string.kw_plugin_versions,
        ),
        setting(
            ConsolePage.Libraries,
            R.string.library_image_limits,
            Icons.Filled.Image,
            R.string.settings_server,
            R.string.libraries_title,
            R.string.kw_image_limits,
        ),
        setting(
            ConsolePage.ServerSettings,
            R.string.server_settings_name,
            Icons.Filled.Tune,
            R.string.settings_server,
            R.string.server_settings_title,
            row = SettingRows.SERVER_NAME,
        ),
        setting(
            ConsolePage.ServerSettings,
            R.string.server_settings_quick_connect,
            Icons.Filled.QrCode2,
            R.string.settings_server,
            R.string.server_settings_title,
            R.string.kw_quick_connect,
            row = SettingRows.SERVER_QUICK_CONNECT,
        ),
        setting(
            ConsolePage.Transcoding,
            R.string.transcoding_hardware,
            Icons.Filled.Memory,
            R.string.settings_server,
            R.string.transcoding_title,
            R.string.kw_hardware,
            row = SettingRows.TRANSCODING_ACCELERATION,
        ),
        setting(
            ConsolePage.Transcoding,
            R.string.transcoding_hevc,
            Icons.Filled.Memory,
            R.string.settings_server,
            R.string.transcoding_title,
            R.string.kw_codecs,
            row = SettingRows.transcoding(ENCODING_HEVC),
        ),
    )

private fun Resources.indexPart4(): List<SettingEntry<SearchTarget>> =
    listOf(
        setting(
            ConsolePage.Transcoding,
            R.string.transcoding_av1,
            Icons.Filled.Memory,
            R.string.settings_server,
            R.string.transcoding_title,
            R.string.kw_codecs,
            shown = { profile, _ -> profile.hasAdvancedEncoding },
            row = SettingRows.transcoding(ENCODING_AV1),
        ),
        setting(
            ConsolePage.Network,
            R.string.network_http_port,
            Icons.Filled.Router,
            R.string.advanced_title,
            R.string.network_title,
            R.string.kw_port,
            row = SettingRows.NETWORK_HTTP_PORT,
        ),
        setting(
            ConsolePage.Network,
            R.string.network_https_port,
            Icons.Filled.Router,
            R.string.advanced_title,
            R.string.network_title,
            R.string.kw_port,
            row = SettingRows.NETWORK_HTTPS_PORT,
        ),
        setting(
            ConsolePage.Network,
            R.string.network_enable_https,
            Icons.Filled.Router,
            R.string.advanced_title,
            R.string.network_title,
            R.string.kw_https,
            row = SettingRows.network(NETWORK_ENABLE_HTTPS),
        ),
        setting(
            ConsolePage.Network,
            R.string.network_remote_access,
            Icons.Filled.Router,
            R.string.advanced_title,
            R.string.network_title,
            R.string.kw_remote,
            row = SettingRows.network(NETWORK_REMOTE_ACCESS),
        ),
        setting(
            ConsolePage.Network,
            R.string.network_base_url,
            Icons.Filled.Router,
            R.string.advanced_title,
            R.string.network_title,
            R.string.kw_proxy,
            row = SettingRows.network(NETWORK_BASE_URL),
        ),
        setting(
            ConsolePage.Advanced,
            R.string.advanced_cache,
            Icons.Filled.Build,
            R.string.advanced_title,
            R.string.advanced_title,
            shown = { profile, _ -> profile.hasCacheAndLegacyAuth },
            row = SettingRows.ADVANCED_CACHE,
        ),
    )

private fun Resources.indexPart5(): List<SettingEntry<SearchTarget>> =
    listOf(
        setting(
            ConsolePage.Logs,
            R.string.logs_retention_title,
            Icons.Filled.Description,
            R.string.advanced_title,
            R.string.logs_title,
            row = SettingRows.LOGS_RETENTION,
        ),
        setting(ConsolePage.Users, R.string.users_remote_access, Icons.Filled.Group, R.string.settings_server, R.string.users_title),
        setting(ConsolePage.Users, R.string.users_downloading, Icons.Filled.Group, R.string.settings_server, R.string.users_title),
        // This app.
        page(ConsolePage.ThisApp, R.string.settings_group_app, Icons.Filled.Tune, R.string.settings_group_app, R.string.kw_this_app),
        setting(
            ConsolePage.ThisApp,
            R.string.settings_share_usage_data,
            Icons.Filled.BarChart,
            R.string.settings_group_app,
            R.string.settings_group_app,
            R.string.kw_privacy,
            row = SettingRows.THIS_APP_USAGE_DATA,
        ),
        setting(
            ConsolePage.ThisApp,
            R.string.settings_send_crash_reports,
            Icons.Filled.Healing,
            R.string.settings_group_app,
            R.string.settings_group_app,
            R.string.kw_privacy,
            row = SettingRows.THIS_APP_CRASH_REPORTS,
        ),
        setting(
            ConsolePage.ThisApp,
            R.string.settings_report_bug,
            Icons.Filled.Healing,
            R.string.settings_group_app,
            R.string.settings_group_app,
            R.string.kw_feedback,
            row = SettingRows.THIS_APP_REPORT_BUG,
        ),
        setting(
            ConsolePage.ThisApp,
            R.string.notifications_switch,
            Icons.Filled.Notifications,
            R.string.settings_group_app,
            R.string.settings_group_app,
            R.string.kw_notifications,
            shown = { _, account -> account.isAdministrator },
            row = SettingRows.THIS_APP_NOTIFICATIONS,
        ),
        page(ConsolePage.Licences, R.string.licences_title, Icons.Filled.Description, R.string.settings_group_app),
        page(
            ConsolePage.Diagnostics,
            CoreR.string.diagnostics_title,
            Icons.Filled.BugReport,
            R.string.settings_group_app,
            R.string.kw_diagnostics,
        ),
    )

/** The user's own on/off settings: a page's switch is found, and shown in the results, by its own name. */
private fun Resources.indexSwitches(): List<SettingEntry<SearchTarget>> =
    listOf(
        setting(
            ConsolePage.Playback,
            R.string.playback_default_audio,
            Icons.Filled.GraphicEq,
            R.string.console_account_title,
            R.string.playback_title,
            row = SettingRows.PLAYBACK_DEFAULT_AUDIO,
        ),
        setting(
            ConsolePage.Playback,
            R.string.playback_remember_audio,
            Icons.Filled.History,
            R.string.console_account_title,
            R.string.playback_title,
            row = SettingRows.PLAYBACK_REMEMBER_AUDIO,
        ),
        setting(
            ConsolePage.Playback,
            R.string.playback_remember_subtitles,
            Icons.Filled.Restore,
            R.string.console_account_title,
            R.string.playback_title,
            row = SettingRows.PLAYBACK_REMEMBER_SUBTITLES,
        ),
        setting(
            ConsolePage.Playback,
            R.string.playback_autoplay,
            Icons.Filled.SkipNext,
            R.string.console_account_title,
            R.string.playback_title,
            row = SettingRows.PLAYBACK_AUTOPLAY,
        ),
        setting(
            ConsolePage.HomeLayout,
            R.string.home_layout_hide_played,
            Icons.Filled.NewReleases,
            R.string.console_account_title,
            R.string.home_layout_title,
            row = SettingRows.HOME_HIDE_PLAYED,
        ),
        setting(
            ConsolePage.HomeLayout,
            R.string.home_layout_missing,
            Icons.Filled.HideSource,
            R.string.console_account_title,
            R.string.home_layout_title,
            row = SettingRows.HOME_MISSING_EPISODES,
        ),
        setting(
            ConsolePage.HomeLayout,
            R.string.home_layout_collections,
            Icons.Filled.CollectionsBookmark,
            R.string.console_account_title,
            R.string.home_layout_title,
            row = SettingRows.HOME_COLLECTIONS,
        ),
    )

/** The server's own on/off settings, for an administrator: Trickplay and Advanced (Quick Connect has its entry above). */
private fun Resources.indexServerSwitches(): List<SettingEntry<SearchTarget>> =
    listOf(
        setting(
            ConsolePage.Trickplay,
            R.string.trickplay_hw_acceleration,
            Icons.Filled.Memory,
            R.string.settings_server,
            R.string.trickplay_title,
            R.string.kw_trickplay,
            row = SettingRows.TRICKPLAY_HW_ACCELERATION,
        ),
        setting(
            ConsolePage.Trickplay,
            R.string.trickplay_hw_encoding,
            Icons.Filled.Videocam,
            R.string.settings_server,
            R.string.trickplay_title,
            R.string.kw_trickplay,
            row = SettingRows.TRICKPLAY_HW_ENCODING,
        ),
        setting(
            ConsolePage.Trickplay,
            R.string.trickplay_key_frames,
            Icons.Filled.Bolt,
            R.string.settings_server,
            R.string.trickplay_title,
            R.string.kw_trickplay,
            shown = { profile, _ -> profile.hasKeyFrameOnlyTrickplay },
            row = SettingRows.TRICKPLAY_KEY_FRAMES,
        ),
        setting(
            ConsolePage.Advanced,
            R.string.advanced_slow_warning,
            Icons.Filled.Warning,
            R.string.advanced_title,
            R.string.advanced_title,
            row = SettingRows.ADVANCED_SLOW_WARNING,
        ),
        setting(
            ConsolePage.Advanced,
            R.string.advanced_client_logs,
            Icons.Filled.BugReport,
            R.string.advanced_title,
            R.string.advanced_title,
            row = SettingRows.ADVANCED_CLIENT_LOGS,
        ),
        setting(
            ConsolePage.Advanced,
            R.string.advanced_metrics,
            Icons.Filled.Analytics,
            R.string.advanced_title,
            R.string.advanced_title,
            row = SettingRows.ADVANCED_METRICS,
        ),
        setting(
            ConsolePage.Advanced,
            R.string.advanced_legacy_auth,
            Icons.Filled.Lock,
            R.string.advanced_title,
            R.string.advanced_title,
            shown = { profile, _ -> profile.hasCacheAndLegacyAuth },
            row = SettingRows.ADVANCED_LEGACY_AUTH,
        ),
    )
