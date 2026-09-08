package org.hnau.pinfin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.pinfin.data.expression.AmountExpression

@Serializable
data class Record<out A>(
    @SerialName("category")
    val category: A,

    @SerialName("amount")
    val amount: AmountExpression,

    @SerialName("comment")
    val comment: Comment,
)