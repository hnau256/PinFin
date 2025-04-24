package hnau.pinfin.model.budgetsorsync

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.manage.ManageModel
import hnau.pinfin.model.SyncModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ManageOrSyncStateModel: GoBackHandlerProvider {

    val id: Int

    data class Manage(
        val model: ManageModel,
    ): GoBackHandlerProvider by model, ManageOrSyncStateModel {

        override val id: Int
            get() = 0
    }

    data class Sync(
        val model: SyncModel,
    ): GoBackHandlerProvider by model, ManageOrSyncStateModel {

        override val id: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("manage")
        data class Manage(
            val skeleton: ManageModel.Skeleton,
        ): Skeleton

        @Serializable
        @SerialName("sync")
        data class Sync(
            val skeleton: SyncModel.Skeleton,
        ): Skeleton
    }
}