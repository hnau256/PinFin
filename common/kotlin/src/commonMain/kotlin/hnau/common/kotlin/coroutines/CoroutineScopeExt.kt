package hnau.common.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun CoroutineScope.createChild(
    additionalContext: CoroutineContext = EmptyCoroutineContext,
    createChildJob: (parentJob: Job) -> Job = ::SupervisorJob,
) = CoroutineScope(
    context = coroutineContext + additionalContext + createChildJob(coroutineContext.job),
)
