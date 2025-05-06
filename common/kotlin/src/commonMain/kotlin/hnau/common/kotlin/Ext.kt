package hnau.common.kotlin

inline fun <T> T?.ifNull(
    ifNull: () -> T,
) = this ?: ifNull()

inline fun <reified O> Any?.castOrElse(
    elseAction: () -> O,
) = when (this) {
    is O -> this
    else -> elseAction()
}

inline fun <reified O> Any?.castOrNull() = this as? O

inline fun <reified O> Any?.castOrThrow() = this as O

@Suppress("NOTHING_TO_INLINE")
inline fun <T> it(it: T) = it


inline fun <T, R> T?.foldNullable(
    ifNull: () -> R,
    ifNotNull: (T & Any) -> R,
): R = when (this) {
    null -> ifNull()
    else -> ifNotNull(this)
}
