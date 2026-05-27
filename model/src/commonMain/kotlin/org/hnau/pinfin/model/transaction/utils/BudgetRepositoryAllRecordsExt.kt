package org.hnau.pinfin.model.transaction.utils

import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlin.time.Instant

val BudgetState.allRecords: List<Pair<Instant, TransactionInfo.Type.Entry.Record>>
    get() = this
        .transactions
        .flatMap { idWithTransaction ->
            when (val type = idWithTransaction.value.type) {
                is TransactionInfo.Type.Entry -> type
                    .records
                    .toList()
                    .map { record -> idWithTransaction.value.timestamp to record }

                is TransactionInfo.Type.Transfer -> emptyList()
            }
        }