package org.hnau.pinfin.model.utils.budget.query

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.model.utils.budget.query.impl.BudgetStateQuery
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class RecordsFilterTest {

    private val cardAccount = testAccount("card")
    private val cashAccount = testAccount("cash")
    private val foodCategory = testCategory(AmountDirection.Debit, "Food")
    private val salaryCategory = testCategory(AmountDirection.Credit, "Salary")

    private fun queryOf(
        vararg transactions: KeyValue<Transaction.Id, TransactionInfo>,
    ): BudgetStateQuery = BudgetStateQuery(
        id = BudgetId.new(),
        state = MutableStateFlow(
            testBudgetState(
                transactions = transactions.toList(),
                categories = listOf(foodCategory, salaryCategory),
                accounts = listOf(cardAccount, cashAccount),
            ),
        ),
    )

    @Test
    fun dateRangeIsInclusive() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10"),
            entryTransaction(LocalDate(2026, 1, 15), cardAccount, foodCategory, "20"),
            entryTransaction(LocalDate(2026, 1, 31), cardAccount, foodCategory, "30"),
        )

        val page = query.records(
            filter = RecordsFilter(dateMin = LocalDate(2026, 1, 1), dateMax = LocalDate(2026, 1, 15)),
            offset = 0,
            limit = 200,
        )

        assertEquals(2, page.total)
    }

    @Test
    fun queryFiltersByCommentCaseInsensitively() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10", comment = "Lunch at cafe"),
            entryTransaction(LocalDate(2026, 1, 2), cardAccount, foodCategory, "20", comment = "groceries"),
        )

        val page = query.records(filter = RecordsFilter(query = "LUNCH"), offset = 0, limit = 200)

        assertEquals(1, page.total)
        assertEquals("Lunch at cafe", page.records.single().comment)
    }

    @Test
    fun amountRangeIsInclusive() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10"),
            entryTransaction(LocalDate(2026, 1, 2), cardAccount, foodCategory, "50"),
            entryTransaction(LocalDate(2026, 1, 3), cardAccount, foodCategory, "100"),
        )

        val page = query.records(
            filter = RecordsFilter(amountMin = testAmount("10").toAmount(testCurrency.scale), amountMax = testAmount("50").toAmount(testCurrency.scale)),
            offset = 0,
            limit = 200,
        )

        assertEquals(2, page.total)
    }

    @Test
    fun categoriesEmptyListIsEquivalentToNull() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10"),
            entryTransaction(LocalDate(2026, 1, 2), cardAccount, salaryCategory, "20"),
        )

        val page = query.records(filter = RecordsFilter(categories = emptyList()), offset = 0, limit = 200)

        assertEquals(2, page.total)
    }

    @Test
    fun categoriesRestrictsToListedIds() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10"),
            entryTransaction(LocalDate(2026, 1, 2), cardAccount, salaryCategory, "20"),
            transferTransaction(LocalDate(2026, 1, 3), cardAccount, cashAccount, "30"),
        )

        val page = query.records(filter = RecordsFilter(categories = listOf(foodCategory.key)), offset = 0, limit = 200)

        assertEquals(1, page.total)
        assertEquals(foodCategory.key, page.records.single().category)
    }

    @Test
    fun accountsEmptyListIsEquivalentToNull() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10"),
            entryTransaction(LocalDate(2026, 1, 2), cashAccount, foodCategory, "20"),
        )

        val page = query.records(filter = RecordsFilter(accounts = emptyList()), offset = 0, limit = 200)

        assertEquals(2, page.total)
    }

    @Test
    fun accountsRestrictsToListedIds() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10"),
            entryTransaction(LocalDate(2026, 1, 2), cashAccount, foodCategory, "20"),
        )

        val page = query.records(filter = RecordsFilter(accounts = listOf(cardAccount.key)), offset = 0, limit = 200)

        assertEquals(1, page.total)
        assertEquals(cardAccount.key, page.records.single().account)
    }

    @Test
    fun directionCreditExcludesTransfersAndDebits() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10"),
            entryTransaction(LocalDate(2026, 1, 2), cardAccount, salaryCategory, "20"),
            transferTransaction(LocalDate(2026, 1, 3), cardAccount, cashAccount, "30"),
        )

        val page = query.records(filter = RecordsFilter(direction = RecordsFilterDirection.Credit), offset = 0, limit = 200)

        assertEquals(1, page.total)
        assertEquals(salaryCategory.key, page.records.single().category)
    }

    @Test
    fun directionDebitExcludesTransfersAndCredits() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10"),
            entryTransaction(LocalDate(2026, 1, 2), cardAccount, salaryCategory, "20"),
            transferTransaction(LocalDate(2026, 1, 3), cardAccount, cashAccount, "30"),
        )

        val page = query.records(filter = RecordsFilter(direction = RecordsFilterDirection.Debit), offset = 0, limit = 200)

        assertEquals(1, page.total)
        assertEquals(foodCategory.key, page.records.single().category)
    }

    @Test
    fun directionTransferPassesBothHalvesOnly() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 1), cardAccount, foodCategory, "10"),
            transferTransaction(LocalDate(2026, 1, 3), cardAccount, cashAccount, "30"),
        )

        val page = query.records(filter = RecordsFilter(direction = RecordsFilterDirection.Transfer), offset = 0, limit = 200)

        assertEquals(2, page.total)
        page.records.forEach { assertEquals(null, it.category) }
    }
}
