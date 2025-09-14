package hnau.pinfin.model.utils

import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.scopedInState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow


//TODO extract to common-kotlin
fun <I, O> StateFlow<I>.flatMapWithScope(
    scope: CoroutineScope,
    transform: (CoroutineScope, I) -> StateFlow<O>,
): StateFlow<O> = this
    .scopedInState(scope)
    .flatMapState(scope) { (scope, value) ->
        transform(scope, value)
    }