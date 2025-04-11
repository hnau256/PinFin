package hnau.common.app.storage

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.stringToBoolean
import hnau.common.kotlin.mapper.stringToFloat
import hnau.common.kotlin.mapper.stringToInt

fun Storage.Factory.Companion.create(
    read: suspend () -> Map<String, String>,
    write: suspend (Map<String, String>) -> Unit,
): Storage.Factory = Storage.Factory {
    Storage(
        initialValues = read(),
        updateValues = write,
    )
}

fun <T> Storage.entry(
    key: String,
    mapper: Mapper<String, T>,
    defaultValue: T,
): StorageEntry<T> = this
    .string(
        key = key,
        defaultValue = mapper.reverse(defaultValue),
    ).map(
        mapper = mapper,
    )

fun Storage.int(
    key: String,
    defaultValue: Int,
): StorageEntry<Int> = entry(
    key = key,
    defaultValue = defaultValue,
    mapper = Mapper.stringToInt,
)

fun Storage.float(
    key: String,
    defaultValue: Float,
): StorageEntry<Float> = entry(
    key = key,
    defaultValue = defaultValue,
    mapper = Mapper.stringToFloat,
)

fun Storage.boolean(
    key: String,
    defaultValue: Boolean,
): StorageEntry<Boolean> = entry(
    key = key,
    defaultValue = defaultValue,
    mapper = Mapper.stringToBoolean,
)
