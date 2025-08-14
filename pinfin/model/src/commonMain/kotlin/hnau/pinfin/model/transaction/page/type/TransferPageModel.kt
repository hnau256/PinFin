@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.page.type

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TransferPageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    ) : TypePageModel {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton 

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}