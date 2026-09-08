package org.hnau.pinfin.model.utils.budget.query.calc

import kotlinx.datetime.LocalDateRange
import org.hnau.pinfin.data.sum
import org.hnau.pinfin.data.utils.DecimalScale
import org.hnau.pinfin.model.utils.analytics.period.PeriodDuration
import org.hnau.pinfin.model.utils.analytics.period.periods
import org.hnau.pinfin.model.utils.analytics.period.subperiodsOf
import org.hnau.pinfin.model.utils.budget.query.Aggregation
import org.hnau.pinfin.model.utils.budget.query.AnalyticsRow
import org.hnau.pinfin.model.utils.budget.query.AnalyticsTable
import org.hnau.pinfin.model.utils.budget.query.FlatRecord
import org.hnau.pinfin.model.utils.budget.query.GroupBy
import org.hnau.pinfin.model.utils.budget.query.PeriodSpec
import org.hnau.pinfin.model.utils.budget.query.RecordDirection
import org.hnau.pinfin.model.utils.budget.query.RecordsFilter
import org.hnau.pinfin.model.utils.budget.query.Totals
import org.hnau.pinfin.model.utils.budget.query.fold
import org.hnau.pinfin.model.utils.budget.state.BudgetState

internal const val TRANSFER_GROUP_KEY: String = "transfer"

internal fun calcAnalyticsTable(
    state: BudgetState,
    filter: RecordsFilter?,
    period: PeriodSpec,
    groupBy: GroupBy?,
    aggregation: Aggregation,
): AnalyticsTable {
    val scale = state.info.currency.scale
    val records = state.flatRecords().filter { it.matches(filter) }
    if (records.isEmpty()) {
        return AnalyticsTable.empty(aggregation = aggregation, groupBy = groupBy)
    }

    val rangeStart = filter?.dateMin ?: records.minOf { it.date }
    val rangeEnd = filter?.dateMax ?: records.maxOf { it.date }
    val range = rangeStart..rangeEnd

    val rows = period.toAnalyticsPeriod().periods(range)

    val periodRows: List<AnalyticsRow> = aggregation.fold(
        ifSum = { incremental ->
            if (incremental) {
                calcIncrementalRows(records = records, rows = rows, range = range, groupBy = groupBy)
            } else {
                calcSumRows(records = records, rows = rows, range = range, groupBy = groupBy)
            }
        },
        ifAverage = { subperiod ->
            val duration = subperiod.toPeriodDuration()
            rows.map { row ->
                val (totals, groups) = averageTotalsAndGroups(
                    records = records,
                    periodRow = row,
                    dataRange = range,
                    groupBy = groupBy,
                    subperiod = duration,
                    scale = scale,
                )
                AnalyticsRow.create(range = row, partial = isPartial(row, range), groups = groups, totals = totals)
            }
        },
    )

    val tableTotals = aggregation.fold(
        ifSum = { totalsOf(records) },
        ifAverage = { subperiod ->
            averageTotalsAndGroups(
                records = records,
                periodRow = range,
                dataRange = range,
                groupBy = null,
                subperiod = subperiod.toPeriodDuration(),
                scale = scale,
            ).first
        },
    )

    return AnalyticsTable.create(
        range = range,
        groupBy = groupBy,
        aggregation = aggregation,
        periods = periodRows,
        totals = tableTotals,
    )
}

private fun calcSumRows(
    records: List<FlatRecord>,
    rows: List<LocalDateRange>,
    range: LocalDateRange,
    groupBy: GroupBy?,
): List<AnalyticsRow> = rows.map { row ->
    val rowRecords = records.filter { it.date in row }
    AnalyticsRow.create(
        range = row,
        partial = isPartial(row, range),
        groups = computeGroups(rowRecords, groupBy),
        totals = totalsOf(rowRecords),
    )
}

private fun calcIncrementalRows(
    records: List<FlatRecord>,
    rows: List<LocalDateRange>,
    range: LocalDateRange,
    groupBy: GroupBy?,
): List<AnalyticsRow> {
    var accTotals = Totals.zero
    val accGroups = mutableMapOf<String, Totals>()
    return rows.map { row ->
        val rowRecords = records.filter { it.date in row }
        accTotals += totalsOf(rowRecords)
        val groupsSnapshot = groupBy?.let { notNullGroupBy ->
            rowRecords.groupBy { record -> record.groupKey(notNullGroupBy) }.forEach { (key, rs) ->
                accGroups[key] = (accGroups[key] ?: Totals.zero) + totalsOf(rs)
            }
            accGroups.filterValues { totals -> !totals.isZero }.toMap()
        }
        AnalyticsRow.create(
            range = row,
            partial = isPartial(row, range),
            groups = groupsSnapshot,
            totals = accTotals,
        )
    }
}

private fun averageTotalsAndGroups(
    records: List<FlatRecord>,
    periodRow: LocalDateRange,
    dataRange: LocalDateRange,
    groupBy: GroupBy?,
    subperiod: PeriodDuration,
    scale: DecimalScale,
): Pair<Totals, Map<String, Totals>?> {
    val subperiods = subperiod.subperiodsOf(periodRow)
    val isFull = { sp: LocalDateRange ->
        sp.endInclusive <= periodRow.endInclusive &&
            sp.start >= dataRange.start &&
            sp.endInclusive <= dataRange.endInclusive
    }
    val fullSubperiods = subperiods.filter(isFull)
    val (usedSubperiods, divisor) = fullSubperiods
        .takeIf { it.isNotEmpty() }
        ?.let { it to it.size }
        ?: (subperiods to subperiods.size)

    val rowRecords = records.filter { it.date in periodRow }
    val usedRecords = rowRecords.filter { record -> usedSubperiods.any { record.date in it } }

    val totals = totalsOf(usedRecords).div(divisor, scale)
    val groups = computeGroups(usedRecords, groupBy)
        ?.mapValues { (_, totals) -> totals.div(divisor, scale) }
        ?.filterValues { !it.isZero }
    return totals to groups
}

private fun isPartial(
    row: LocalDateRange,
    range: LocalDateRange,
): Boolean = row.start < range.start || row.endInclusive > range.endInclusive

private fun FlatRecord.groupKey(
    groupBy: GroupBy,
): String = groupBy.fold(
    ifCategory = { category?.id ?: TRANSFER_GROUP_KEY },
    ifAccount = { account.id },
)

private fun computeGroups(
    records: List<FlatRecord>,
    groupBy: GroupBy?,
): Map<String, Totals>? {
    groupBy ?: return null
    return records
        .groupBy { it.groupKey(groupBy) }
        .mapValues { (_, rs) -> totalsOf(rs) }
        .filterValues { !it.isZero }
}

private fun totalsOf(
    records: List<FlatRecord>,
): Totals {
    val (debitRecords, creditRecords) = records.partition { it.direction == RecordDirection.Debit }
    return Totals(
        sumDebit = debitRecords.map { it.amount }.sum(),
        sumCredit = creditRecords.map { it.amount }.sum(),
    )
}
