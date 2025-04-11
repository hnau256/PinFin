package hnau.pinfin.client.model.mainstack

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.client.model.MainModel
import hnau.pinfin.client.model.transaction.TransactionModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface MainStackElementModel : GoBackHandlerProvider {

    val key: Any?

    data class Main(
        val model: MainModel,
    ) : MainStackElementModel, GoBackHandlerProvider by model {

        override val key: Any
            get() = 0
    }

    data class Transaction(
        val model: TransactionModel,
    ) : MainStackElementModel, GoBackHandlerProvider by model {

        override val key: Any
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("main")
        data class Main(
            val skeleton: MainModel.Skeleton = MainModel.Skeleton(),
        ) : Skeleton

        @Serializable
        @SerialName("transaction")
        data class Transaction(
            val skeleton: TransactionModel.Skeleton,
        ) : Skeleton
    }
}
