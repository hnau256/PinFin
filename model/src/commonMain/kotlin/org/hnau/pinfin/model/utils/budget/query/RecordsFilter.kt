package org.hnau.pinfin.model.utils.budget.query

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.CategoryId

@Fold
@Serializable
enum class RecordsFilterDirection {

    @SerialName("credit")
    Credit,

    @SerialName("debit")
    Debit,

    @SerialName("transfer")
    Transfer,
}

/** All fields are optional; absence means no restriction. */
@Serializable
data class RecordsFilter(
    @SerialName("date_min")
    val dateMin: LocalDate? = null,
    @SerialName("date_max")
    val dateMax: LocalDate? = null,
    @SerialName("query")
    val query: String? = null,
    @SerialName("amount_min")
    val amountMin: Amount? = null,
    @SerialName("amount_max")
    val amountMax: Amount? = null,
    @SerialName("categories")
    val categories: List<CategoryId>? = null,
    @SerialName("accounts")
    val accounts: List<AccountId>? = null,
    @SerialName("direction")
    val direction: RecordsFilterDirection? = null,
)
