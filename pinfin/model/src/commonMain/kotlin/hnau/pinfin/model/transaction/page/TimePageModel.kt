@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.page

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction.utils.NavAction
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TimePageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    val navAction: NavAction,
    val time: StateFlow<LocalTime>,
    val onTimeChanged: (LocalTime) -> Unit,
) : PageModel {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}