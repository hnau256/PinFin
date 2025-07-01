@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.analytics.tab

import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.goback.NeverGoBackHandler
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class CategoriesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val budgetsRepository: BudgetRepository
    }

    @Serializable
    /*data*/ class Skeleton

    val categories: StateFlow<List<CategoryInfo>> = dependencies
        .budgetsRepository
        .categories
        .list

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}