package org.hnau.pinfin.model.utils.budget

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hnau.commons.kotlin.it
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.mapper.toMapper
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.upchain.sync.core.ServerHost
import org.hnau.upchain.sync.http.HttpScheme

@Serializable
data class ShareCode
@Deprecated("Use ShareCode.create instead")
constructor(
    val id: BudgetId,
    val scheme: HttpScheme = HttpScheme.default,
    val host: ServerHost = ServerHost.default,
) {


    companion object {

        @Suppress("JSON_FORMAT_REDUNDANT")
        val stringMapper: Mapper<String, ShareCode> = Mapper<String, String>(
            direct = { string ->
                string
                    .dropWhile(Char::isWhitespace)
                    .dropLastWhile(Char::isWhitespace)
            },
            reverse = ::it
        ) + Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
        }.toMapper(serializer())

        @Suppress("DEPRECATION")
        fun create(
            id: BudgetId,
            info: BudgetInfo,
        ): ShareCode = ShareCode(
            id = id,
            scheme = info.sync.scheme,
            host = info.sync.host,
        )
    }
}