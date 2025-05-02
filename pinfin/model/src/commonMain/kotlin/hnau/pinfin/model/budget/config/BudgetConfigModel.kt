package hnau.pinfin.model.budget.config

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.fold
import hnau.pinfin.model.manage.BudgetsListOpener
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class BudgetConfigModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsListOpener: BudgetsListOpener

        val repository: BudgetRepository

        fun editName(): BudgetEditNameModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val editName: MutableStateFlow<BudgetEditNameModel.Skeleton?> =
            null.toMutableStateFlowAsInitial(),
    )

    private val inProgressRegistry = InProgressRegistry()

    //TODO
    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.isProgress

    fun openBudgetsList() {
        scope.launch {
            inProgressRegistry.executeRegistered {
                dependencies.budgetsListOpener.openBudgetsList()
            }
        }
    }

    val nameOrEdit: StateFlow<Either<Pair<String, () -> Unit>, BudgetEditNameModel>> = skeleton
        .editName
        .scopedInState(scope)
        .flatMapState(scope) { (stateScope, editNameSkeletonOrNull) ->
            editNameSkeletonOrNull.fold(
                ifNull = {
                    dependencies
                        .repository
                        .state
                        .mapState(stateScope) {
                            val name = it.info.title
                            val edit = {
                                skeleton.editName.value = BudgetEditNameModel.Skeleton(
                                    info = dependencies.repository.state.value.info,
                                )
                            }
                            (name to edit).left()
                        }

                },
                ifNotNull = { editNameSkeleton ->
                    BudgetEditNameModel(
                        scope = stateScope,
                        skeleton = editNameSkeleton,
                        dependencies = dependencies.editName(),
                        onDone = { skeleton.editName.value = null },
                    )
                        .right()
                        .toMutableStateFlowAsInitial()
                }
            )
        }

    override val goBackHandler: GoBackHandler = nameOrEdit.flatMapState(scope) { nameOrEditModel ->
        when (nameOrEditModel) {
            is Either.Left -> null.toMutableStateFlowAsInitial()
            is Either.Right -> nameOrEditModel.value.goBackHandler
        }
    }
}