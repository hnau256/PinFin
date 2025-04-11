package hnau.common.app.model.stack

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

fun <T> MutableStateFlow<NonEmptyStack<T>>.push(
    element: T,
) = update { stack ->
    stack + element
}

fun <T> NonEmptyStack<T>.tryDropLast(): NonEmptyStack<T>? {
    if (head.isEmpty()) {
        return null
    }
    return NonEmptyStack(
        head = head.dropLast(1),
        tail = head.last(),
    )
}

fun <T> MutableStateFlow<NonEmptyStack<T>>.tryDropLast(): Boolean {
    val newStack = value.tryDropLast() ?: return false
    value = newStack
    return true
}
