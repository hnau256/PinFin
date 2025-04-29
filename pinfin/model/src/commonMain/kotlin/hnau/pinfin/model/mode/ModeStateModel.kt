package hnau.pinfin.model.mode

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.manage.ManageModel
import hnau.pinfin.model.sync.SyncStackModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ModeStateModel: GoBackHandlerProvider {

    val id: Int

    data class Manage(
        val model: ManageModel,
    ): GoBackHandlerProvider by model, ModeStateModel {

        override val id: Int
            get() = 0
    }

    data class Sync(
        val model: SyncStackModel,
    ): GoBackHandlerProvider by model, ModeStateModel {

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
            val skeleton: SyncStackModel.Skeleton,
        ): Skeleton
    }
}