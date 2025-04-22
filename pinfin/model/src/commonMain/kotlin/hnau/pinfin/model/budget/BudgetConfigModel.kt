package hnau.pinfin.model.budget

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class BudgetConfigModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
): GoBackHandlerProvider {

    @Shuffle
    interface Dependencies

    @Serializable
    data class Skeleton(
        val a: Int = 0,
    )
}