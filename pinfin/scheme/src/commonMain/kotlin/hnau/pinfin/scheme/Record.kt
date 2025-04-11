package hnau.pinfin.scheme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Record(
    @SerialName("category")
    val category: CategoryId,

    @SerialName("amount")
    val amount: Amount,

    @SerialName("comment")
    val comment: Comment,
)