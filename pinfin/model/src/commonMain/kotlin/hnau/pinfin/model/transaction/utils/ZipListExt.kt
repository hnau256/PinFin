package hnau.pinfin.model.transaction.utils

import arrow.core.toNonEmptyListOrNull
import hnau.pinfin.model.utils.ZipList
import hnau.pinfin.model.utils.exclude

inline fun <T> ZipList<T>.remove(
    predicate: (T) -> Boolean,
): ZipList<T>? {

    before
        .exclude(predicate)
        ?.let { (newBefore) ->
            return copy(before = newBefore)
        }

    after
        .exclude(predicate)
        ?.let { (newAfter) ->
            return copy(after = newAfter)
        }

    if (!predicate(selected)) {
        return this
    }

    before
        .toNonEmptyListOrNull()
        ?.let { nonEmptyBefore ->
            return copy(
                before = nonEmptyBefore.dropLast(1),
                selected = nonEmptyBefore.last(),
            )
        }

    after
        .toNonEmptyListOrNull()
        ?.let { nonEmptyAfter ->
            return copy(
                selected = nonEmptyAfter.head,
                after = nonEmptyAfter.tail,
            )
        }

    return null
}