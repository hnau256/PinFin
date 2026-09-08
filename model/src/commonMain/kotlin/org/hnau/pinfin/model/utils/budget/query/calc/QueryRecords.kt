package org.hnau.pinfin.model.utils.budget.query.calc

import org.hnau.pinfin.model.utils.budget.query.MAX_RECORDS_LIMIT
import org.hnau.pinfin.model.utils.budget.query.RecordsFilter
import org.hnau.pinfin.model.utils.budget.query.RecordsPage
import org.hnau.pinfin.model.utils.budget.state.BudgetState

internal fun queryRecords(
    state: BudgetState,
    filter: RecordsFilter?,
    offset: Int,
    limit: Int,
): RecordsPage {
    val effectiveLimit = limit.coerceIn(1, MAX_RECORDS_LIMIT)
    val effectiveOffset = offset.coerceAtLeast(0)
    val filtered = state
        .flatRecords(newestFirst = true)
        .sortedByDescending { it.date }
        .filter { it.matches(filter) }
    val page = filtered
        .drop(effectiveOffset)
        .take(effectiveLimit)
    return RecordsPage(
        records = page,
        total = filtered.size,
        offset = effectiveOffset,
        limit = effectiveLimit,
        hasMore = effectiveOffset + page.size < filtered.size,
    )
}
