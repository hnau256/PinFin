package hnau.pinfin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import hnau.pinfin.data.dto.Transaction as TransactionDTO

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

    @Serializable
    @SerialName("remove_transaction")
    data class RemoveTransaction(
        @SerialName("id")
        val id: TransactionDTO.Id,
    ) : Update
}