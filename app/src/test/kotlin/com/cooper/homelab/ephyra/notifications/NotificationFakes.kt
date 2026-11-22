package com.cooper.homelab.ephyra.notifications

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cooper.homelab.core.foundation.ServerId
import com.cooper.homelab.core.notifications.DataStoreFeedCursorStore
import com.cooper.homelab.core.notifications.FeedCursorStore
import com.cooper.homelab.core.testing.InMemoryDataStore

/** An [ActivityNotifier] that records what it was asked to post. */
class FakeNotifier(
    var mayPost: Boolean = true,
) : ActivityNotifier {
    val summaries = mutableListOf<ActivitySummary>()
    var connectionProblems = 0
    var connectionCancels = 0
    var activityCancels = 0

    override suspend fun canPost(): Boolean = mayPost

    override suspend fun notifyActivity(summary: ActivitySummary) {
        summaries += summary
    }

    override suspend fun notifyConnectionProblem(server: ServerId) {
        connectionProblems++
    }

    override suspend fun cancelConnectionProblem(server: ServerId) {
        connectionCancels++
    }

    override suspend fun cancelNotices(server: ServerId) {
        activityCancels++
    }
}

/** The cursor store the app uses, over a DataStore that lives in memory. */
fun testFeedCursors(): FeedCursorStore = DataStoreFeedCursorStore(InMemoryDataStore(), "feed_cursor")

/** The activity cursor the released version wrote, in the layout it wrote it: three preferences, the owner, the id and the date. */
suspend fun DataStore<Preferences>.writeReleasedCursor(
    owner: String,
    id: Long,
    dateMillis: Long?,
) {
    edit { prefs ->
        prefs[stringPreferencesKey("cursor_owner")] = owner
        prefs[longPreferencesKey("cursor_id")] = id
        dateMillis?.let { prefs[longPreferencesKey("cursor_date_millis")] = it }
    }
}

/** One activity-log entry as the server writes it, newest ids last. */
fun activityJson(
    id: Int,
    type: String,
    severity: String = "Information",
    date: String = "2026-11-01T10:%02d:00Z".format(id % 60),
): String =
    """{"Id":$id,"Name":"Entry $id with a secret address 203.0.113.9","Type":"$type","Severity":"$severity","Date":"$date","ShortOverview":"IP address: 203.0.113.9"}"""
