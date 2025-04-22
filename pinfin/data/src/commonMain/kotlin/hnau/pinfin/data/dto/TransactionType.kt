package hnau.pinfin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {

    @SerialName("entry")
    Entry,

    @SerialName("transfer")
    Transfer;

    companion object {

        val default = Entry
    }
}

@Serializable
data class TransactionTypeValues<out T>(
    @SerialName("entry")
    val entry: T,

    @SerialName("transfer")
    val transfer: T,
) {

    operator fun get(
        type: TransactionType,
    ): T = when (type) {
        TransactionType.Entry -> entry
        TransactionType.Transfer -> transfer
    }
}