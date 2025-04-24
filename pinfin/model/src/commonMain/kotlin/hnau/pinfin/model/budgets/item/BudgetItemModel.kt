package hnau.pinfin.model.budgets.item

import arrow.core.Either
import hnau.pinfin.data.dto.BudgetId
import hnau.pinfin.data.repository.BudgetRepository
import hnau.pinfin.data.repository.BudgetState
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
import kotlinx.serialization.Serializable

class BudgetItemModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val onClick: () -> Unit,
) {

    @Shuffle
    interface Dependencies {

        val id: BudgetId

        val deferredRepository: Deferred<BudgetRepository>
    }

    @Serializable
    /*data*/ class Skeleton

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