package com.cooper.homelab.ephyra.ui.console

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.SaveStatus
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.Culture
import com.cooper.homelab.ephyra.server.PlaybackPreferences
import com.cooper.homelab.ephyra.server.SubtitleMode

private val previewCultures =
    listOf(
        Culture("English", "en", listOf("eng")),
        Culture("French", "fr", listOf("fre", "fra")),
        Culture("Japanese", "ja", listOf("jpn")),
    )

private val previewReady =
    PlaybackUiState.Ready(PlaybackPreferences(audio = "jpn", subtitles = "eng", subtitleMode = SubtitleMode.Smart), previewCultures)

/** The playback page: chosen across the matrix, then each other state on the phone cell. */
class PlaybackScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun chosen() = PlaybackScreen(previewReady, PlaybackActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun anyLanguage() =
        PlaybackScreen(PlaybackUiState.Ready(PlaybackPreferences("", "", SubtitleMode.Default), previewCultures), PlaybackActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun switchesOff() =
        PlaybackScreen(
            PlaybackUiState.Ready(
                PlaybackPreferences(
                    audio = "jpn",
                    subtitles = "eng",
                    subtitleMode = SubtitleMode.Always,
                    playDefaultAudioTrack = false,
                    rememberAudio = false,
                    rememberSubtitles = false,
                    autoplayNextEpisode = false,
                ),
                previewCultures,
            ),
            PlaybackActions(),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun saving() = PlaybackScreen(previewReady.copy(save = SaveProgress(SaveStatus.Saving)), PlaybackActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun saved() = PlaybackScreen(previewReady.copy(save = SaveProgress(SaveStatus.Saved)), PlaybackActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = PlaybackScreen(PlaybackUiState.Loading, PlaybackActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun failed() = PlaybackScreen(PlaybackUiState.Failed(ConsoleFailure.Unreachable), PlaybackActions())
}
