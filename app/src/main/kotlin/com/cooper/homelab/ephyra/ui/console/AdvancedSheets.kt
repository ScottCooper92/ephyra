package com.cooper.homelab.ephyra.ui.console

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.cooper.homelab.ephyra.R
import com.cooper.homelab.ephyra.server.AdvancedSettings

private fun AdvancedNumber.body(): Int =
    when (this) {
        AdvancedNumber.RetentionDays -> R.string.advanced_retention_body
        AdvancedNumber.MonitorDelay -> R.string.advanced_monitor_delay_body
        AdvancedNumber.UpdateDuration -> R.string.advanced_update_duration_body
        AdvancedNumber.ScanConcurrency -> R.string.advanced_scan_concurrency_body
        AdvancedNumber.ImageEncodingLimit -> R.string.advanced_image_limit_body
        AdvancedNumber.ExtractionTimeout -> R.string.advanced_extraction_timeout_body
        AdvancedNumber.SlowThreshold -> R.string.advanced_slow_threshold_body
        AdvancedNumber.InactiveThreshold -> R.string.advanced_inactive_body
        AdvancedNumber.CacheSize -> R.string.advanced_cache_body
    }

private fun AdvancedNumber.current(settings: AdvancedSettings): String =
    when (this) {
        AdvancedNumber.RetentionDays -> settings.activityLogRetentionDays?.toString().orEmpty()
        AdvancedNumber.MonitorDelay -> settings.libraryMonitorDelay.toString()
        AdvancedNumber.UpdateDuration -> settings.libraryUpdateDuration.toString()
        AdvancedNumber.ScanConcurrency -> settings.libraryScanConcurrency.toString()
        AdvancedNumber.ImageEncodingLimit -> settings.parallelImageEncodingLimit.toString()
        AdvancedNumber.ExtractionTimeout -> settings.imageExtractionTimeoutMs.toString()
        AdvancedNumber.SlowThreshold -> settings.slowResponseThresholdMs.toString()
        AdvancedNumber.InactiveThreshold -> settings.inactiveSessionThreshold.toString()
        AdvancedNumber.CacheSize -> settings.cacheSize.toString()
    }

/**
 * A number's sheet.
 */
@Composable
internal fun NumberSheet(
    number: AdvancedNumber,
    settings: AdvancedSettings,
    actions: AdvancedActions,
) = TextSettingSheet(onDismissRequest = { actions.onEdit(null) }) { NumberForm(number, settings, actions) }

/** A number's form: Done keeps what's typed if it is in range, and says so if it isn't; Cancel leaves the setting as it was. */
@Composable
internal fun NumberForm(
    number: AdvancedNumber,
    settings: AdvancedSettings,
    actions: AdvancedActions,
) {
    var value by rememberSaveable(number) { mutableStateOf(number.current(settings)) }
    var tried by rememberSaveable(number) { mutableStateOf(false) }
    val valid = number.parse(value) != null
    TextSettingForm(
        title = stringResource(number.title()),
        body = stringResource(number.body()),
        value = value,
        onValueChange = { value = it },
        onDone = {
            tried = true
            if (valid) actions.onSave(number, value)
        },
        onCancel = { actions.onEdit(null) },
        error = if (tried && !valid) stringResource(R.string.advanced_number_invalid, number.min, number.max) else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

/** A list's sheet. */
@Composable
internal fun ListSheet(
    list: AdvancedList,
    settings: AdvancedSettings,
    actions: AdvancedActions,
) = TextSettingSheet(onDismissRequest = { actions.onEditList(null) }) { ListForm(list, settings, actions) }

/** A list's form: the hosts one per line, or the cast receivers one per line as `id, name`. */
@Composable
internal fun ListForm(
    list: AdvancedList,
    settings: AdvancedSettings,
    actions: AdvancedActions,
) {
    val current =
        when (list) {
            AdvancedList.CorsHosts -> settings.corsHosts.joinToString("\n")
            AdvancedList.CastReceivers -> settings.castReceivers.joinToString("\n") { "${it.id}, ${it.name}" }
        }
    var value by rememberSaveable(list) { mutableStateOf(current) }
    var tried by rememberSaveable(list) { mutableStateOf(false) }
    val valid = list == AdvancedList.CorsHosts || parseCastReceivers(value) != null
    TextSettingForm(
        title = stringResource(if (list == AdvancedList.CorsHosts) R.string.advanced_cors else R.string.advanced_cast),
        body = stringResource(if (list == AdvancedList.CorsHosts) R.string.advanced_cors_body else R.string.advanced_cast_body),
        value = value,
        onValueChange = { value = it },
        onDone = {
            tried = true
            if (valid) actions.onSaveList(list, value)
        },
        onCancel = { actions.onEditList(null) },
        error = if (tried && !valid) stringResource(R.string.advanced_cast_invalid) else null,
        singleLine = false,
        minLines = LIST_MIN_LINES,
        maxLines = LIST_MAX_LINES,
    )
}

private const val LIST_MIN_LINES = 3
private const val LIST_MAX_LINES = 8
