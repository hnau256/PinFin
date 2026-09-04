package org.hnau.pinfin.model.transaction.utils

import kotlinx.datetime.LocalDate
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import org.hnau.pinfin.model.utils.budget.state.fold

val BudgetState.allRecords: List<Pair<LocalDate, TransactionInfo.Type.Entry.Record>>
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