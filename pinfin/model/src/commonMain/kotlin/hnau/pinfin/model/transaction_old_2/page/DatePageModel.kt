@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction_old_2.page

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class DatePageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
        val date: StateFlow<LocalDate>,
    val onDateChanged: (LocalDate) -> Unit,
) {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}