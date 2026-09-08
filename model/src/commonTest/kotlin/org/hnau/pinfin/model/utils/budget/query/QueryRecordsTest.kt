package org.hnau.pinfin.model.utils.budget.query

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.query.impl.BudgetStateQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QueryRecordsTest {

    private val account = testAccount("card")
    private val foodCategory = testCategory(AmountDirection.Debit, "Food")

    private fun queryOfDailyTransactions(
        count: Int,
    ): BudgetStateQuery {
        val transactions = (1..count).map { day ->
            entryTransaction(LocalDate(2026, 1, day), account, foodCategory, day.toString())
        }
        return BudgetStateQuery(
            id = BudgetId.new(),
            state = MutableStateFlow(
                testBudgetState(
                    transactions = transactions,
                    categories = listOf(foodCategory),
                    accounts = listOf(account),
                ),
            ),
        )
    }

    @Test
    fun newestFirstOrdering() = runBlocking {
        val query = queryOfDailyTransactions(3)

        val page = query.records(filter = null, offset = 0, limit = 200)

        assertEquals(listOf("3", "2", "1"), page.records.map { it.amount.value.toStringExpanded() })
    }

    @Test
    fun recordsWithinOneTransactionKeepForwardOrder() = runBlocking {
        val from = testAccount("card")
        val to = testAccount("cash")
        val transaction = transferTransaction(LocalDate(2026, 1, 1), from, to, "10")
        val query = BudgetStateQuery(
            id = BudgetId.new(),
            state = MutableStateFlow(
                testBudgetState(transactions = listOf(transaction), accounts = listOf(from, to)),
            ),
        )

        val page = query.records(filter = null, offset = 0, limit = 200)

        assertEquals(from.key, page.records[0].account)
        assertEquals(to.key, page.records[1].account)
    }

    @Test
    fun totalHasMoreAndOffsetLimitAreReported() = runBlocking {
        val query = queryOfDailyTransactions(5)

        val page = query.records(filter = null, offset = 1, limit = 2)

        assertEquals(5, page.total)
        assertEquals(1, page.offset)
        assertEquals(2, page.limit)
        assertEquals(2, page.records.size)
        assertTrue(page.hasMore)
    }

    @Test
    fun limitAboveMaxIsClampedTo200() = runBlocking {
        val query = queryOfDailyTransactions(3)

        val page = query.records(filter = null, offset = 0, limit = 500)

        assertEquals(MAX_RECORDS_LIMIT, page.limit)
    }

    @Test
    fun offsetBeyondTotalReturnsEmptyPage() = runBlocking {
        val query = queryOfDailyTransactions(3)

        val page = query.records(filter = null, offset = 100, limit = 10)

        assertTrue(page.records.isEmpty())
        assertFalse(page.hasMore)
    }
}
