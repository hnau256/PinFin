package org.hnau.pinfin.model.utils.budget.query

import arrow.core.nonEmptyListOf
import kotlinx.datetime.LocalDate
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import org.hnau.pinfin.model.utils.budget.state.prototype.BudgetStatePrototype

internal val testCurrency: Currency = Currency.default

internal fun testAmount(
    value: String,
): AmountExpression = AmountExpression.createOrNull(value, testCurrency)!!

internal fun testAccount(
    id: String,
    amount: KeyValue<AmountDirection, Amount> = KeyValue(AmountDirection.Debit, Amount.zero),
): KeyValue<AccountId, AccountInfo> {
    val accountId = AccountId(id)
    return KeyValue(accountId, AccountInfo.createDefault(accountId, amount))
}

internal fun testCategory(
    direction: AmountDirection,
    name: String,
): KeyValue<CategoryId, CategoryInfo> {
    val categoryId = CategoryId(direction, name)
    return KeyValue(categoryId, CategoryInfo.createDefault(categoryId))
}

internal fun entryTransaction(
    date: LocalDate,
    account: KeyValue<AccountId, AccountInfo>,
    category: KeyValue<CategoryId, CategoryInfo>,
    amount: String,
    comment: String = "",
): KeyValue<Transaction.Id, TransactionInfo> = KeyValue(
    Transaction.Id.new(),
    TransactionInfo(
        timestamp = date,
        comment = Comment(""),
        type = TransactionInfo.Type.Entry(
            idWithAccount = account,
            records = nonEmptyListOf(
                TransactionInfo.Type.Entry.Record(
                    idWithCategory = category,
                    amount = testAmount(amount),
                    comment = Comment(comment),
                ),
            ),
        ),
    ),
)

internal fun transferTransaction(
    date: LocalDate,
    from: KeyValue<AccountId, AccountInfo>,
    to: KeyValue<AccountId, AccountInfo>,
    amount: String,
): KeyValue<Transaction.Id, TransactionInfo> = KeyValue(
    Transaction.Id.new(),
    TransactionInfo(
        timestamp = date,
        comment = Comment(""),
        type = TransactionInfo.Type.Transfer(
            from = from,
            to = to,
            amount = testAmount(amount),
        ),
    ),
)

internal fun testBudgetState(
    transactions: List<KeyValue<Transaction.Id, TransactionInfo>> = emptyList(),
    categories: List<KeyValue<CategoryId, CategoryInfo>> = emptyList(),
    accounts: List<KeyValue<AccountId, AccountInfo>> = emptyList(),
): BudgetState = BudgetState(
    prototype = BudgetStatePrototype.empty,
    info = BudgetInfo.create(id = org.hnau.pinfin.data.BudgetId.new(), config = null),
    transactions = transactions,
    categories = categories,
    accounts = accounts,
)
