package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.core.foundation.ServerVersion
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStateLightPreview
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.BackupCreation
import com.cooper.homelab.ephyra.server.BackupOptions
import com.cooper.homelab.ephyra.server.JellyfinHttpException
import com.cooper.homelab.ephyra.server.ServerBackup
import java.io.IOException

private const val NOW = 1_793_534_400_000L // 31 Oct 2026, 12:00 UTC
private const val DAY = 24 * 60 * 60 * 1000L

private val running = ServerVersion.of(10, 11, 6)

private val previewBackups =
    listOf(
        ServerBackup("jellyfin-backup-20261030090000.zip", NOW - DAY, "10.11.6.0", BackupOptions()),
        ServerBackup(
            "jellyfin-backup-20261020090000.zip",
            NOW - 11 * DAY,
            "10.11.5.0",
            BackupOptions(database = true, metadata = true, subtitles = true, trickplay = true),
        ),
    )

private val previewReady = BackupsUiState.Ready(previewBackups)

/**
 * Backups: the list across the matrix, then each thing the page can be doing, its sheets, the restore confirm and the message dialogs
 * (a backup not made, a restore the server did not come back from), and the read states.
 */
class BackupsScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun backups() = BackupsScreen(previewReady, BackupsActions(), running, NOW)

    /** A pull in progress: the page stays, with the pull spinner over it. */
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun refreshing() = BackupsScreen(previewReady.copy(refreshing = true), BackupsActions(), running, NOW)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun creating() = BackupsScreen(previewReady.copy(creation = BackupCreation.Creating), BackupsActions(), running, NOW)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun created() =
        BackupsScreen(
            previewReady.copy(creation = BackupCreation.Created(previewBackups.first())),
            BackupsActions(),
            running,
            NOW,
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun notMade() = Surface { BackupNotMadeDialog(BackupCreation.Failed(IOException("down")), BackupsActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun notMadeRefused() =
        Surface {
            BackupNotMadeDialog(
                BackupCreation.Failed(JellyfinHttpException(500, "Backup/Create")),
                BackupsActions(),
                inWindow = false,
            )
        }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun notMadeWithReason() =
        Surface {
            BackupNotMadeDialog(
                BackupCreation.Failed(
                    JellyfinHttpException(500, "Backup/Create", "The backup directory does not have at least 5GiB free space"),
                ),
                BackupsActions(),
                inWindow = false,
            )
        }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun restoreNotBack() = Surface { RestoreNotBackDialog(BackupsActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun restoring() = BackupsScreen(previewReady.copy(restore = Restore.Restoring), BackupsActions(), running, NOW)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun restored() = BackupsScreen(previewReady.copy(restore = Restore.Restored), BackupsActions(), running, NOW)

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraScreenStateLightPreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun newBackup() = Surface { CreateBackupSheet(BackupOptions(database = true, metadata = true), BackupsActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraScreenStateLightPreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun oneBackup() = Surface { BackupSheet(previewBackups.last(), running, BackupsActions(), NOW, inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun oneBackupOtherRelease() =
        Surface {
            BackupSheet(previewBackups.last(), ServerVersion.of(10, 11, 7), BackupsActions(), NOW, inWindow = false)
        }

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @EphyraFontScalePreviews
    @Composable
    fun restoreConfirm() = Surface { RestoreDialog(previewBackups.first(), running, BackupsActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun restoreConfirmOtherRelease() = Surface { RestoreDialog(previewBackups.last(), running, BackupsActions(), inWindow = false) }

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun empty() = BackupsScreen(BackupsUiState.Ready(emptyList()), BackupsActions(), running, NOW)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = BackupsScreen(BackupsUiState.Loading, BackupsActions(), running, NOW)

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = BackupsScreen(BackupsUiState.Failed(ConsoleFailure.Refused), BackupsActions(), running, NOW)
}
