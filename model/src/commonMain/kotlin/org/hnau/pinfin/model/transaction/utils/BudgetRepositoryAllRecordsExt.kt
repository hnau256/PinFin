package org.hnau.pinfin.model.transaction.utils

import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import org.hnau.pinfin.model.utils.budget.state.fold
import kotlin.time.Instant

val BudgetState.allRecords: List<Pair<Instant, TransactionInfo.Type.Entry.Record>>
    get() = this
        .transactions
        .flatMap { idWithTransaction ->
            idWithTransaction.value.type.fold(
                ifEntry = { _, records ->
                    records
                        .toList()
                        .map { record -> idWithTransaction.value.timestamp to record }
                },
                ifTransfer = { _, _, _ -> emptyList() },
            )
        }