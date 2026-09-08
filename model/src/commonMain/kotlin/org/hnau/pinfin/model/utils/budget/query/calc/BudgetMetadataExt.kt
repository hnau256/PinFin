package org.hnau.pinfin.model.utils.budget.query.calc

import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.query.AccountMeta
import org.hnau.pinfin.model.utils.budget.query.BudgetMetadata
import org.hnau.pinfin.model.utils.budget.query.CategoryMeta
import org.hnau.pinfin.model.utils.budget.query.CurrencyMeta
import org.hnau.pinfin.model.utils.budget.query.SignedAmount
import org.hnau.pinfin.model.utils.budget.state.BudgetState

internal fun BudgetState.metadata(
    id: BudgetId,
): BudgetMetadata {
    val timestamps = transactions.map { it.value.timestamp }
    return BudgetMetadata(
        id = id,
        title = info.title,
        currency = CurrencyMeta(scale = info.currency.scale.scale),
        transactionsCount = transactions.size,
        recordsCount = flatRecords().size,
        firstDate = timestamps.minOrNull(),
        lastDate = timestamps.maxOrNull(),
        categories = categories
            .sortedBy { it.value.title }
            .map { (id, info) ->
                CategoryMeta(
                    id = id,
                    title = info.title,
                    direction = id.direction.toRecordDirection(),
                )
            },
        accounts = accounts
            .sortedBy { it.value.title }
            .map { (id, info) ->
                AccountMeta(
                    id = id,
                    title = info.title,
                    balance = SignedAmount.of(info.amount),
                    hideIfZero = info.hideIfAmountIsZero,
                )
            },
    )
}
