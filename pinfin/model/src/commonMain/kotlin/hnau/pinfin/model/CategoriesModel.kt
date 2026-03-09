@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.budgetstack.BudgetStackOpener
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers

class CategoriesModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        val budgetStackOpener: BudgetStackOpener
    }


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

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}