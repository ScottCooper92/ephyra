package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.preview.EphyraWidePreview
import com.cooper.homelab.ephyra.preview.EphyraWideSpanishPreview
import com.cooper.homelab.ephyra.server.LogEntry
import com.cooper.homelab.ephyra.server.LogFile
import com.cooper.homelab.ephyra.server.LogLevel
import com.cooper.homelab.ephyra.server.parseLogEntries
import com.cooper.homelab.ephyra.server.redactLog
import java.time.ZoneOffset

private const val LONG_NAME = "FFmpeg.Transcode-2026-10-31_09-15-02_3f2a9c.log"
private const val DAY = 24 * 60 * 60_000L
private const val NOW = 1_793_534_400_000L // 31 Oct 2026, 12:00 UTC

private val previewFiles =
    listOf(
        LogFile("log_20261031.log", 412_000, NOW - 60_000),
        LogFile("log_20261030.log", 2_300_000, NOW - DAY),
        LogFile("log_20261029.log", 1_100_000, NOW - 2 * DAY),
    )

private val previewText =
    "[2026-10-31 11:58:02.114 +00:00] [INF] [1] Main: Jellyfin version: 10.11.2\n" +
        "[2026-10-31 11:58:04.552 +00:00] [DBG] [12] Emby.Server.Library.LibraryManager: Validating media library\n" +
        "[2026-10-31 11:59:10.003 +00:00] [WRN] [18] Jellyfin.Api.Auth: Slow query took 4.2 s\n" +
        "[2026-10-31 11:59:41.871 +00:00] [ERR] [21] Jellyfin.Server: Request /Videos/1/stream?api_key=0123456789abcdef failed\n" +
        "System.Net.Http.HttpRequestException: The connection was reset\n" +
        "   at Jellyfin.Server.Stream.Run()\n" +
        "[2026-10-31 11:59:58.020 +00:00] [INF] [4] Jellyfin.Api.SessionController: Session ended\n"

/** Times without millis, so a row prints its raw time and the frame doesn't change as "Yesterday" becomes a date. */
private fun fixedTimes(entries: List<LogEntry>) =
    entries.map { it.copy(timestampMillis = null, timestampRaw = it.timestampRaw?.substring(TIME_AT, TIME_END)) }

private const val TIME_AT = 11
private const val TIME_END = 19

private val previewEntries = fixedTimes(parseLogEntries(redactLog(previewText)))

private fun open(
    query: String = "",
    level: LogLevel = LogLevel.Debug,
    truncated: Boolean = false,
    entries: List<LogEntry> = previewEntries,
    name: String = "log_20261031.log",
    content: LogContent = LogContent.Ready(entries, truncated),
) = LogsUiState.Ready(
    previewFiles,
    OpenLog(name, content, query, level),
)

/** Logs: the file list across the matrix, then a log open, cut short, searched with no match, and the read states. */
class LogsScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun fullName() = Surface { LogNameDialog(LONG_NAME, {}, inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun list() = LogsScreen(LogsUiState.Ready(previewFiles), LogsActions(), zone = ZoneOffset.UTC)

    /** A pull in progress: the page stays, with the pull spinner over it. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun refreshing() = LogsScreen(LogsUiState.Ready(previewFiles, refreshing = true), LogsActions(), zone = ZoneOffset.UTC)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun openLog() = LogScreen(open(), "log_20261031.log", LogsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @Composable
    fun openLogLongName() = LogScreen(open(name = LONG_NAME), LONG_NAME, LogsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun openLogLoading() = LogScreen(open(content = LogContent.Loading), "log_20261031.log", LogsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun openLogFailed() = LogScreen(open(content = LogContent.Failed(ConsoleFailure.Unreachable)), "log_20261031.log", LogsActions())

    /** A log open after a process restore, while the files load again under its screen. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun logFilesLoading() = LogScreen(LogsUiState.Loading, "log_20261031.log", LogsActions())

    /** The same reload, failed: the error screen with Retry under the log's own bar. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun logFilesFailed() = LogScreen(LogsUiState.Failed(ConsoleFailure.Unreachable), "log_20261031.log", LogsActions())

    /** The files are back but the view model holds no log, or another one, so this one is read again: the skeleton. */
    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun logNotOpenYet() = LogScreen(LogsUiState.Ready(previewFiles), "log_20261031.log", LogsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun truncated() = LogScreen(open(truncated = true), "log_20261031.log", LogsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun warningsUp() = LogScreen(open(level = LogLevel.Warn), "log_20261031.log", LogsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun unlabelledEntry() =
        LogScreen(
            open(entries = fixedTimes(parseLogEntries(redactLog("   at the tail of a cut trace\n" + previewText.lines().first())))),
            "log_20261031.log",
            LogsActions(),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun noMatch() = LogScreen(open(query = "nothing like this"), "log_20261031.log", LogsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun noFiles() = LogsScreen(LogsUiState.Ready(emptyList()), LogsActions(), zone = ZoneOffset.UTC)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun retention() = LogsScreen(LogsUiState.Ready(previewFiles, retention = 3), LogsActions(), zone = ZoneOffset.UTC)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun retentionOneDay() = LogsScreen(LogsUiState.Ready(previewFiles, retention = 1), LogsActions(), zone = ZoneOffset.UTC)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun retentionUnsent() =
        LogsScreen(
            LogsUiState.Ready(previewFiles, retention = 7, save = SaveProgress(failed = true)),
            LogsActions(),
            zone = ZoneOffset.UTC,
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun retentionSheet() = Surface { RetentionForm(3, LogsActions()) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = LogsScreen(LogsUiState.Loading, LogsActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = LogsScreen(LogsUiState.Failed(ConsoleFailure.Refused), LogsActions())
}

/** The logs page on a landscape tablet, where a log opens in a third pane: the invitation to pick one while none is. */
class LogsPaneScreenshotTest {
    @PreviewTest
    @EphyraWidePreview
    @EphyraWideSpanishPreview
    @Composable
    fun placeholder() = Surface { LogPlaceholder() }
}
