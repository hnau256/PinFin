package hnau.pinfin.model

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.repository.BudgetRepository
import hnau.pinfin.model.budgetstack.BudgetStackModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class LoadBudgetModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val deferredBudgetRepository: Deferred<BudgetRepository>

        fun budget(
            budgetRepository: BudgetRepository,
        ): BudgetStackModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var budget: BudgetStackModel.Skeleton? = null,
    )

    val budget: StateFlow<Loadable<BudgetStackModel>> = LoadableStateFlow(scope) {
        dependencies
            .deferredBudgetRepository
            .await()
    }.mapWithScope(scope) { budgetScope, budgetRepositoryOrLoading ->
        budgetRepositoryOrLoading.map { budgetRepository ->
            BudgetStackModel(
                scope = budgetScope,
                dependencies = dependencies.budget(
                    budgetRepository = budgetRepository,
                ),
                skeleton = skeleton::budget
                    .toAccessor()
                    .getOrInit { BudgetStackModel.Skeleton() },
            )
        }
    }

    override val goBackHandler: GoBackHandler = budget.flatMapState(scope) { budgetModelOrLoading ->
        budgetModelOrLoading.fold(
            ifLoading = { NeverGoBackHandler },
            ifReady = GoBackHandlerProvider::goBackHandler
        )
    }
}