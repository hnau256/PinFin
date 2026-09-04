package org.hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptySet
import arrow.core.nonEmptySetOf
import arrow.core.toNonEmptyListOrNull
import arrow.core.toNonEmptyListOrThrow
import kotlinx.datetime.LocalDateRange
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.groupByToNonEmpty
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.AmountDirectionValues
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.div
import org.hnau.pinfin.data.sum
import org.hnau.pinfin.model.filter.Filters
import org.hnau.pinfin.model.utils.analytics.period.subperiodsOf

/**
 * Чистая функция: суммы по [groups] за один [period] страницы аналитики.
 *
 * [entries] должны быть уже отфильтрованы по [period] — функция не обращается к системным
 * часам и не знает о полном диапазоне бюджета, кроме переданного явно [totalRange]
 * (первая транзакция .. последняя транзакция), нужного только для правила «полных подпериодов»
 * при среднем (см. docs/analytics-v2-plan.md, "2.5. Расчёт").
 */
fun calcPeriod(
    entries: List<AnalyticsEntry>,
    period: LocalDateRange,
    totalRange: LocalDateRange,
    config: AnalyticsConfig,
    groups: List<GroupKey>,
    currency: Currency,
): PeriodResult {
    val groupAmounts = groups.mapNotNull { group ->
        val constraints = group.constraints(config) ?: return@mapNotNull null
        val (direction, amount) = calcGroupAmount(
            operation = config.operation,
            constraints = constraints,
            entries = entries,
            period = period,
            totalRange = totalRange,
            currency = currency,
        )
        amount
            .takeIf { it != Amount.zero }
            ?: return@mapNotNull null

        GroupAmount(
            group = group,
            constraints = constraints,
            direction = direction,
            amount = amount,
        )
    }

    val valuesByDirection = groupAmounts
        .groupByToNonEmpty(GroupAmount::direction)
        .mapValues { (_, items) ->
            items
                .map { item ->
                    KeyValue(
                        key = item.group,
                        value = PeriodResult.Half.Value(
                            amount = item.amount,
                            filters = Filters(
                                categories = item.constraints.categories,
                                accounts = item.constraints.accounts,
                                period = period,
                            ),
                        ),
                    )
                }
                .sortedByDescending { item -> item.value.amount }
                .toNonEmptyListOrThrow()
        }

    return PeriodResult(
        values = AmountDirectionValues.create { direction ->
            valuesByDirection[direction]?.let(PeriodResult::Half)
        },
    )
}

private data class GroupAmount(
    val group: GroupKey,
    val constraints: EntryConstraints,
    val direction: AmountDirection,
    val amount: Amount,
)

private data class EntryConstraints(
    val categories: NonEmptySet<CategoryId?>?,
    val accounts: NonEmptySet<AccountId>?,
)

/**
 * Ограничения по счетам/категориям для одной группы: пересечение собственного ключа группы
 * с глобальным ограничением [AnalyticsConfig.accounts]/[AnalyticsConfig.categories] (фаза 4,
 * docs/analytics-v2-plan.md, "Фаза 4"). `null` - группа целиком исключена ограничением
 * (например, группировка по категориям, а сама категория не входит в разрешённый набор).
 */
private fun GroupKey.constraints(
    config: AnalyticsConfig,
): EntryConstraints? = fold(
    ifNone = {
        EntryConstraints(
            categories = config.categories,
            accounts = config.accounts,
        )
    },
    ifCategory = { idWithCategory ->
        val categoryId = idWithCategory?.key
        val allowed = config.categories?.let { categoryId in it } ?: true
        allowed.foldBoolean(
            ifTrue = {
                EntryConstraints(
                    categories = nonEmptySetOf(categoryId),
                    accounts = config.accounts,
                )
            },
            ifFalse = { null },
        )
    },
    ifAccount = { idWithAccount ->
        val allowed = config.accounts?.let { idWithAccount.key in it } ?: true
        allowed.foldBoolean(
            ifTrue = {
                EntryConstraints(
                    categories = config.categories,
                    accounts = nonEmptySetOf(idWithAccount.key),
                )
            },
            ifFalse = { null },
        )
    },
)

private fun AnalyticsEntry.matches(
    constraints: EntryConstraints,
): Boolean {
    val categories = constraints.categories
    if (categories != null && idWithCategoryOrDirection.getOrNull()?.key !in categories) {
        return false
    }

    val accounts = constraints.accounts
    if (accounts != null && idWithAccount.key !in accounts) {
        return false
    }
    return true
}

private fun calcGroupAmount(
    operation: AnalyticsConfig.Operation,
    constraints: EntryConstraints,
    entries: List<AnalyticsEntry>,
    period: LocalDateRange,
    totalRange: LocalDateRange,
    currency: Currency,
): KeyValue<AmountDirection, Amount> = operation.fold(
    ifSum = {
        calcSubperiodAmount(
            subperiod = period,
            constraints = constraints,
            entries = entries,
        )
    },
    ifAverage = { subperiod ->
        val subperiods = subperiod.subperiodsOf(period)
        // "Полный" подпериод — целиком внутри периода страницы И внутри диапазона данных.
        val isFull = { subperiodRange: LocalDateRange ->
            subperiodRange.endInclusive <= period.endInclusive &&
                subperiodRange.start >= totalRange.start &&
                subperiodRange.endInclusive <= totalRange.endInclusive
        }
        val fullSubperiods = subperiods.filter(isFull)
        // Если полных подпериодов нет (период короче подпериода или данных меньше одного
        // подпериода) — считаем по всем пересекающимся подпериодам (fallback, вариант (б)).
        val (summedSubperiods, divisor) = fullSubperiods
            .takeIf { it.isNotEmpty() }
            ?.let { it to it.size }
            ?: (subperiods to subperiods.size)

        summedSubperiods
            .map { subperiodRange ->
                calcSubperiodAmount(
                    subperiod = subperiodRange,
                    constraints = constraints,
                    entries = entries,
                )
            }
            .toNonEmptyListOrThrow()
            .sum()
            .map { sum ->
                sum.div(
                    divisor = divisor,
                    scale = currency.scale,
                )
            }
    },
)

private fun calcSubperiodAmount(
    subperiod: LocalDateRange,
    constraints: EntryConstraints,
    entries: List<AnalyticsEntry>,
): KeyValue<AmountDirection, Amount> = entries
    .filter { entry -> entry.date in subperiod }
    .filter { entry -> entry.matches(constraints) }
    .toNonEmptyListOrNull()
    .foldNullable(
        ifNull = {
            KeyValue(
                AmountDirection.Credit,
                Amount.zero,
            )
        },
        ifNotNull = { matchingEntries ->
            matchingEntries
                .map(AnalyticsEntry::directionedAmount)
                .sum()
        },
    )
