package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import arrow.core.toNonEmptyListOrThrow
import hnau.common.kotlin.foldNullable
import hnau.pinfin.data.Amount
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlinx.datetime.LocalDateRange

fun a(
    budgetState: BudgetState,
    config: GraphConfig,
) {

     val a = budgetState
        .transactions
        .toNonEmptyListOrNull()
        ?.splitTransactionsToPeriods(config.period)
        ?.map { (period, transactions) ->
            transactions
                .toNonEmptyListOrNull()
                ?.splitToPeriods(
                    customStartOfOneOfPeriods = null,
                    duration = config.subPeriod,
                    extractDate = TransactionInfo::date,
                )
                ?.mapNotNull { (_, transactions) ->
                    transactions.toNonEmptyListOrNull()
                }
                ?.map { subTransactions ->
                    val entries = subTransactions
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
                                        entry.category == null || entry.category in usedCategories
                                    }
                                }
                            )
                        }

                    val grouped: Map<out GroupKey?, NonEmptyList<Amount>> = when (config.groupBy) {
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

                     grouped.mapValues { (_, amounts) ->
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
}

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