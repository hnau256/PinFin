@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.page.type

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction.page.type.entry.EntryPagePageModel
import hnau.pinfin.model.transaction.part.type.TypePartModel
import hnau.pinfin.model.transaction.part.type.entry.EntryPart
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class EntryPageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    val page: StateFlow<EntryPagePageModel>,
) : TypePageModel {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}