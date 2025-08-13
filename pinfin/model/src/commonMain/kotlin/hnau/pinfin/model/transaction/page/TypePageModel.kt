@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.page

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction.page.type.EntryPageModel
import hnau.pinfin.model.transaction.page.type.PageTypeModel
import hnau.pinfin.model.transaction.page.type.TransferPageModel
import hnau.pinfin.model.transaction.part.type.PartTypeModel
import hnau.pinfin.model.transaction.utils.NavAction
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TypePageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    private val navAction: NavAction,
    type: StateFlow<PartTypeModel>,
): PageModel {

    @Pipe
    interface Dependencies {

        fun entry(): EntryPageModel.Dependencies

        fun transfer(): TransferPageModel.Dependencies
    }

    @Serializable
    /*data*/ class Skeleton

    val model: StateFlow<PageTypeModel> = type.mapWithScope(scope) { typeScope, type ->
        type.createPage(
            scope = scope,
            navAction = navAction,
        )
    }

    override val goBackHandler: GoBackHandler = model
        .flatMapState(scope) { model -> model.goBackHandler }
}