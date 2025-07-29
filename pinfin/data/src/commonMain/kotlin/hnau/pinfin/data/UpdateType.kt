package hnau.pinfin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import hnau.pinfin.data.AccountConfig as AccountConfigDTO
import hnau.pinfin.data.CategoryConfig as CategoryConfigDTO
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

    @Serializable
    @SerialName("account_config")
    data class AccountConfig(
        val id: AccountId,
        val config: AccountConfigDTO,
    ) : UpdateType

    @Serializable
    @SerialName("category_config")
    data class CategoryConfig(
        val id: CategoryId,
        val config: CategoryConfigDTO,
    ) : UpdateType
}