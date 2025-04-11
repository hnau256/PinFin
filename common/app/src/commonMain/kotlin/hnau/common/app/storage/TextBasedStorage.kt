package hnau.common.app.storage

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.toMapper
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

fun TextBasedStorage(
    initial: String?,
    update: suspend (String) -> Unit,
): Storage = Storage(
    initialValues = initial
        ?.let(mapper.direct)
        ?: emptyMap(),
    updateValues = { newValues ->
        val serialized = mapper.reverse(newValues)
        update(serialized)
    },
)

fun Storage.Companion.textBased(
    initial: String?,
    update: suspend (String) -> Unit,
): Storage = TextBasedStorage(
    initial = initial,
    update = update,
)

private val mapper: Mapper<String, Map<String, String>> =
    Json.toMapper(MapSerializer(String.serializer(), String.serializer()))
