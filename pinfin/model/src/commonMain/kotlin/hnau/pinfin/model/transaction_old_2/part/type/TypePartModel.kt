package hnau.pinfin.model.transaction_old_2.part.type

import hnau.common.app.model.goback.GoBackHandler
import hnau.pinfin.model.transaction_old_2.page.type.TypePageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface TypePartModel {

    val goBackHandler: GoBackHandler

    val key: Int

    fun createPage(
        scope: CoroutineScope,
    ): TypePageModel


    data class Entry(
        val model: EntryModel,
    ) : TypePartModel {

        override val key: Int
            get() = 0

        override fun createPage(
            scope: CoroutineScope,
        ): TypePageModel = model.createPage(
            scope = scope,
        )

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Transfer(
        val model: TransferModel,
    ) : TypePartModel {

        override val key: Int
            get() = 1

        override fun createPage(
            scope: CoroutineScope,
        ): TypePageModel = model.createPage(
            scope = scope,
        )

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    @Serializable
    sealed interface Skeleton {

        @Serializable
        @SerialName("entry")
        data class Entry(
            val skeleton: EntryModel.Skeleton,
        ) : Skeleton

        @Serializable
        @SerialName("transfer")
        data class Transfer(
            val skeleton: TransferModel.Skeleton,
        ) : Skeleton
    }
}