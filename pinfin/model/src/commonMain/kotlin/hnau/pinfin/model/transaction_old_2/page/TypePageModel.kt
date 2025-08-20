@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction_old_2.page

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapStateLite
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction_old_2.page.type.TypePageModel
import hnau.pinfin.model.transaction_old_2.part.type.TypePartModel
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
) {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton

    val model: StateFlow<TypePageModel> = type.mapWithScope(
        scope = scope,
    ) { typeScope, type ->
        type.createPage(
            scope = scope,
        )
    }

    val goBackHandler: GoBackHandler = model
        .flatMapStateLite(TypePageModel::goBackHandler)
}