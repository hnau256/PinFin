@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.budgetstack.BudgetStackOpener
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class CategoriesModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        val budgetStackOpener: BudgetStackOpener
    }

    @Serializable
    /*data*/ class Skeleton

    data class Item(
        val info: CategoryInfo,
        val onClick: () -> Unit,
    )

    val categories: StateFlow<NonEmptyList<Item>?> = dependencies
        .budgetRepository
        .state
        .mapState(scope) { state ->
            state
                .categories
                .toNonEmptyListOrNull()
                ?.map {info ->
                    Item(
                        info = info,
                        onClick = {
                            dependencies
                                .budgetStackOpener
                                .openCategory(info)
                        }
                    )
                }
        }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}