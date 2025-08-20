@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction_old_2.page.type.entry.record

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction_old_2.part.type.entry.record.RecordInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class RecordPageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    info: RecordInfo,
) {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton(

    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}