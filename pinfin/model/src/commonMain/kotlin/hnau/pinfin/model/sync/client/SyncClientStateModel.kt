package hnau.pinfin.model.sync.client

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.sync.client.budget.SyncClientLoadBudgetModel
import hnau.pinfin.model.sync.client.list.SyncClientListModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface SyncClientStateModel : GoBackHandlerProvider {

    val key: Int

    data class List(
        val model: SyncClientListModel,
    ) : SyncClientStateModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 0
    }

    data class Budget(
        val model: SyncClientLoadBudgetModel,
    ) : SyncClientStateModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("list")
        data class List(
            val skeleton: SyncClientListModel.Skeleton = SyncClientListModel.Skeleton(),
        ) : Skeleton

        @Serializable
        @SerialName("budget")
        data class Budget(
            val skeleton: SyncClientLoadBudgetModel.Skeleton,
        ) : Skeleton
    }
}
