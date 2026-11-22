package com.cooper.homelab.ephyra.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.cooper.homelab.core.foundation.Logger
import com.cooper.homelab.core.notifications.PollOutcome
import com.cooper.homelab.core.notifications.PollReactor
import com.cooper.homelab.core.notifications.runPollWork
import com.cooper.homelab.ephyra.auth.ServerIdentity

/** One run of the poll: checks the log and maps the outcome to WorkManager's result. */
class PollRunner(
    private val checker: ActivityChecker,
    private val reactor: PollReactor,
    private val serverIdentity: ServerIdentity,
) {
    suspend fun run(): PollOutcome = reactor.react(serverIdentity.currentServerId(), checker.check())
}

/**
 * The periodic poll. Everything with a decision in it is in [PollRunner]. A throw past the checker's own guards is transient by
 * nature, so [runPollWork] retries it rather than fails, and logs it by its type through [logger], never its message.
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters,
    private val runner: PollRunner,
    private val logger: Logger,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runPollWork(logger) { runner.run() }
}

/** Builds [NotificationWorker] with the graph's runner; WorkManager's own initialiser is removed in the manifest so this one is used. */
class NotificationWorkerFactory(
    private val runner: () -> PollRunner,
    private val logger: () -> Logger,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? =
        if (workerClassName ==
            NotificationWorker::class.java.name
        ) {
            NotificationWorker(appContext, workerParameters, runner(), logger())
        } else {
            null
        }
}
