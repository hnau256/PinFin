package org.hnau.pinfin.model.utils.budget.query

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.query.impl.BudgetStateQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [BudgetStateQuery.records] flattens transactions into [FlatRecord]s (docs/mcp-plan.md, "4.1").
 */
class FlatRecordsTest {

    private val account = testAccount("card")
    private val foodCategory = testCategory(AmountDirection.Debit, "Food")

    @Test
    fun entryWithMultipleRecordsProducesOneFlatRecordPerRecord() = runBlocking {
        val date = LocalDate(2026, 1, 10)
        val transaction = entryTransaction(date, account, foodCategory, "50", comment = "lunch")
        val query = BudgetStateQuery(
            id = BudgetId.new(),
            state = MutableStateFlow(
                testBudgetState(
                    transactions = listOf(transaction),
                    categories = listOf(foodCategory),
                    accounts = listOf(account),
                ),
            ),
        )

        val page = query.records(filter = null, offset = 0, limit = 200)

        assertEquals(1, page.records.size)
        val record = page.records.single()
        assertEquals(foodCategory.key, record.category)
        assertEquals(RecordDirection.Debit, record.direction)
        assertEquals("lunch", record.comment)
        assertNull(record.transferCounterpartAccount)
    }

    @Test
    fun emptyCommentBecomesNull() = runBlocking {
        val date = LocalDate(2026, 1, 10)
        val transaction = entryTransaction(date, account, foodCategory, "50", comment = "")
        val query = BudgetStateQuery(
            id = BudgetId.new(),
            state = MutableStateFlow(
                testBudgetState(
                    transactions = listOf(transaction),
                    categories = listOf(foodCategory),
                    accounts = listOf(account),
                ),
            ),
        )

        val page = query.records(filter = null, offset = 0, limit = 200)

        assertNull(page.records.single().comment)
    }

    @Test
    fun transferProducesTwoFlatRecordsWithoutCategoryOrComment() = runBlocking {
        val date = LocalDate(2026, 1, 10)
        val from = testAccount("card")
        val to = testAccount("cash")
        val transaction = transferTransaction(date, from, to, "100")
        val query = BudgetStateQuery(
            id = BudgetId.new(),
            state = MutableStateFlow(
                testBudgetState(
                    transactions = listOf(transaction),
                    accounts = listOf(from, to),
                ),
            ),
        )

        val page = query.records(filter = null, offset = 0, limit = 200)

        assertEquals(2, page.records.size)
        val fromRecord = page.records.single { it.account == from.key }
        val toRecord = page.records.single { it.account == to.key }

        assertNull(fromRecord.category)
        assertNull(fromRecord.comment)
        assertEquals(RecordDirection.Debit, fromRecord.direction)
        assertEquals(to.key, fromRecord.transferCounterpartAccount)

        assertNull(toRecord.category)
        assertEquals(RecordDirection.Credit, toRecord.direction)
        assertEquals(from.key, toRecord.transferCounterpartAccount)

        assertEquals(fromRecord.transactionId, toRecord.transactionId)
    }
}
