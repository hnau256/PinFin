package hnau.pinfin.model.budgetstack

import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.pinfin.model.CategoriesModel
import hnau.pinfin.model.accountstack.AccountStackModel
import hnau.pinfin.model.budget.BudgetModel
import hnau.pinfin.model.categorystack.CategoryStackModel
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

    data class Account(
        val model: AccountStackModel,
    ) : BudgetStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 2
    }

    data class Categories(
        val model: CategoriesModel,
    ) : BudgetStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 3
    }

    data class Category(
        val model: CategoryStackModel,
    ) : BudgetStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 4
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

        @Serializable
        @SerialName("account")
        data class Account(
            val skeleton: AccountStackModel.Skeleton,
        ) : Skeleton {

            override val key: Int
                get() = 2
        }

        @Serializable
        @SerialName("categories")
        data class Categories(
            val skeleton: CategoriesModel.Skeleton,
        ) : Skeleton {

            override val key: Int
                get() = 3
        }

        @Serializable
        @SerialName("category")
        data class Category(
            val skeleton: CategoryStackModel.Skeleton,
        ) : Skeleton {

            override val key: Int
                get() = 4
        }
    }
}
