package org.hnau.pinfin.model.utils.budget.query

import kotlinx.serialization.json.Json

/** Shared JSON configuration for every DTO of the query layer (MCP and, later, AppFunctions). */
val queryJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    classDiscriminator = "type"
}
