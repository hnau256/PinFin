package hnau.pinfin.data.repository.budget

import arrow.core.NonEmptyList
import hnau.pinfin.data.dto.Amount
import hnau.pinfin.data.dto.Comment
import hnau.pinfin.data.dto.Transaction
import kotlinx.datetime.Instant

data class TransactionInfo(
    val id: Transaction.Id,
    val timestamp: Instant,
    val comment: Comment,
    val type: Type,
) {

    sealed interface Type {

        data class Entry(
            val account: AccountInfo,
            val records: NonEmptyList<Record>,
        ) : Type {

            data class Record(
                val category: CategoryInfo,
                val amount: Amount,
                val comment: Comment,
            ) {

                companion object
            }

            companion object
        }

        data class Transfer(
            val from: AccountInfo,
            val to: AccountInfo,
            val amount: Amount,
        ) : Type {

            companion object
        }

        companion object
    }

    companion object
}