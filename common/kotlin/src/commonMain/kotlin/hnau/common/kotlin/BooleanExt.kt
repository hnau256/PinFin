package hnau.common.kotlin

inline fun <R> Boolean.foldBoolean(
    ifTrue: () -> R,
    ifFalse: () -> R,
): R = when (this) {
    true -> ifTrue()
    false -> ifFalse()
}

inline fun <R> Boolean.ifTrue(
    block: () -> R,
): R? = foldBoolean(
    ifTrue = block,
    ifFalse = { null },
)

inline fun <R> Boolean.ifFalse(
    block: () -> R,
): R? = foldBoolean(
    ifTrue = { null },
    ifFalse = block,
)