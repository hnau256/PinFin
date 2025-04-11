package hnau.common.kotlin.coroutines

import arrow.core.partially1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


fun <A> operationOrNullIfExecuting(
    scope: CoroutineScope,
    action: suspend (A) -> Unit,
): StateFlow<((A) -> Unit)?> = MutableStateFlow(false).let { executing ->
    executing
        .mapState(scope) { isExecuting ->
            when (isExecuting) {
                true -> null
                false -> {
                    { argument ->
                        scope.launch {
                            executing.value = true
                            action(argument)
                            executing.value = false
                        }
                    }
                }
            }
        }
}


fun actionOrNullIfExecuting(
    scope: CoroutineScope,
    action: suspend () -> Unit,
): StateFlow<(() -> Unit)?> = operationOrNullIfExecuting<Unit>(
    scope = scope,
    action = { action() },
).mapState(scope) { operationOrNull ->
    operationOrNull?.partially1(Unit)
}