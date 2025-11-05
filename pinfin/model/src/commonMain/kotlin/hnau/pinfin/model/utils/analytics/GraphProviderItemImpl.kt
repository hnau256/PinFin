package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import arrow.core.toNonEmptyListOrThrow
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.lazy.AsyncLazy
import hnau.pinfin.data.Amount
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlinx.datetime.LocalDateRange

class GraphProviderItemImpl(
    override val period: LocalDateRange,
    getTransactions: (suspend () -> NonEmptyList<TransactionInfo>)?,
    config: GraphConfig,
) : GraphProvider.Item {

    private val content: AsyncLazy<GraphProvider.Item.Content?> = AsyncLazy {

        val transactions = getTransactions?.invoke() ?: return@AsyncLazy null

        val entries = transactions
            .flatMap(TransactionInfo::toAnalyticsEntries)
            .let { entries ->
                config.usedAccounts.foldNullable(
                    ifNull = { entries },
                    ifNotNull = { usedAccounts ->
                        entries.filter { entry ->
                            entry.account.id in usedAccounts
                        }
                    }
                )
            }
            .let { entries ->
                config.usedCategories.foldNullable(
                    ifNull = { entries },
                    ifNotNull = { usedCategories ->
                        entries.filter { entry ->
                            entry.category?.id in usedCategories
                        }
                    }
                )
            }
            .toNonEmptyListOrNull()
            ?: return@AsyncLazy null

        val groupedAmounts: NonEmptyList<Pair<GroupKey?, Pair<NonEmptyList<TransactionInfo>, NonEmptyList<Amount>>>> =
            when (config.groupBy) {
                GraphConfig.GroupBy.Account -> entries
                    .groupBy { entry -> GroupKey.Account(entry.account) }
                    .mapValues { (_, entries) ->
                        entries.toNonEmptyListOrThrow()
                    }

                GraphConfig.GroupBy.Category -> entries
                    .groupBy { entry -> GroupKey.Category(entry.category) }
                    .mapValues { (_, entries) ->
                        entries.toNonEmptyListOrThrow()
                    }

                null -> mapOf(null to entries)
            }
                .toList()
                .sortedBy { (key) ->
                    when (key) {
                        is GroupKey.Account -> key.account.id.id
                        is GroupKey.Category -> key.category?.id?.id.orEmpty()
                        null -> ""
                    }
                }
                .toNonEmptyListOrThrow()
                .map { (key, entries) ->
                    val amounts = entries
                        .toNonEmptyListOrThrow()
                        .let { nonEmptyEntries ->
                            val transactions = nonEmptyEntries
                                .map(AnalyticsEntry::transaction)
                                .distinctBy(TransactionInfo::id)
                            val amounts = nonEmptyEntries.map(AnalyticsEntry::amount)
                            transactions to amounts
                        }
                    key to amounts
                }

        val values = groupedAmounts
            .mapNotNull { (key, transactionsWithAmounts) ->

                val (transactions, amounts) = transactionsWithAmounts

                val nonZeroAmount = amounts
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
                    .takeIf { it != Amount.zero }
                    ?: return@mapNotNull null

                GraphProvider.Item.Content.Value(
                    key = key,
                    transactions = transactions,
                    amount = nonZeroAmount,
                )
            }
            .toNonEmptyListOrNull()
            ?: return@AsyncLazy null

        GraphProvider.Item.Content(
            values = values,
        )
    }

    override suspend fun getContent(): GraphProvider.Item.Content? =
        content.get()
}