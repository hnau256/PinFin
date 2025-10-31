package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateRange

class GraphProviderImpl(
    private val config: GraphConfig
) : GraphProvider {

    private val accessCacheMutex = Mutex()

    private var cache: Pair<UpchainHash?, NonEmptyList<GraphProvider.Item>?>? = null

    override suspend fun getItems(
        budgetState: BudgetState,
    ): NonEmptyList<GraphProvider.Item>? = accessCacheMutex.withLock {
        val hash = budgetState.hash
        cache
            ?.takeIf { it.first == hash }
            ?.foldNullable(
                ifNotNull = { (_, items) -> items },
                ifNull = {
                    calcItems(budgetState)
                        .also { items -> cache = hash to items }
                }
            )
    }

    private suspend fun calcItems(
        budgetState: BudgetState,
    ): NonEmptyList<GraphProvider.Item>? = withContext(Dispatchers.Default) {
        budgetState
            .transactions
            .toNonEmptyListOrNull()
            ?.splitTransactionsToPeriods(config.period)
            ?.map { (period, transactions) ->
                GraphProviderItemImpl(
                    period = period,
                    config = config,
                    getTransactions = transactions
                        .toNonEmptyListOrNull()
                        ?.let { nonEmptyTransactions ->
                            { nonEmptyTransactions }
                        },
                )
            }
    }

    companion object {

        private fun NonEmptyList<TransactionInfo>.splitTransactionsToPeriods(
            period: GraphConfig.Period,
        ): NonEmptyList<Pair<LocalDateRange, List<TransactionInfo>>>? = when (period) {
            GraphConfig.Period.Inclusive -> {
                val firstTransactionDate = first().date
                val lastTransactionDate = last().date
                nonEmptyListOf((firstTransactionDate..lastTransactionDate) to this)
            }

            is GraphConfig.Period.Fixed -> splitToPeriods(
                customStartOfOneOfPeriods = period.startOfOneOfPeriods,
                duration = period.duration,
                extractDate = TransactionInfo::date,
            )
        }
    }
}