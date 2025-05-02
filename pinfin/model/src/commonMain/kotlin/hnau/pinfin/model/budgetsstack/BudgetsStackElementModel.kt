package hnau.pinfin.model.budgetsstack

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.budgetslist.BudgetsListModel
import hnau.pinfin.model.sync.SyncStackModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetsStackElementModel : GoBackHandlerProvider {

    val key: Int

    data class List(
        val model: BudgetsListModel,
    ) : BudgetsStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 0
    }

    data class Sync(
        val model: SyncStackModel,
    ) : BudgetsStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("list")
        data class List(
            val skeleton: BudgetsListModel.Skeleton = BudgetsListModel.Skeleton(),
        ) : Skeleton

        @Serializable
        @SerialName("sync")
        data class Sync(
            val skeleton: SyncStackModel.Skeleton = SyncStackModel.Skeleton(),
        ) : Skeleton
    }
}
