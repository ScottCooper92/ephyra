package com.cooper.homelab.ephyra.ui.console

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.SaveStatus
import com.cooper.homelab.ephyra.preview.EphyraFontScalePreviews
import com.cooper.homelab.ephyra.preview.EphyraScreenStatePreview
import com.cooper.homelab.ephyra.preview.EphyraSpanishPreviews
import com.cooper.homelab.ephyra.preview.EphyraWidePreview
import com.cooper.homelab.ephyra.preview.EphyraWideSpanishPreview
import com.cooper.homelab.ephyra.server.HomeLayout
import com.cooper.homelab.ephyra.server.UserView

private val previewViews =
    listOf(
        UserView("movies", "Movies", "movies"),
        UserView("shows", "Shows", "tvshows"),
        UserView("music", "Music", "music"),
        UserView("home", "Home videos and photos from the family archive"),
    )

private val previewHome =
    HomeLayoutUiState.Ready(
        previewViews,
        HomeLayout(order = listOf("shows"), myMediaExcludes = setOf("home"), latestExcludes = setOf("music", "home")),
        groupable = listOf(previewViews.last()),
    )

/** The home screen page: the libraries and switches across the matrix, a library's page, then each other state. */
class HomeLayoutScreenshotTest {
    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun libraries() = HomeLayoutScreen(previewHome, HomeLayoutActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @EphyraSpanishPreviews
    @Composable
    fun library() = HomeLayoutLibraryScreen(previewHome, "home", HomeLayoutActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @Composable
    fun middleLibrary() = HomeLayoutLibraryScreen(previewHome, "movies", HomeLayoutActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraFontScalePreviews
    @Composable
    fun onlyLibrary() = HomeLayoutLibraryScreen(HomeLayoutUiState.Ready(previewViews.take(1), HomeLayout()), "movies", HomeLayoutActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun firstLibrary() = HomeLayoutLibraryScreen(previewHome, "shows", HomeLayoutActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun noLibraries() = HomeLayoutScreen(HomeLayoutUiState.Ready(emptyList(), HomeLayout()), HomeLayoutActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @EphyraSpanishPreviews
    @Composable
    fun saving() = HomeLayoutScreen(previewHome.copy(save = SaveProgress(SaveStatus.Saving)), HomeLayoutActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun saved() = HomeLayoutScreen(previewHome.copy(save = SaveProgress(SaveStatus.Saved)), HomeLayoutActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun loading() = HomeLayoutScreen(HomeLayoutUiState.Loading, HomeLayoutActions())

    @PreviewTest
    @EphyraScreenStatePreview
    @Composable
    fun failed() = HomeLayoutScreen(HomeLayoutUiState.Failed(ConsoleFailure.Unreachable), HomeLayoutActions())
}

/** The home screen page on a landscape tablet, where a library opens in a third pane: the invitation to pick one while none is. */
class HomeLayoutPaneScreenshotTest {
    @PreviewTest
    @EphyraWidePreview
    @EphyraWideSpanishPreview
    @Composable
    fun placeholder() = Surface { HomeLayoutPlaceholder() }
}
