package hnau.pinfin.model.sync.client

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.sync.client.budget.SyncClientBudgetModel
import hnau.pinfin.model.sync.client.list.SyncClientListModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface SyncClientStackElementModel : GoBackHandlerProvider {

    val key: Int

    data class List(
        val model: SyncClientListModel,
    ) : SyncClientStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 0
    }

    data class Budget(
        val model: SyncClientBudgetModel,
    ) : SyncClientStackElementModel, GoBackHandlerProvider by model {

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
            val skeleton: SyncClientBudgetModel.Skeleton,
        ) : Skeleton
    }
}
