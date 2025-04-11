package hnau.common.app.storage

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.mapper.Mapper
import kotlinx.coroutines.flow.StateFlow

inline fun <I, O> StorageEntry<I>.transform(
    crossinline extract: I.() -> O,
    crossinline merge: I.(O) -> I,
): StorageEntry<O> = object : StorageEntry<O> {

    private val receiver: StorageEntry<I>
        get() = this@transform

    override val value: StateFlow<O> =
        receiver.value.mapStateLite(extract)

    override suspend fun updateValue(newValue: O) {
        val updatedValue = receiver.value.value.merge(newValue)
        receiver.updateValue(updatedValue)
    }
}

fun <I, O> StorageEntry<I>.map(
    mapper: Mapper<I, O>,
): StorageEntry<O> = transform(
    extract = mapper.direct,
    merge = { newValue -> mapper.reverse(newValue) },
)
