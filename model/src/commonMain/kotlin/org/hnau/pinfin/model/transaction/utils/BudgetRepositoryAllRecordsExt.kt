package org.hnau.pinfin.model.transaction.utils

import kotlinx.datetime.LocalDate
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

val BudgetState.allRecords: List<Pair<LocalDate, Record<KeyValue<CategoryId, CategoryInfo>>>>
    get() = this
        .transactions
        .flatMap { idWithTransaction ->
            idWithTransaction.value.type.fold(
                ifEntry = { _, records ->
                    records
                        .records
                        .toList()
                        .map { record -> idWithTransaction.value.timestamp to record }
                },
                ifTransfer = { _, _, _ -> emptyList() },
            )
        }