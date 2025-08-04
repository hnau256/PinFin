package hnau.pinfin.model.budgetstack

import hnau.common.app.model.goback.GoBackHandler
import hnau.pinfin.model.CategoriesModel
import hnau.pinfin.model.accountstack.AccountStackModel
import hnau.pinfin.model.budget.BudgetModel
import hnau.pinfin.model.categorystack.CategoryStackModel
import hnau.pinfin.model.transaction.TransactionModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetStackElementModel {

    val key: Int

    val goBackHandler: GoBackHandler

    data class Budget(
        val model: BudgetModel,
    ) : BudgetStackElementModel {

        override val key: Int
            get() = 0

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Transaction(
        val model: TransactionModel,
    ) : BudgetStackElementModel {

        override val key: Int
            get() = 1

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Account(
        val model: AccountStackModel,
    ) : BudgetStackElementModel {

        override val key: Int
            get() = 2

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Categories(
        val model: CategoriesModel,
    ) : BudgetStackElementModel {

        override val key: Int
            get() = 3

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Category(
        val model: CategoryStackModel,
    ) : BudgetStackElementModel {

        override val key: Int
            get() = 4

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
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
