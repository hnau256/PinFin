@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.page

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction.page.type.TypePageModel
import hnau.pinfin.model.transaction.part.type.TypePartModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TypePageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
        type: StateFlow<TypePartModel>,
): PageModel {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton

    val model: StateFlow<TypePageModel> = type.mapWithScope(scope) { typeScope, type ->
        type.createPage(
            scope = scope,
                    )
    }

    override val goBackHandler: GoBackHandler = model
        .flatMapState(scope) { model -> model.goBackHandler }
}