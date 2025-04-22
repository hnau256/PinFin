@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.BudgetsRepository
import hnau.pinfin.data.budget.BudgetRepository
import hnau.pinfin.model.budgetstack.BudgetStackModel
import hnau.pinfin.data.dto.BudgetId
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class LoadBudgetModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        val id: BudgetId,
        var mainStack: BudgetStackModel.Skeleton? = null,
    )

    @Shuffle
    interface Dependencies {

        val budgetsRepository: BudgetsRepository

        fun budgetStack(
            budgetRepository: BudgetRepository,
        ): BudgetStackModel.Dependencies

        companion object
    }

    val budgetStackModel: StateFlow<Loadable<BudgetStackModel>> = LoadableStateFlow(scope) {
        dependencies.budgetsRepository[skeleton.id]
    }
        .mapWithScope(scope) { initializedScope, infoOrLoading ->
            infoOrLoading.map { info ->
                BudgetStackModel(
                    scope = initializedScope,
                    dependencies = dependencies.budgetStack(
                        budgetRepository = info.repository,
                    ),
                    skeleton = skeleton::mainStack
                        .toAccessor()
                        .getOrInit(BudgetStackModel::Skeleton),
                )
            }
        }

    override val goBackHandler: StateFlow<(() -> Unit)?> = budgetStackModel
        .flatMapState(scope) { currentMainModel ->
            currentMainModel.fold(
                ifLoading = { NeverGoBackHandler },
                ifReady = GoBackHandlerProvider::goBackHandler,
            )
        }
}