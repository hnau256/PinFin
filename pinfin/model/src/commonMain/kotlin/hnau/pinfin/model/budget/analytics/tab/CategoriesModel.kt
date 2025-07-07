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
import hnau.pinfin.data.CategoryDirection
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
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

    class State private constructor(
        val categories: List<Pair<CategoryInfo, Amount>>,
        val maxCategoryCredit: Amount,
        val minCategoryDebit: Amount,
        val creditSum: Amount,
        val debitSum: Amount,
    ) {

        val total: SignedAmount = SignedAmount(
            value = creditSum.value.toLong() - debitSum.value.toLong()
        )

        override fun equals(
            other: Any?,
        ): Boolean = (other as? State)
            ?.categories
            ?.takeIf { it == categories } != null

        override fun hashCode(): Int = categories.hashCode()

        companion object {

            fun create(
                amounts: Map<CategoryInfo, Amount>,
            ): State {
                val credits = amounts
                    .filter { it.key.id.direction == CategoryDirection.Credit }
                    .values
                val debits = amounts
                    .filter { it.key.id.direction == CategoryDirection.Debit }
                    .values
                return State(
                    categories = amounts
                        .toList()
                        .sortedBy { it.first.id },
                    maxCategoryCredit = credits.maxOrNull() ?: Amount.zero,
                    minCategoryDebit = debits.minOrNull() ?: Amount.zero,
                    creditSum = credits.fold(Amount.zero, Amount::plus),
                    debitSum = debits.fold(Amount.zero, Amount::plus),
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