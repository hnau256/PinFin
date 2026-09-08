package org.hnau.pinfin.model.mcp

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal fun schemaObject(
    description: String? = null,
    required: List<String> = emptyList(),
    properties: JsonObjectBuilder.() -> Unit,
): JsonObject = buildJsonObject {
    put("type", "object")
    description?.let { put("description", it) }
    putJsonObject("properties", properties)
    if (required.isNotEmpty()) {
        putJsonArray("required") { required.forEach { add(JsonPrimitive(it)) } }
    }
}

internal fun schemaString(
    description: String,
    enum: List<String>? = null,
    format: String? = null,
): JsonObject = buildJsonObject {
    put("type", "string")
    put("description", description)
    format?.let { put("format", it) }
    enum?.let { values ->
        putJsonArray("enum") { values.forEach { add(JsonPrimitive(it)) } }
    }
}

internal fun schemaInteger(
    description: String,
    minimum: Int? = null,
    maximum: Int? = null,
): JsonObject = buildJsonObject {
    put("type", "integer")
    put("description", description)
    minimum?.let { put("minimum", it) }
    maximum?.let { put("maximum", it) }
}

internal fun schemaBoolean(
    description: String,
): JsonObject = buildJsonObject {
    put("type", "boolean")
    put("description", description)
}

internal fun schemaArray(
    description: String,
    items: JsonObject,
): JsonObject = buildJsonObject {
    put("type", "array")
    put("description", description)
    put("items", items)
}

internal fun schemaOneOf(
    description: String,
    variants: List<JsonObject>,
): JsonObject = buildJsonObject {
    put("description", description)
    put("oneOf", buildJsonArray { variants.forEach { add(it) } })
}
