package hnau.common.kotlin.mapper

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

fun <T> Json.toMapper(
    serializer: KSerializer<T>,
): Mapper<String, T> = Mapper(
    direct = { json -> decodeFromString(serializer, json) },
    reverse = { value -> encodeToString(serializer, value) },
)
