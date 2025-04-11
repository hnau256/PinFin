package hnau.common.app.model.stack

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun StateFlow<NonEmptyStack<GoBackHandlerProvider>>.tailGoBackHandler(
    scope: CoroutineScope,
): GoBackHandler = flatMapState(scope) { stack ->
    stack.tail.goBackHandler
}

fun <T> MutableStateFlow<NonEmptyStack<T>>.stackGoBackHandler(
    scope: CoroutineScope,
): GoBackHandler = mapState(scope) { stack ->
    val newStack = stack.tryDropLast() ?: return@mapState null
    return@mapState { value = newStack }
}
