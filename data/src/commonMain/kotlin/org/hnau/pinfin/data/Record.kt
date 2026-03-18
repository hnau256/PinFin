package org.hnau.pinfin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.pinfin.data.expression.AmountExpression

@Serializable
data class Record(
    @SerialName("category")
    val category: CategoryId,

    @SerialName("amount")
    val amount: AmountExpression,

    @SerialName("comment")
    val comment: Comment,
)