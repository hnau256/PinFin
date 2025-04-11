@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.client.model

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.client.data.budget.BudgetRepository
import hnau.pinfin.client.data.UpdateRepository
import hnau.pinfin.client.model.mainstack.MainStackModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class InitModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Serializable
    data class Skeleton(
        var mainStack: MainStackModel.Skeleton? = null,
    )

    @Shuffle
    interface Dependencies {

        val updateRepository: UpdateRepository

        fun mainStack(
            budgetRepository: BudgetRepository,
        ): MainStackModel.Dependencies

        companion object
    }

    data class Initialized(
        val budgetRepository: BudgetRepository,
    )

    val mainStackModel: StateFlow<Loadable<MainStackModel>> = LoadableStateFlow(scope) {
        coroutineScope {
            val budgetRepository = BudgetRepository.create(
                scope = scope,
                updateRepository = dependencies.updateRepository,
            )
            Initialized(
                budgetRepository = budgetRepository,
            )
        }
    }
        .mapWithScope(scope) { initializedScope, initializedOrLoading ->
            initializedOrLoading.map { initialized ->
                MainStackModel(
                    scope = initializedScope,
                    dependencies = dependencies.mainStack(
                        budgetRepository = initialized.budgetRepository,
                    ),
                    skeleton = skeleton::mainStack
                        .toAccessor()
                        .getOrInit(MainStackModel::Skeleton),
                )
            }
        }

    override val goBackHandler: StateFlow<(() -> Unit)?> = mainStackModel
        .flatMapState(scope) { currentMainModel ->
            currentMainModel.fold(
                ifLoading = { NeverGoBackHandler },
                ifReady = GoBackHandlerProvider::goBackHandler,
            )
        }
}