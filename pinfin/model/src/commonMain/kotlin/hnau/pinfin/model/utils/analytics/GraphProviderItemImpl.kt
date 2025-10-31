package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrThrow
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.lazy.AsyncLazy
import hnau.pinfin.data.Amount
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateRange

class GraphProviderItemImpl(
    override val period: LocalDateRange,
    getTransactions: (suspend () -> NonEmptyList<TransactionInfo>)?,
    config: GraphConfig,
) : GraphProvider.Item {

    override val content: GraphProvider.Item.Content? = getTransactions?.let { get ->

        val transactions = AsyncLazy { get() }

        val values = AsyncLazy {
            withContext(Dispatchers.Default) {
                val transactions = transactions.get()
                val entries = transactions
                    .flatMap(TransactionInfo::toAnalyticsEntries)
                    .let { entries ->
                        config.usedAccounts.foldNullable(
                            ifNull = { entries },
                            ifNotNull = { usedAccounts ->
                                entries.filter { entry ->
                                    entry.account in usedAccounts
                                }
                            }
                        )
                    }
                    .let { entries ->
                        config.usedCategories.foldNullable(
                            ifNull = { entries },
                            ifNotNull = { usedCategories ->
                                entries.filter { entry ->
                                    entry.category in usedCategories
                                }
                            }
                        )
                    }

                val groupedAmounts: Map<out GroupKey?, NonEmptyList<Amount>> =
                    when (config.groupBy) {
                        GraphConfig.GroupBy.Account -> entries.groupBy { entry ->
                            GroupKey.Account(entry.account)
                        }

                        GraphConfig.GroupBy.Category -> entries.groupBy { entry ->
                            GroupKey.Category(entry.category)
                        }

                        null -> mapOf(null to entries)
                    }.mapValues { (_, entries) ->
                        entries
                            .map(AnalyticsEntry::amount)
                            .toNonEmptyListOrThrow()
                    }

                groupedAmounts.mapValues { (_, amounts) ->
                    amounts
                        .fold(
                            initial = Amount.zero,
                        ) { acc, amount ->
                            when (config.operation) {
                                GraphConfig.Operation.Sum -> acc + amount
                                GraphConfig.Operation.Average -> acc + amount
                            }
                        }
                        .let { amount ->
                            when (config.operation) {
                                GraphConfig.Operation.Sum -> amount
                                GraphConfig.Operation.Average -> (amount.value.toFloat() / amounts.size)
                                    .toInt()
                                    .let(::Amount)
                            }
                        }
                }
            }
        }

        GraphProvider.Item.Content(
            getTransactions = transactions::get,
            getValues = values::get,
        )
    }
}