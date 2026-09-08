package org.hnau.pinfin.model.utils.budget.query

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Transaction

@Fold
@Serializable
enum class RecordDirection {

    @SerialName("debit")
    Debit,

    @SerialName("credit")
    Credit,
}

/** A single flattened movement of money on one account. */
@Serializable
data class FlatRecord(
    @SerialName("transaction_id")
    val transactionId: Transaction.Id,
    @SerialName("date")
    val date: LocalDate,
    @SerialName("account")
    val account: AccountId,
    @SerialName("category")
    val category: CategoryId? = null,
    @SerialName("direction")
    val direction: RecordDirection,
    @SerialName("amount")
    val amount: Amount,
    @SerialName("comment")
    val comment: String? = null,
    @SerialName("transfer_counterpart_account")
    val transferCounterpartAccount: AccountId? = null,
)
