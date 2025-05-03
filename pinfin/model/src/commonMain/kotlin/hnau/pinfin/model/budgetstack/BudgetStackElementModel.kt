package hnau.pinfin.model.budgetstack

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.budget.BudgetModel
import hnau.pinfin.model.transaction.TransactionModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetStackElementModel : GoBackHandlerProvider {

    val key: Int

    data class Budget(
        val model: BudgetModel,
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

        val key: Int

        @Serializable
        @SerialName("budget")
        data class Budget(
            val skeleton: BudgetModel.Skeleton = BudgetModel.Skeleton(),
        ) : Skeleton {

            override val key: Int
                get() = 0
        }

        @Serializable
        @SerialName("transaction")
        data class Transaction(
            val skeleton: TransactionModel.Skeleton,
        ) : Skeleton {

            override val key: Int
                get() = 1
        }
    }
}
