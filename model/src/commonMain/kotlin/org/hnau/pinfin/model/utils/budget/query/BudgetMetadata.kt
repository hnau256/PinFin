package org.hnau.pinfin.model.utils.budget.query

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.CategoryId

@Serializable
data class BudgetSummary(
    val id: BudgetId,
    val title: String,
)

@Serializable
data class BudgetsList(
    val budgets: List<BudgetSummary>,
)

@Serializable
data class CurrencyMeta(
    val scale: Long,
)

@Serializable
data class CategoryMeta(
    val id: CategoryId,
    val title: String,
    val direction: RecordDirection,
)

@Serializable
data class AccountMeta(
    val id: AccountId,
    val title: String,
    val balance: SignedAmount,
    @SerialName("hide_if_zero")
    val hideIfZero: Boolean,
)

@Serializable
data class BudgetMetadata(
    val id: BudgetId,
    val title: String,
    val currency: CurrencyMeta,
    @SerialName("transactions_count")
    val transactionsCount: Int,
    @SerialName("records_count")
    val recordsCount: Int,
    @SerialName("first_date")
    val firstDate: LocalDate? = null,
    @SerialName("last_date")
    val lastDate: LocalDate? = null,
    val categories: List<CategoryMeta>,
    val accounts: List<AccountMeta>,
)
