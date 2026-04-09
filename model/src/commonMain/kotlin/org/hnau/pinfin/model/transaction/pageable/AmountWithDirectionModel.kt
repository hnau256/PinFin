@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
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
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.commons.app.model.utils.Editable
import org.hnau.pinfin.model.transaction.utils.allRecords
import org.hnau.commons.app.model.utils.combineEditableWith
import org.hnau.commons.app.model.utils.valueOrNone
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import kotlin.time.Instant

class AmountWithDirectionModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val category: StateFlow<CategoryInfo?>,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    val goForward: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val currency: Currency

        val budgetRepository: BudgetRepository

        fun amount(): AmountModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val delegate: AmountModel.Skeleton,
        val initialDirection: AmountDirection?,
        val manualDirection: MutableStateFlow<AmountDirection?> =
            initialDirection.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                delegate = AmountModel.Skeleton.createForNew(),
                initialDirection = null,
            )

            fun createForEdit(
                amount: AmountExpression,
            ): Skeleton {
                val (direction, withoutDirection) = amount.splitToDirectionAndRaw()
                return Skeleton(
                    delegate = AmountModel.Skeleton.createForEdit(
                        amount = withoutDirection,
                    ),
                    initialDirection = direction,
                )
            }
        }
    }

    val amountModel = AmountModel(
        scope = scope,
        skeleton = skeleton.delegate,
        isFocused = isFocused,
        requestFocus = requestFocus,
        goForward = goForward,
        dependencies = dependencies.amount(),
    )

    fun createPage(): AmountModel.Page =
        amountModel.createPage()

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
                                    val (direction) = categoryRecord
                                        .amount
                                        .toAmount(dependencies.currency.scale)
                                        .splitToDirectionAndRaw()
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
        .flatMapWithScope(scope) { scope, manualOrNull ->
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

    internal val amountEditable: StateFlow<Editable<AmountExpression>> = amountModel
        .amountEditable
        .combineEditableWith(
            scope = scope,
            other = Editable.Value.create(
                scope = scope,
                value = direction,
                initialValueOrNone = skeleton.initialDirection.toOption(),
            )
        ) { amount, direction ->
            amount.withDirection(direction)
        }

    val amount: StateFlow<AmountExpression?> = amountEditable
        .mapState(scope) { it.valueOrNone.getOrNull() }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}