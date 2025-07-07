@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.analytics.tab

import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.goback.NeverGoBackHandler
import hnau.pinfin.data.Amount
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.data.CategoryDirectionValues
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.SignedAmount
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class CategoriesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val budgetsRepository: BudgetRepository
    }

    @Serializable
    /*data*/ class Skeleton

    data class State(
        val directions: CategoryDirectionValues<State.Direction>,
    ) {

        val sum: SignedAmount = SignedAmount(
            value = directions.credit.sum.value.toLong() -
                    directions.debit.sum.value.toLong()
        )

        data class Direction private constructor(
            val items: List<Item>,
            val sum: Amount,
        ) {

            data class Item(
                val info: CategoryInfo,
                val amount: SignedAmount,
                val fraction: Float,
            )

            companion object {

                fun create(
                    amounts: Map<CategoryInfo, Amount>,
                    direction: AmountDirection,
                ): Direction {
                    val items = amounts
                        .entries
                        .filter { it.key.id.direction == direction }
                        .map { it.key to it.value }
                    val maxOrNull = items
                        .maxOfOrNull { it.second.value.toLong() }
                        ?.toFloat()
                    return Direction(
                        items = items
                            .map { (info, amount) ->
                                Item(
                                    info = info,
                                    amount = SignedAmount(
                                        amount = amount,
                                        direction = direction,

                                        ),
                                    fraction = maxOrNull
                                        ?.let { sum -> amount.value.toLong() / sum }
                                        ?: 0f,
                                )
                            }
                            .sortedByDescending { it.amount.amount.value },
                        sum = items
                            .fold(
                                initial = Amount.zero,
                            ) { acc, infoWithAmount ->
                                acc + infoWithAmount.second
                            },
                    )
                }
            }
        }
    }

    val state: StateFlow<Loadable<State?>> = dependencies
        .budgetsRepository
        .state
        .map { state ->
            val thisMonthStart = Clock
                .System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .let { now ->
                    LocalDate(
                        year = now.year,
                        month = now.month,
                        dayOfMonth = 1,
                    )
                }
                .atTime(
                    hour = 0,
                    minute = 0,
                )
            withContext(Dispatchers.Default) {
                val amounts: MutableMap<CategoryInfo, Amount> = mutableMapOf()
                state
                    .transactions
                    .filter { transaction ->
                        val timestamp = transaction
                            .timestamp
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        timestamp >= thisMonthStart
                    }
                    .forEach { transaction ->
                        when (val type = transaction.type) {
                            is TransactionInfo.Type.Transfer -> Unit
                            is TransactionInfo.Type.Entry -> type.records.forEach { record ->
                                val category = record.category
                                val current = amounts.getOrPut(category) { Amount.zero }
                                amounts[category] = current + record.amount
                            }
                        }
                    }
                if (amounts.isEmpty()) {
                    return@withContext null
                }
                State(
                    directions = CategoryDirectionValues.create { direction ->
                        State.Direction.create(
                            direction = direction,
                            amounts = amounts,
                        )
                    }
                )
            }.let(::Ready)
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = Loading,
        )

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}