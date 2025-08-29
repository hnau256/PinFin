@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.model.transaction.utils.allRecords
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

class AmountWithDirectionModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val category: StateFlow<CategoryInfo?>,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        fun amount(): AmountModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val delegate: AmountModel.Skeleton,
        val manualDirection: MutableStateFlow<AmountDirection?>,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                delegate = AmountModel.Skeleton.createForNew(),
                manualDirection = null.toMutableStateFlowAsInitial(),
            )

            fun createForEdit(
                amount: Amount,
            ): Skeleton {
                val (direction, withoutDirection) = amount.splitToDirectionAndRaw()
                return Skeleton(
                    delegate = AmountModel.Skeleton.createForEdit(
                        amount = withoutDirection,
                    ),
                    manualDirection = direction.toMutableStateFlowAsInitial(),
                )
            }
        }
    }

    val amountModel = AmountModel(
        scope = scope,
        dependencies = dependencies.amount(),
        skeleton = skeleton.delegate,
        isFocused = isFocused,
        requestFocus = requestFocus,
    )

    fun createPage(
        scope: CoroutineScope,
    ): AmountModel.Page = amountModel.createPage(
        scope = scope,
    )

    private fun getDirectionBasedOnCategory(
        scope: CoroutineScope,
    ): StateFlow<AmountDirection?> = dependencies
        .budgetRepository
        .state
        .combineStateWith(
            scope = scope,
            other = category,
        ) { state, category ->
            state to category
        }
        .mapLatest { (state, categoryOtNull) ->
            withContext(Dispatchers.Default) {
                categoryOtNull?.let { category ->
                    state
                        .allRecords
                        .mapNotNull { (timestamp, record) ->
                            record
                                .takeIf { it.category == category }
                                ?.let { categoryRecord ->
                                    val (direction) = categoryRecord.amount.splitToDirectionAndRaw()
                                    timestamp to direction
                                }
                        }
                        .maxByOrNull(Pair<Instant, *>::first)
                        ?.second
                }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    val direction: StateFlow<AmountDirection> = skeleton.manualDirection
        .scopedInState(scope)
        .flatMapState(scope) { (scope, manualOrNull) ->
            manualOrNull.foldNullable(
                ifNotNull = AmountDirection::toMutableStateFlowAsInitial,
                ifNull = {
                    getDirectionBasedOnCategory(scope)
                        .mapState(scope) { it ?: AmountDirection.default }
                }
            )
        }

    fun switchDirection() {
        skeleton.manualDirection.value = direction.value.opposite
    }

    val amount: StateFlow<Amount?> = amountModel
        .amount
        .scopedInState(scope)
        .flatMapState(scope) { (scope, amountOrNull) ->
            amountOrNull.foldNullable(
                ifNull = { null.toMutableStateFlowAsInitial() },
                ifNotNull = { amount ->
                    direction.mapState(scope) { direction ->
                        amount.withDirection(direction)
                    }
                }
            )
        }


    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}