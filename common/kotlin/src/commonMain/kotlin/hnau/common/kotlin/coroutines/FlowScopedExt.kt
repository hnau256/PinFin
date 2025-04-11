package hnau.common.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.runningFold

inline fun <T> Flow<T>.scoped(
    parentScope: CoroutineScope,
    crossinline createChildJob: (parentJob: Job) -> Job = ::SupervisorJob,
): Flow<Scoped<T>> = runningFold<T, Scoped<T>?>(
    initial = null,
) { previous, nextValue ->
    previous?.scope?.cancel()
    val nextScope = parentScope.createChild(createChildJob = createChildJob)
    Scoped(nextScope, nextValue)
}
    .filterNotNull()

inline fun <T> StateFlow<T>.scopedInState(
    parentScope: CoroutineScope,
    crossinline createChildJob: (parentJob: Job) -> Job = ::SupervisorJob,
): StateFlow<Scoped<T>> = runningFoldState(
    scope = parentScope,
    createInitial = { value ->
        val initialScope = parentScope.createChild(createChildJob = createChildJob)
        Scoped(initialScope, value)
    },
    operation = { previousScopeWithValue, nextValue ->
        previousScopeWithValue.scope.cancel()
        val nextScope = parentScope.createChild(createChildJob = createChildJob)
        Scoped(nextScope, nextValue)
    },
)
