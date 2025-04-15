package hnau.pinfin.client.model.budgetstack

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.client.model.TransactionsModel
import hnau.pinfin.client.model.transaction.TransactionModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetStackElementModel : GoBackHandlerProvider {

    val key: Int

    data class Transactions(
        val model: TransactionsModel,
    ) : BudgetStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 0
    }

    data class Transaction(
        val model: TransactionModel,
    ) : BudgetStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("transactions")
        data class Transactions(
            val skeleton: TransactionsModel.Skeleton = TransactionsModel.Skeleton(),
        ) : Skeleton

        @Serializable
        @SerialName("transaction")
        data class Transaction(
            val skeleton: TransactionModel.Skeleton,
        ) : Skeleton
    }
}
