package hnau.pinfin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import hnau.pinfin.data.Transaction as TransactionDTO

@Serializable
sealed interface UpdateType {

    @Serializable
    @SerialName("transaction")
    data class Transaction(
        @SerialName("id")
        val id: TransactionDTO.Id,

        @SerialName("transaction")
        val transaction: TransactionDTO,
    ) : UpdateType

    @Serializable
    @SerialName("remove_transaction")
    data class RemoveTransaction(
        @SerialName("id")
        val id: TransactionDTO.Id,
    ) : UpdateType

    @Serializable
    @SerialName("config")
    data class Config(
        val config: BudgetConfig,
    ) : UpdateType
}