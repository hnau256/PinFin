package org.hnau.pinfin.model.utils.budget.query

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.model.utils.budget.query.calc.TRANSFER_GROUP_KEY
import org.hnau.pinfin.model.utils.budget.query.impl.BudgetStateQuery
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnalyticsTableCalculatorTest {

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
    fun wholeWithoutGroupsIsOneRowMatchingTotals() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 5), cardAccount, foodCategory, "50"),
            entryTransaction(LocalDate(2026, 2, 10), cardAccount, salaryCategory, "200"),
        )

        val table = query.analytics(
            filter = null,
            period = PeriodSpec.Whole,
            groupBy = null,
            aggregation = Aggregation.Sum(),
        )

        assertEquals(1, table.periods.size)
        val row = table.periods.single()
        assertEquals(false, row.partial)
        assertEquals(table.sumDebit, row.sumDebit)
        assertEquals(table.sumCredit, row.sumCredit)
        assertNull(row.groups)
    }

    @Test
    fun monthlyGroupedByCategoryDropsZeroGroupsAndPutsTransfersInTheirOwnGroup() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 5), cardAccount, foodCategory, "50"),
            entryTransaction(LocalDate(2026, 2, 10), cardAccount, salaryCategory, "200"),
            transferTransaction(LocalDate(2026, 3, 1), cardAccount, cashAccount, "30"),
        )

        val table = query.analytics(
            filter = null,
            period = PeriodSpec.Months(count = 1, startDay = 1),
            groupBy = GroupBy.Category,
            aggregation = Aggregation.Sum(),
        )

        assertEquals(3, table.periods.size)
        val january = table.periods[0]
        assertEquals(setOf(foodCategory.key.id), january.groups!!.keys)
        val march = table.periods[2]
        assertTrue(TRANSFER_GROUP_KEY in march.groups!!.keys)
    }

    @Test
    fun dateBoundsFromFilterSetRangeAndMarkEdgeRowsPartial() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 15), cardAccount, foodCategory, "50"),
            entryTransaction(LocalDate(2026, 2, 15), cardAccount, foodCategory, "50"),
            entryTransaction(LocalDate(2026, 3, 15), cardAccount, foodCategory, "50"),
        )

        val table = query.analytics(
            filter = RecordsFilter(dateMin = LocalDate(2026, 1, 10), dateMax = LocalDate(2026, 3, 20)),
            period = PeriodSpec.Months(count = 1, startDay = 1),
            groupBy = null,
            aggregation = Aggregation.Sum(),
        )

        val range = table.range!!
        assertEquals(LocalDate(2026, 1, 10), range.start)
        assertEquals(LocalDate(2026, 3, 20), range.end)
        assertEquals(true, table.periods.first().partial)
        assertEquals(true, table.periods.last().partial)
    }

    @Test
    fun incrementalSumAccumulatesAndTableTotalsMatchLastRow() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 5), cardAccount, foodCategory, "10"),
            entryTransaction(LocalDate(2026, 2, 5), cardAccount, foodCategory, "20"),
            entryTransaction(LocalDate(2026, 3, 5), cardAccount, foodCategory, "30"),
        )

        val table = query.analytics(
            filter = null,
            period = PeriodSpec.Months(count = 1, startDay = 1),
            groupBy = null,
            aggregation = Aggregation.Sum(incremental = true),
        )

        val debits = table.periods.map { it.sumDebit.value.toStringExpanded() }
        assertEquals(listOf("10", "30", "60"), debits)
        assertEquals(table.sumDebit, table.periods.last().sumDebit)
    }

    @Test
    fun averageDividesByFullMonthsOnlyWithinYear() = runBlocking {
        val transactions = (1..8).map { month ->
            entryTransaction(LocalDate(2026, month, 15), cardAccount, foodCategory, "100")
        }
        val query = queryOf(*transactions.toTypedArray())

        val table = query.analytics(
            filter = RecordsFilter(dateMax = LocalDate(2026, 9, 4)),
            period = PeriodSpec.Years(count = 1, startMonth = 1, startDay = 1),
            groupBy = null,
            aggregation = Aggregation.Average(subperiod = SubperiodSpec(count = 1, unit = SubperiodUnit.Month)),
        )

        // 800 spent over 8 full months -> average 100/month (9th month is not full: cut by date_max).
        assertEquals("100", table.periods.single().sumDebit.value.toStringExpanded())
    }

    @Test
    fun groupByAccountSplitsTransferHalves() = runBlocking {
        val query = queryOf(
            transferTransaction(LocalDate(2026, 1, 5), cardAccount, cashAccount, "40"),
        )

        val table = query.analytics(
            filter = null,
            period = PeriodSpec.Whole,
            groupBy = GroupBy.Account,
            aggregation = Aggregation.Sum(),
        )

        val row = table.periods.single()
        val groups = row.groups!!
        assertEquals("40", groups[cardAccount.key.id]!!.sumDebit.value.toStringExpanded())
        assertEquals("40", groups[cashAccount.key.id]!!.sumCredit.value.toStringExpanded())
        assertEquals(row.sum, SignedAmount.zero)
    }

    @Test
    fun emptyFilterResultProducesEmptyTable() = runBlocking {
        val query = queryOf(
            entryTransaction(LocalDate(2026, 1, 5), cardAccount, foodCategory, "50"),
        )

        val table = query.analytics(
            filter = RecordsFilter(categories = listOf(salaryCategory.key)),
            period = PeriodSpec.Whole,
            groupBy = null,
            aggregation = Aggregation.Sum(),
        )

        assertNull(table.range)
        assertTrue(table.periods.isEmpty())
        assertEquals(SignedAmount.zero, table.sum)
    }
}
