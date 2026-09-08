package org.hnau.pinfin.model.utils.budget.query

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.Transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuerySerializationTest {

    private val amount = Amount.createOrNull(BigDecimal.parseString("12000.00"))!!

    @Test
    fun totalsSerializesWithComputedSum() {
        val json = queryJson.encodeToString(Totals(sumDebit = amount, sumCredit = Amount.zero))

        assertTrue(json.contains("\"sum_debit\""))
        assertTrue(json.contains("\"sum_credit\""))
        assertTrue(json.contains("\"sum\":\"-12000\""))
    }

    @Test
    fun flatRecordSerializesSnakeCaseKeys() {
        val record = FlatRecord(
            transactionId = Transaction.Id.new(),
            date = LocalDate(2026, 8, 14),
            account = AccountId("card"),
            category = null,
            direction = RecordDirection.Debit,
            amount = amount,
            comment = null,
            transferCounterpartAccount = AccountId("cash"),
        )

        val json = queryJson.encodeToString(record)

        assertTrue(json.contains("\"transaction_id\""))
        assertTrue(json.contains("\"transfer_counterpart_account\":\"cash\""))
        assertTrue(json.contains("\"category\":null"))
        assertTrue(json.contains("\"comment\":null"))
    }

    @Test
    fun analyticsTableRoundTripsGroupsMap() {
        val table = AnalyticsTable(
            range = DateRangeDto(LocalDate(2026, 1, 1), LocalDate(2026, 9, 4)),
            groupBy = GroupBy.Category,
            aggregation = Aggregation.Sum(incremental = false),
            periods = listOf(
                AnalyticsRow(
                    start = LocalDate(2026, 1, 1),
                    end = LocalDate(2026, 1, 31),
                    partial = false,
                    groups = mapOf("-Food" to Totals(sumDebit = amount, sumCredit = Amount.zero)),
                    sumDebit = amount,
                    sumCredit = Amount.zero,
                    sum = SignedAmount.creditMinusDebit(Amount.zero, amount),
                ),
            ),
            sumDebit = amount,
            sumCredit = Amount.zero,
            sum = SignedAmount.creditMinusDebit(Amount.zero, amount),
        )

        val json = queryJson.encodeToString(table)
        val decoded = queryJson.decodeFromString<AnalyticsTable>(json)

        assertEquals(table, decoded)
        assertTrue(json.contains("\"group_by\":\"category\""))
    }

    @Test
    fun periodSpecVariantsDecodeFromJson() {
        assertEquals(PeriodSpec.Whole, queryJson.decodeFromString<PeriodSpec>("""{"type":"whole"}"""))
        assertEquals(
            PeriodSpec.Months(count = 1, startDay = 1),
            queryJson.decodeFromString<PeriodSpec>("""{"type":"months","count":1,"start_day":1}"""),
        )
        assertEquals(
            PeriodSpec.Years(count = 1, startMonth = 3, startDay = 1),
            queryJson.decodeFromString<PeriodSpec>("""{"type":"years","count":1,"start_month":3,"start_day":1}"""),
        )
        assertEquals(
            PeriodSpec.Days(count = 7, anchor = LocalDate(2026, 1, 5)),
            queryJson.decodeFromString<PeriodSpec>("""{"type":"days","count":7,"anchor":"2026-01-05"}"""),
        )
    }

    @Test
    fun aggregationVariantsDecodeFromJson() {
        assertEquals(Aggregation.Sum(incremental = true), queryJson.decodeFromString<Aggregation>("""{"type":"sum","incremental":true}"""))
        assertEquals(
            Aggregation.Average(SubperiodSpec(count = 1, unit = SubperiodUnit.Month)),
            queryJson.decodeFromString<Aggregation>("""{"type":"average","subperiod":{"count":1,"unit":"month"}}"""),
        )
    }

    @Test
    fun recordsFilterMissingFieldsDecodeAsNull() {
        val filter = queryJson.decodeFromString<RecordsFilter>("""{}""")

        assertNull(filter.dateMin)
        assertNull(filter.categories)
        assertNull(filter.direction)
    }
}
