package hnau.common.app.model.stack

import kotlinx.serialization.Serializable

@Serializable
data class NonEmptyStack<out T>(
    val head: List<T>,
    val tail: T,
) {

    constructor(
        tail: T,
    ) : this(
        head = emptyList(),
        tail = tail,
    )

    val all: List<T>
        get() = head + tail
}

inline fun <I, O> NonEmptyStack<I>.map(
    transform: (I) -> O,
): NonEmptyStack<O> = NonEmptyStack(
    head = head.map(transform),
    tail = tail.let(transform),
)

operator fun <T> NonEmptyStack<T>.plus(
    other: T,
): NonEmptyStack<T> = NonEmptyStack(
    head = all,
    tail = other,
)
