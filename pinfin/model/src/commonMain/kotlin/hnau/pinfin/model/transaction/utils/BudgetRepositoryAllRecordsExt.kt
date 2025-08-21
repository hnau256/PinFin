package hnau.pinfin.model.transaction.utils

import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlin.time.Instant

val BudgetState.allRecords: List<Pair<Instant, TransactionInfo.Type.Entry.Record>>
    get() = this
        .transactions
        .flatMap { transaction ->
            when (val type = transaction.type) {
                is TransactionInfo.Type.Entry -> type
                    .records
                    .toList()
                    .map { record -> transaction.timestamp to record }

                is TransactionInfo.Type.Transfer -> emptyList()
            }
        }