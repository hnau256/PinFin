package org.hnau.pinfin.model.utils.budget.query.calc

import org.hnau.pinfin.model.utils.budget.query.FlatRecord
import org.hnau.pinfin.model.utils.budget.query.RecordDirection
import org.hnau.pinfin.model.utils.budget.query.RecordsFilter
import org.hnau.pinfin.model.utils.budget.query.fold

internal fun FlatRecord.matches(
    filter: RecordsFilter?,
): Boolean {
    filter ?: return true

    filter.dateMin?.let { if (date < it) return false }
    filter.dateMax?.let { if (date > it) return false }
    filter.amountMin?.let { if (amount < it) return false }
    filter.amountMax?.let { if (amount > it) return false }

    filter.query?.takeIf(String::isNotBlank)?.let { q ->
        if (comment?.contains(q, ignoreCase = true) != true) return false
    }

    filter.categories?.takeIf { it.isNotEmpty() }?.let { categories ->
        if (category == null || category !in categories) return false
    }

    filter.accounts?.takeIf { it.isNotEmpty() }?.let { accounts ->
        if (account !in accounts) return false
    }

    filter.direction?.let { direction ->
        val ok = direction.fold(
            ifCredit = { category != null && this.direction == RecordDirection.Credit },
            ifDebit = { category != null && this.direction == RecordDirection.Debit },
            ifTransfer = { category == null },
        )
        if (!ok) return false
    }

    return true
}
