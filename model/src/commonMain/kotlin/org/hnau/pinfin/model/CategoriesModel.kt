@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.model.budgetstack.BudgetStackOpener
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

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
        val idWithCategory: KeyValue<CategoryId, CategoryInfo>,
        val onClick: () -> Unit,
    )

    val categories: StateFlow<NonEmptyList<Item>?> = dependencies
        .budgetRepository
        .state
        .mapState(scope) { state ->
            state
                .categories
                .toNonEmptyListOrNull()
                ?.map { idWithCategory ->
                    Item(
                        idWithCategory = idWithCategory,
                        onClick = {
                            dependencies
                                .budgetStackOpener
                                .openCategory(
                                    id = idWithCategory.key,
                                    info = idWithCategory.value,
                                )
                        }
                    )
                }
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}