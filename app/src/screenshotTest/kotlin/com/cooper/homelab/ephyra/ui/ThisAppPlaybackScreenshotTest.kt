package com.cooper.homelab.ephyra.ui

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.server.Culture
import com.cooper.homelab.ephyra.server.PlaybackPreferences
import com.cooper.homelab.ephyra.server.SubtitleMode
import com.cooper.homelab.ephyra.ui.console.PlaybackUiState

private val previewCultures =
    listOf(
        Culture("English", "en", listOf("eng")),
        Culture("French", "fr", listOf("fre", "fra")),
        Culture("Japanese", "ja", listOf("jpn")),
    )

/** The This app page with the client on: the Playback group holds the server's own language preferences, as the playback page shows them. */
class ThisAppPlaybackScreenshotTest {
    private val clientOn = Actions(appSettings = AppSettings(client = true))

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun chosen() =
        ThisAppScreen(
            clientOn,
            onBack = {},
            playback =
                PlaybackUiState.Ready(
                    PlaybackPreferences(audio = "jpn", subtitles = "eng", subtitleMode = SubtitleMode.Smart),
                    previewCultures,
                ),
        )

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun anyLanguage() =
        ThisAppScreen(
            clientOn,
            onBack = {},
            playback = PlaybackUiState.Ready(PlaybackPreferences("", "", SubtitleMode.Default), previewCultures),
        )
}
