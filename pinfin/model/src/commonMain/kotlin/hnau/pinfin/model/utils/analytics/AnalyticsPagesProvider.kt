package hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.nonEmptySetOf
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.map
import hnau.pinfin.model.utils.analytics.config.AnalyticsSplitConfig
import hnau.pinfin.model.utils.budget.state.BudgetState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class AnalyticsPagesProvider(
    private val config: AnalyticsSplitConfig,
) {

    suspend fun generatePages(
        state: BudgetState,
        today: LocalDate,
    ): NonEmptyList<AnalyticsPage> = withContext(Dispatchers.Default) {
        calcTotalRange(
            state = state,
            today = today,
        )
            .let { range ->
                when (config.period) {
                    AnalyticsSplitConfig.Period.Inclusive -> nonEmptyListOf(range)
                    is AnalyticsSplitConfig.Period.Fixed -> range.splitToPeriods(
                        duration = config.period.duration,
                        startOfOneOfPeriods = config.period.startOfOneOfPeriods,
                    )
                }

            }
            .let { periods ->
                periods.map { period ->
                    AnalyticsPage(
                        period = period,
                        items = when (config.groupBy) {
                            AnalyticsSplitConfig.GroupBy.Account -> {
                                val allAccounts = state.accounts
                                config
                                    .usedAccounts
                                    .foldNullable(
                                        ifNull = { allAccounts },
                                        ifNotNull = { usedAccounts ->
                                            allAccounts.filter { account ->
                                                account.id in usedAccounts
                                            }
                                        }
                                    )
                                    .map { account ->
                                        AnalyticsPage.Item(
                                            key = AnalyticsPage.Item.Key.Account(account),
                                            constraints = AnalyticsPage.Item.Constraints(
                                                categories = config.usedCategories,
                                                accounts = nonEmptySetOf(account.id),
                                            ),
                                        )
                                    }
                            }

                            AnalyticsSplitConfig.GroupBy.Category -> {
                                val allCategoriesWithNull = state.categories + null
                                config
                                    .usedCategories
                                    .foldNullable(
                                        ifNull = { allCategoriesWithNull },
                                        ifNotNull = { usedCategories ->
                                            allCategoriesWithNull.filter { categoryOrNull ->
                                                categoryOrNull?.id in usedCategories
                                            }
                                        }
                                    )
                                    .map { categoryOrNull ->
                                        AnalyticsPage.Item(
                                            key = AnalyticsPage.Item.Key.Category(categoryOrNull),
                                            constraints = AnalyticsPage.Item.Constraints(
                                                categories = nonEmptySetOf(categoryOrNull?.id),
                                                accounts = config.usedAccounts,
                                            )
                                        )
                                    }
                            }

                            null -> nonEmptyListOf(
                                AnalyticsPage.Item(
                                    key = null,
                                    constraints = AnalyticsPage.Item.Constraints(
                                        categories = config.usedCategories,
                                        accounts = config.usedAccounts,
                                    )
                                )
                            )
                        },
                    )
                }
            }
    }

    private fun calcTotalRange(
        state: BudgetState,
        today: LocalDate,
    ): LocalDateRange = state
        .transactions
        .toNonEmptyListOrNull()
        .foldNullable(
            ifNull = { today..today },
            ifNotNull = { transactions ->
                (transactions.first() to transactions.last())
                    .map { transaction ->
                        transaction
                            .timestamp
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                    }
                    .let { (minFromTransactions, maxFromTransactions) ->
                        val min = minOf(minFromTransactions, today)
                        val max = maxOf(maxFromTransactions, today)
                        min..max
                    }
            }
        )
}