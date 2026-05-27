package org.hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.model.utils.amount
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

data class AnalyticsEntry(
    val idWithAccount: KeyValue<AccountId,  AccountInfo>,
    val idWithCategory: KeyValue<CategoryId,  CategoryInfo>?,
    val amount: Amount,
    val date: LocalDate,
)

fun TransactionInfo.toAnalyticsEntries(
    currency: Currency,
): NonEmptyList<AnalyticsEntry> {
    val date = timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val amount = amount(currency)
    return when (type) {
        is TransactionInfo.Type.Transfer -> nonEmptyListOf(
            AnalyticsEntry(
                idWithAccount = type.from,
                idWithCategory = null,
                amount = -amount,
                date = date,
            ),
            AnalyticsEntry(
                idWithAccount = type.to,
                idWithCategory = null,
                amount = amount,
                date = date,
            )
        )

        is TransactionInfo.Type.Entry -> type
            .records
            .map { record ->
                AnalyticsEntry(
                    idWithAccount = type.idWithAccount,
                    idWithCategory = record.idWithCategory,
                    amount = record.amount.toAmount(currency.scale),
                    date = date,
                )
            }
    }
}