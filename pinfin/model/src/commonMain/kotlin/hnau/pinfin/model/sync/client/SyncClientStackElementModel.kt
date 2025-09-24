package hnau.pinfin.model.sync.client

import hnau.common.app.model.goback.GoBackHandler
import hnau.pinfin.model.sync.client.budget.SyncClientLoadBudgetModel
import hnau.pinfin.model.sync.client.list.SyncClientListModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface SyncClientStackElementModel {

    val key: Int

    val goBackHandler: GoBackHandler

    data class List(
        val model: SyncClientListModel,
    ) : SyncClientStackElementModel {

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler

        override val key: Int
            get() = 0
    }

    data class Budget(
        val model: SyncClientLoadBudgetModel,
    ) : SyncClientStackElementModel {

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler

        override val key: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        val key: Int

        @Serializable
        @SerialName("list")
        data object List : Skeleton {

            override val key: Int
                get() = 0
        }

        @Serializable
        @SerialName("budget")
        data class Budget(
            val skeleton: SyncClientLoadBudgetModel.Skeleton,
        ) : Skeleton {

            override val key: Int
                get() = 1
        }
    }
}
