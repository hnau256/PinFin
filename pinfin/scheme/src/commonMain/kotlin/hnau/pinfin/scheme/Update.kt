package hnau.pinfin.scheme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import hnau.pinfin.scheme.Transaction as TransactionDTO

@Serializable
sealed interface Update {

    @Serializable
    @SerialName("transaction")
    data class Transaction(
        @SerialName("id")
        val id: TransactionDTO.Id,

        @SerialName("transaction")
        val transaction: TransactionDTO,
    ) : Update
}