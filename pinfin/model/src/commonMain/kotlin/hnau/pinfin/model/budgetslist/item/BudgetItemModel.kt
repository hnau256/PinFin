package hnau.pinfin.model.budgetslist.item

import arrow.core.Either
import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.manage.BudgetOpener
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class BudgetItemModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Shuffle
    interface Dependencies {

        val id: BudgetId

        val deferredRepository: Deferred<BudgetRepository>

        val budgetOpener: BudgetOpener
    }

    @Serializable
    /*data*/ class Skeleton

    private val inProgressRegistry = InProgressRegistry()

    //TODO
    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.isProgress

    fun open() {
        scope.launch {
            inProgressRegistry.executeRegistered {
                dependencies.budgetOpener.openBudget(id)
            }
        }
    }

    //TODO remove use info
    val id: BudgetId
        get() = dependencies.id

    @OptIn(ExperimentalCoroutinesApi::class)
    val info: StateFlow<Either<BudgetId, BudgetState>> = flow {
        val repository = dependencies.deferredRepository.await()
        emit(repository)
    }
        .flatMapLatest { repository -> repository.state }
        .map { Either.Right(it) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = Either.Left(dependencies.id),
        )
}