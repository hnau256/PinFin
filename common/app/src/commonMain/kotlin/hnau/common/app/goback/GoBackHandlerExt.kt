package hnau.common.app.goback

import hnau.common.kotlin.coroutines.flatMapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

val NeverGoBackHandler: GoBackHandler = MutableStateFlow(null)

fun GoBackHandler.fallback(
    scope: CoroutineScope,
    fallback: GoBackHandler,
): GoBackHandler = flatMapState(scope) { goBackOrNull ->
    goBackOrNull
        ?.let(::MutableStateFlow)
        ?: fallback
}
