package hnau.common.kotlin

inline fun <R> Boolean.ifTrue(
    block: () -> R,
): R? = when (this) {
    true -> block()
    false -> null
}