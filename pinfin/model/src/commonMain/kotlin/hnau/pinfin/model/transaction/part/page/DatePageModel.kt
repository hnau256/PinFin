@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.part.page

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction.utils.NavAction
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
    val navAction: NavAction,
    val date: StateFlow<LocalDate>,
    val onDateChanged: (LocalDate) -> Unit,
) : PageModel {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton : PageModel.Skeleton

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}