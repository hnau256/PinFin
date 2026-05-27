package org.hnau.pinfin.model.utils.budget.state

import arrow.core.NonEmptyList
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.data.expression.AmountExpression
import kotlin.time.Instant

data class TransactionInfo(
    val timestamp: Instant,
    val comment: Comment,
    val type: Type,
) {

    sealed interface Type {

        data class Entry(
            val idWithAccount: KeyValue<AccountId,  AccountInfo>,
            val records: NonEmptyList<Record>,
        ) : Type {

            data class Record(
                val idWithCategory: KeyValue<CategoryId,  CategoryInfo>,
                val amount: AmountExpression,
                val comment: Comment,
            ) {

                companion object
            }

            companion object
        }

        data class Transfer(
            val from: KeyValue<AccountId,  AccountInfo>,
            val to: KeyValue<AccountId,  AccountInfo>,
            val amount: AmountExpression,
        ) : Type {

            companion object
        }

        companion object
    }

    companion object
}