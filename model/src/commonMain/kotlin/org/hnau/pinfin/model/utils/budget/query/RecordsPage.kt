package org.hnau.pinfin.model.utils.budget.query

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val MAX_RECORDS_LIMIT: Int = 200
const val DEFAULT_RECORDS_LIMIT: Int = 50

@Serializable
data class RecordsPage(
    @SerialName("records")
    val records: List<FlatRecord>,
    @SerialName("total")
    val total: Int,
    @SerialName("offset")
    val offset: Int,
    @SerialName("limit")
    val limit: Int,
    @SerialName("has_more")
    val hasMore: Boolean,
)
