package hnau.pinfin.model.budgetsstack

import hnau.common.app.model.goback.GoBackHandler
import hnau.pinfin.model.budgetslist.BudgetsListModel
import hnau.pinfin.model.sync.SyncStackModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BudgetsStackElementModel {

    val key: Int

    val goBackHandler: GoBackHandler

    data class List(
        val model: BudgetsListModel,
    ) : BudgetsStackElementModel {

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler

        override val key: Int
            get() = 0
    }

    data class Sync(
        val model: SyncStackModel,
    ) : BudgetsStackElementModel {

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
        @SerialName("sync")
        data class Sync(
            val skeleton: SyncStackModel.Skeleton = SyncStackModel.Skeleton(),
        ) : Skeleton {

            override val key: Int
                get() = 1
        }
    }
}
