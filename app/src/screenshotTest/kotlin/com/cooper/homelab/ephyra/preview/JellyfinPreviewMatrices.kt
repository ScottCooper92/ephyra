package com.cooper.homelab.ephyra.preview

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.binge.designsystem.preview.FOLDABLE_PORTRAIT_SPEC
import com.binge.designsystem.preview.PHONE_LANDSCAPE_SPEC
import com.binge.designsystem.preview.PHONE_PORTRAIT_SPEC
import com.binge.designsystem.preview.SOURCE_LOCALE
import com.binge.designsystem.preview.ScreenshotThemeWrapper
import com.binge.designsystem.preview.TABLET_LANDSCAPE_SPEC
import com.binge.designsystem.theme.BingeBrand
import com.binge.designsystem.tv.preview.TV_PREVIEW_SPEC
import com.binge.designsystem.tv.preview.TvScreenshotThemeOnBlackWrapper
import com.cooper.homelab.ephyra.theme.EphyraBrand

/*
 * This app's multipreviews: the design system's cells, wearing [EphyraBrand] instead of Binge's.
 *
 * A multipreview binds its theme wrapper by class, so an app with its own brand subclasses the
 * wrapper and declares its own annotations from the design system's public specs and [SOURCE_LOCALE],
 * as binge-archive does.
 */

/** The phone harness theme, wearing [EphyraBrand]. */
class EphyraScreenshotThemeWrapper : ScreenshotThemeWrapper() {
    override val brand: BingeBrand = EphyraBrand
}

/**
 * The home's layout frame: the phone, the phone on its side and the unfolded foldable. Ephyra's own code reads no window width,
 * height or posture, so a screen's layout changes by window only through the design system's templates and the console's two panes.
 * The home shows both, so it alone has these cells. Every other screen takes [EphyraScreenStatePreview], and the wide console
 * is [EphyraWidePreview].
 */
@PreviewWrapper(EphyraScreenshotThemeWrapper::class)
@Preview(name = "phone", device = PHONE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
@Preview(name = "phone-land", device = PHONE_LANDSCAPE_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
@Preview(name = "foldable", device = FOLDABLE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
annotation class JellyfinHomeLayoutPreviews

/** A phone window at font scales 1.3 and 2.0, the two the clipping rule holds a screen to; the 1.0 cell is the screen's own frame. */
@PreviewWrapper(EphyraScreenshotThemeWrapper::class)
@Preview(name = "font13", device = PHONE_PORTRAIT_SPEC, fontScale = 1.3f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
@Preview(name = "font20", device = PHONE_PORTRAIT_SPEC, fontScale = 2f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
annotation class EphyraFontScalePreviews

/** One phone cell in Spanish, whose copy runs longer than English. */
@PreviewWrapper(EphyraScreenshotThemeWrapper::class)
@Preview(name = "es", device = PHONE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = "es")
annotation class EphyraSpanishPreviews

/** Spanish at font scales 1.3 and 2.0: its longer words at the sizes that most often clip them. */
@PreviewWrapper(EphyraScreenshotThemeWrapper::class)
@Preview(name = "es-font13", device = PHONE_PORTRAIT_SPEC, fontScale = 1.3f, uiMode = UI_MODE_NIGHT_YES, locale = "es")
@Preview(name = "es-font20", device = PHONE_PORTRAIT_SPEC, fontScale = 2f, uiMode = UI_MODE_NIGHT_YES, locale = "es")
annotation class EphyraSpanishFontScalePreviews

/** The phone cell alone: a screen's own frame, and each state of it. */
@PreviewWrapper(EphyraScreenshotThemeWrapper::class)
@Preview(name = "phone", device = PHONE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
annotation class EphyraScreenStatePreview

/** The light phone cell alone, beside [EphyraScreenStatePreview]'s dark one, for a state whose colours matter in both. */
@PreviewWrapper(EphyraScreenshotThemeWrapper::class)
@Preview(name = "phone-light", device = PHONE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_NO, locale = SOURCE_LOCALE)
annotation class EphyraScreenStateLightPreview

/** The wide window the console shows two panes on: a landscape tablet, in the dark. */
@PreviewWrapper(EphyraScreenshotThemeWrapper::class)
@Preview(name = "tablet", device = TABLET_LANDSCAPE_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
annotation class EphyraWidePreview

/** The wide window in Spanish, whose copy runs longer than English. */
@PreviewWrapper(EphyraScreenshotThemeWrapper::class)
@Preview(name = "tablet-es", device = TABLET_LANDSCAPE_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = "es")
annotation class EphyraWideSpanishPreview

/** The TV harness painting the window's black, for a screen root, wearing [EphyraBrand]. */
class EphyraTvScreenshotThemeOnBlackWrapper : TvScreenshotThemeOnBlackWrapper() {
    override val brand: BingeBrand = EphyraBrand
}

/** A television screen root, with the window's black behind it. */
@PreviewWrapper(EphyraTvScreenshotThemeOnBlackWrapper::class)
@Preview(name = "tv", device = TV_PREVIEW_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
annotation class EphyraTvScreenPreviews

/** A television screen root in Spanish, and at font scales 1.3 and 2.0. */
@PreviewWrapper(EphyraTvScreenshotThemeOnBlackWrapper::class)
@Preview(name = "tv-es", device = TV_PREVIEW_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = "es")
@Preview(name = "tv-font13", device = TV_PREVIEW_SPEC, fontScale = 1.3f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
@Preview(name = "tv-font20", device = TV_PREVIEW_SPEC, fontScale = 2f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
annotation class EphyraTvStressPreviews
