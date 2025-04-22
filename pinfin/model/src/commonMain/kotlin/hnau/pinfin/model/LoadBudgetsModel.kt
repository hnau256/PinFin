package hnau.pinfin.model

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.repository.BudgetsRepository
import hnau.pinfin.model.budgetsstack.BudgetsStackModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class LoadBudgetsModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsRepositoryFactory: BudgetsRepository.Factory

        fun budgetsStack(
            budgetsRepository: BudgetsRepository,
        ): BudgetsStackModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var budgetsStack: BudgetsStackModel.Skeleton? = null,
    )

    val budgetsStack: StateFlow<Loadable<BudgetsStackModel>> = LoadableStateFlow(
        scope = scope,
    ) {
        dependencies.budgetsRepositoryFactory.createBudgetsRepository(
            scope = scope,
        )
    }
        .mapWithScope(
            scope = scope,
        ) { stateScope, budgetsRepositoryOrLoading ->
            budgetsRepositoryOrLoading.map { budgetsRepository ->
                BudgetsStackModel(
                    scope = stateScope,
                    dependencies = dependencies.budgetsStack(
                        budgetsRepository = budgetsRepository,
                    ),
                    skeleton = skeleton::budgetsStack
                        .toAccessor()
                        .getOrInit { BudgetsStackModel.Skeleton() },
                )
            }
        }

    override val goBackHandler: StateFlow<(() -> Unit)?> = budgetsStack
        .flatMapState(scope) { currentMainModel ->
            currentMainModel.fold(
                ifLoading = { NeverGoBackHandler },
                ifReady = GoBackHandlerProvider::goBackHandler,
            )
        }
}