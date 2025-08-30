package hnau.pinfin.model.utils

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import kotlinx.serialization.Serializable

//TODO extract to common-kotlin
@Serializable
data class ZipList<T>(
    val before: List<T>,
    val selected: T,
    val after: List<T>,
) : AbstractList<T>() {

    override val size: Int
        get() = before.size + 1 + after.size

    override fun get(
        index: Int,
    ): T = before.size.let { beforeSize ->
        when {
            index < beforeSize -> before[index]
            index == beforeSize -> selected
            else -> after[index - beforeSize - 1]
        }
    }

    inline fun <O> mapFull(
        transform: (index: Int, selected: Boolean, value: T) -> O,
    ): ZipList<O> = before.size.let { beforeSize ->
        ZipList(
            before = before.mapIndexed { i, value ->
                transform(
                    i,
                    false,
                    value,
                )
            },
            selected = transform(beforeSize, true, selected),
            after = after.mapIndexed { i, value ->
                transform(
                    i + beforeSize + 1,
                    false,
                    value,
                )
            }
        )
    }

    inline fun <O> map(
        transform: (T) -> O,
    ): ZipList<O> = mapFull { _, _, value ->
        transform(value)
    }

    fun back(): ZipList<T>? = before
        .toNonEmptyListOrNull()
        ?.let { nonEmptyBefore ->
            ZipList(
                before = nonEmptyBefore.dropLast(1),
                selected = nonEmptyBefore.last(),
                after = buildList {
                    add(selected)
                    addAll(after)
                }
            )
        }

    fun forward(): ZipList<T>? = after
        .toNonEmptyListOrNull()
        ?.let { nonEmptyAfter ->
            ZipList(
                before = before + selected,
                selected = nonEmptyAfter.head,
                after = nonEmptyAfter.tail,
            )
        }

    fun toNonEmptyList(): NonEmptyList<T> = before.toNonEmptyListOrNull().foldNullable(
        ifNull = {
            NonEmptyList(
                head = selected,
                tail = after,
            )
        },
        ifNotNull = { nonEmptyBefore ->
            nonEmptyBefore + selected + after
        }
    )
}

inline fun <T> Iterable<T>.toZipListOrNull(
    checkIsSelected: (T) -> Boolean,
): ZipList<T>? = this
    .fold(
        initial = ZipListBuilder<T>()
    ) { builder, value ->
        builder.add(
            value = value,
            checkIsSelected = checkIsSelected,
        )
    }
    .toSelectedListOrNull()

@PublishedApi
internal data class ZipListBuilder<T>(
    val before: List<T> = emptyList(),
    val selected: Option<T> = None,
    val after: List<T> = emptyList(),
) {

    inline fun add(
        value: T,
        checkIsSelected: (T) -> Boolean,
    ): ZipListBuilder<T> = selected.fold(
        ifEmpty = {
            checkIsSelected(value).foldBoolean(
                ifTrue = {
                    ZipListBuilder(
                        before = before,
                        selected = Some(value),
                        after = after
                    )
                },
                ifFalse = {
                    ZipListBuilder(
                        before = before + value,
                        selected = selected,
                        after = after
                    )
                }
            )
        },
        ifSome = {
            ZipListBuilder(
                before = before,
                selected = selected,
                after = after + value
            )
        }
    )

    fun toSelectedListOrNull(): ZipList<T>? = selected
        .map { selected ->
            ZipList(
                before = before,
                selected = selected,
                after = after,
            )
        }
        .getOrNull()
}