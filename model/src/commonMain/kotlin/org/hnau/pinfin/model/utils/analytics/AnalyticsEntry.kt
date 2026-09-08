package org.hnau.pinfin.model.utils.analytics

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import kotlinx.datetime.LocalDate
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.it
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

data class AnalyticsEntry(
    val idWithAccount: KeyValue<AccountId, AccountInfo>,
    val idWithCategoryOrDirection: Either<AmountDirection, KeyValue<CategoryId, CategoryInfo>>,
    val amount: Amount,
    val date: LocalDate,
) {

    val directionedAmount: KeyValue<AmountDirection, Amount>
        get() = KeyValue(
            key = idWithCategoryOrDirection.fold(
                ifLeft = ::it,
                ifRight = { it.key.direction }
            ),
            value = amount,
        )
}

fun Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>>.toAnalyticsEntries(
    currency: Currency,
): NonEmptyList<AnalyticsEntry> {
    val date: LocalDate = timestamp
    return type.fold(
        ifTransfer = { from, to, amountExpression ->
            val amount: Amount = amountExpression.toAmount(currency.scale)
            nonEmptyListOf(
                AnalyticsEntry(
                    idWithAccount = from,
                    idWithCategoryOrDirection = Either.Left(AmountDirection.Debit),
                    amount = amount,
                    date = date,
                ),
                AnalyticsEntry(
                    idWithAccount = to,
                    idWithCategoryOrDirection = Either.Left(AmountDirection.Credit),
                    amount = amount,
                    date = date,
                )
            )
        },
        ifEntry = { account, records ->
            records
                .map { record ->
                    AnalyticsEntry(
                        idWithAccount = account,
                        idWithCategoryOrDirection = Either.Right(record.category),
                        amount = record.amount.toAmount(currency.scale),
                        date = date,
                    )
                }
        },
    )
}