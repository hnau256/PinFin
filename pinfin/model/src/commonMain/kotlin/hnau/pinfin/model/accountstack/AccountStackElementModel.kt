package hnau.pinfin.model.accountstack

import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.pinfin.model.IconModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface AccountStackElementModel : GoBackHandlerProvider {

    val key: Int

    data class Info(
        val model: AccountModel,
    ) : AccountStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 0
    }

    data class Icon(
        val model: IconModel,
    ) : AccountStackElementModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 1
    }

    @Serializable
    sealed interface Skeleton {

        val key: Int

        @Serializable
        @SerialName("info")
        data class Info(
            val skeleton: AccountModel.Skeleton,
        ) : Skeleton {

            override val key: Int
                get() = 0
        }

        @Serializable
        @SerialName("icon")
        data class Icon(
            val skeleton: IconModel.Skeleton = IconModel.Skeleton(),
        ) : Skeleton {

            override val key: Int
                get() = 1
        }
    }
}
