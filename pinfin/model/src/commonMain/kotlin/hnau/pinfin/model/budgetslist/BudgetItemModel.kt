package hnau.pinfin.model.budgetslist

import hnau.common.kotlin.LoadableStateFlow
import hnau.pinfin.data.BudgetsRepository
import hnau.pinfin.data.dto.BudgetId
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class BudgetItemModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val onClick: () -> Unit,
) {

    @Shuffle
    interface Dependencies {

        val budgetsRepository: BudgetsRepository
    }

    @Serializable
    data class Skeleton(
        val id: BudgetId,
    )

    val id: BudgetId
        get() = skeleton.id

    val info = LoadableStateFlow(
        scope = scope,
    ) {
        dependencies
            .budgetsRepository[skeleton.id]
            .repository
    }
}