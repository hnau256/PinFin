@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.analytics.tab

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Amount
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.UseSerializers
import kotlin.math.absoluteValue
import kotlin.time.Clock

class CategoriesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val budgetsRepository: BudgetRepository
    }


    data class State private constructor(
        val items: List<Item>,
        val sum: Amount,
    ) {

        data class Item(
            val info: CategoryInfo,
            val amount: Amount,
            val fraction: Float,
        )

        companion object {

            fun create(
                amounts: Map<CategoryInfo, Amount>,
            ): State {
                val items = amounts
                    .entries
                    .map { it.key to it.value }
                val maxOrNull = items
                    .maxOfOrNull { it.second.value.toLong().absoluteValue }
                    ?.toFloat()
                return State(
                    items = items
                        .map { (info, amount) ->
                            Item(
                                info = info,
                                amount = amount,
                                fraction = maxOrNull
                                    ?.let { sum -> amount.value.toLong().absoluteValue / sum }
                                    ?: 0f,
                            )
                        }
                        .sortedBy { item ->
                            item.amount.value.let { value ->
                                when {
                                    value > 0 -> value
                                    else -> value - Int.MIN_VALUE
                                }
                            }
                        },
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
                State.create(
                    amounts = amounts,
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