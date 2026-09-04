package org.hnau.pinfin.model.utils.analytics

import arrow.core.Either
import arrow.core.nonEmptySetOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.model.utils.analytics.period.AnalyticsPeriod
import org.hnau.pinfin.model.utils.analytics.period.PeriodDuration
import org.hnau.pinfin.model.utils.analytics.period.PeriodUnit
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [calcPeriod] - чистая функция расчёта сумм по группам (docs/analytics-v2-plan.md, "2.5. Расчёт").
 * Отдельно проверяется правило "среднее по неполным подпериодам" (принятый вариант (а) с
 * fallback на (б), последний абзац "2.5"): делитель - число подпериодов, целиком лежащих
 * и внутри периода страницы, и внутри диапазона данных; если таких нет - все пересекающиеся.
 */
class AnalyticsCalculatorTest {

    private val currency = Currency.default

    private val foodCategoryId = CategoryId(AmountDirection.Debit, "food")
    private val foodCategory = KeyValue(foodCategoryId, CategoryInfo.createDefault(foodCategoryId))

    private val salaryCategoryId = CategoryId(AmountDirection.Credit, "salary")
    private val salaryCategory = KeyValue(salaryCategoryId, CategoryInfo.createDefault(salaryCategoryId))

    private val accountId = AccountId("main")
    private val account = KeyValue(
        accountId,
        AccountInfo.createDefault(accountId, KeyValue(AmountDirection.Debit, Amount.zero)),
    )

    private val secondAccountId = AccountId("second")
    private val secondAccount = KeyValue(
        secondAccountId,
        AccountInfo.createDefault(secondAccountId, KeyValue(AmountDirection.Debit, Amount.zero)),
    )

    private fun amount(
        value: String,
    ): Amount = AmountExpression
        .createOrNull(value, currency)!!
        .toAmount(currency.scale)

    private fun entry(
        category: KeyValue<CategoryId, CategoryInfo>,
        amount: String,
        date: LocalDate,
        account: KeyValue<AccountId, AccountInfo> = this.account,
    ): AnalyticsEntry = AnalyticsEntry(
        idWithAccount = account,
        idWithCategoryOrDirection = Either.Right(category),
        amount = amount(amount),
        date = date,
    )

    @Test
    fun sumSplitsByCategoryAndDirection() {
        val period = LocalDate(2026, 9, 1)..LocalDate(2026, 9, 30)
        val entries = listOf(
            entry(foodCategory, "50", LocalDate(2026, 9, 5)),
            entry(salaryCategory, "200", LocalDate(2026, 9, 10)),
            // Вне периода - не должно повлиять на результат (calcPeriod доверяет вызывающей стороне).
            entry(foodCategory, "999", LocalDate(2026, 10, 1)),
        )
        val config = AnalyticsConfig(
            period = AnalyticsPeriod.Months(1, 1),
            groupBy = AnalyticsConfig.GroupBy.Category,
            operation = AnalyticsConfig.Operation.Sum,
        )
        val groups = listOf(
            GroupKey.Category(foodCategory),
            GroupKey.Category(salaryCategory),
        )

        val result = calcPeriod(
            entries = entries.filter { it.date in period },
            period = period,
            totalRange = period,
            config = config,
            groups = groups,
            currency = currency,
        )

        assertEquals(amount("50"), result.values.debit?.values?.single()?.value?.amount)
        assertEquals(GroupKey.Category(foodCategory), result.values.debit?.values?.single()?.key)
        assertEquals(amount("200"), result.values.credit?.values?.single()?.value?.amount)

        // 200 (credit) - 50 (debit) = 150 credit net.
        assertEquals(KeyValue(AmountDirection.Credit, amount("150")), result.total)
    }

    @Test
    fun groupWithZeroNetAmountIsExcluded() {
        val period = LocalDate(2026, 9, 1)..LocalDate(2026, 9, 30)
        // Одна и та же категория с двумя направлениями, взаимно гасящими друг друга, невозможна
        // для одной категории, но группа "без разбивки" может дать нулевой net.
        val entries = emptyList<AnalyticsEntry>()
        val config = AnalyticsConfig(
            period = AnalyticsPeriod.Months(1, 1),
            groupBy = null,
            operation = AnalyticsConfig.Operation.Sum,
        )

        val result = calcPeriod(
            entries = entries,
            period = period,
            totalRange = period,
            config = config,
            groups = listOf(GroupKey.None),
            currency = currency,
        )

        assertNull(result.values.debit)
        assertNull(result.values.credit)
        assertNull(result.total)
    }

    @Test
    fun noGroupingProducesSingleNetRow() {
        val period = LocalDate(2026, 9, 1)..LocalDate(2026, 9, 30)
        val entries = listOf(
            entry(foodCategory, "50", LocalDate(2026, 9, 5)),
            entry(salaryCategory, "200", LocalDate(2026, 9, 10)),
        )
        val config = AnalyticsConfig(
            period = AnalyticsPeriod.Months(1, 1),
            groupBy = null,
            operation = AnalyticsConfig.Operation.Sum,
        )

        val result = calcPeriod(
            entries = entries,
            period = period,
            totalRange = period,
            config = config,
            groups = listOf(GroupKey.None),
            currency = currency,
        )

        assertEquals(GroupKey.None, result.values.credit?.values?.single()?.key)
        assertEquals(amount("150"), result.values.credit?.values?.single()?.value?.amount)
        assertNull(result.values.debit)
    }

    @Test
    fun averageDividesByFullSubperiodsWhenAvailable() {
        // Страница "год 2026", среднее в месяц. 8 полных месяцев (янв-авг) по 100, сентябрь -
        // неполный (данные только по 4 сентября, до конца диапазона данных). Дивизор = 8
        // (полных месяцев), а не 9 (всех пересекающихся) - см. "2.5", принятый вариант (а).
        val period = LocalDate(2026, 1, 1)..LocalDate(2026, 12, 31)
        val totalRange = LocalDate(2024, 3, 15)..LocalDate(2026, 9, 4)

        val entries = (1..8).map { month ->
            entry(foodCategory, "100", LocalDate(2026, month, 15))
        } + entry(foodCategory, "10", LocalDate(2026, 9, 4))

        val config = AnalyticsConfig(
            period = AnalyticsPeriod.Years(1, Month.JANUARY, 1),
            groupBy = AnalyticsConfig.GroupBy.Category,
            operation = AnalyticsConfig.Operation.Average(
                subperiod = PeriodDuration(count = 1, unit = PeriodUnit.Month),
            ),
        )

        val result = calcPeriod(
            entries = entries,
            period = period,
            totalRange = totalRange,
            config = config,
            groups = listOf(GroupKey.Category(foodCategory)),
            currency = currency,
        )

        // 800 / 8 = 100 - сумма ТОЛЬКО полных подпериодов делится на их число (вариант (а), см. "2.5").
        assertEquals(amount("100"), result.values.debit?.values?.single()?.value?.amount)
    }

    @Test
    fun averageFallsBackToIntersectingSubperiodsWhenNoneAreFull() {
        // Страница короче подпериода: период "январь 2026" (1 месяц), среднее в квартал.
        // Единственный подпериод "1 янв - 31 мар" не помещается в период страницы целиком -
        // полных нет, поэтому делим на 1 (число пересекающихся), как вариант (б).
        val period = LocalDate(2026, 1, 1)..LocalDate(2026, 1, 31)
        val totalRange = LocalDate(2020, 1, 1)..LocalDate(2026, 12, 31)

        val entries = listOf(
            entry(foodCategory, "90", LocalDate(2026, 1, 15)),
        )

        val config = AnalyticsConfig(
            period = AnalyticsPeriod.Months(1, 1),
            groupBy = AnalyticsConfig.GroupBy.Category,
            operation = AnalyticsConfig.Operation.Average(
                subperiod = PeriodDuration(count = 3, unit = PeriodUnit.Month),
            ),
        )

        val result = calcPeriod(
            entries = entries,
            period = period,
            totalRange = totalRange,
            config = config,
            groups = listOf(GroupKey.Category(foodCategory)),
            currency = currency,
        )

        assertEquals(amount("90"), result.values.debit?.values?.single()?.value?.amount)
    }

    @Test
    fun categoriesRestrictionExcludesGroupsOutsideIt() {
        // Фаза 4 (docs/analytics-v2-plan.md, "Фаза 4"): config.categories ограничивает аналитику
        // подмножеством категорий - группа "salary" вне ограничения должна полностью пропасть,
        // а не просто не участвовать во фильтрации записей.
        val period = LocalDate(2026, 9, 1)..LocalDate(2026, 9, 30)
        val entries = listOf(
            entry(foodCategory, "50", LocalDate(2026, 9, 5)),
            entry(salaryCategory, "200", LocalDate(2026, 9, 10)),
        )
        val config = AnalyticsConfig(
            period = AnalyticsPeriod.Months(1, 1),
            groupBy = AnalyticsConfig.GroupBy.Category,
            operation = AnalyticsConfig.Operation.Sum,
            categories = nonEmptySetOf(foodCategoryId),
        )

        val result = calcPeriod(
            entries = entries,
            period = period,
            totalRange = period,
            config = config,
            groups = listOf(
                GroupKey.Category(foodCategory),
                GroupKey.Category(salaryCategory),
            ),
            currency = currency,
        )

        assertEquals(amount("50"), result.values.debit?.values?.single()?.value?.amount)
        assertNull(result.values.credit)
        // Фильтр для перехода в транзакции должен нести то же ограничение.
        assertEquals(
            nonEmptySetOf(foodCategoryId),
            result.values.debit?.values?.single()?.value?.filters?.categories,
        )
    }

    @Test
    fun accountsRestrictionExcludesGroupsOutsideIt() {
        // То же для config.accounts при группировке по счетам.
        val period = LocalDate(2026, 9, 1)..LocalDate(2026, 9, 30)
        val entries = listOf(
            entry(foodCategory, "50", LocalDate(2026, 9, 5), account = account),
            entry(foodCategory, "30", LocalDate(2026, 9, 6), account = secondAccount),
        )
        val config = AnalyticsConfig(
            period = AnalyticsPeriod.Months(1, 1),
            groupBy = AnalyticsConfig.GroupBy.Account,
            operation = AnalyticsConfig.Operation.Sum,
            accounts = nonEmptySetOf(accountId),
        )

        val result = calcPeriod(
            entries = entries,
            period = period,
            totalRange = period,
            config = config,
            groups = listOf(
                GroupKey.Account(account),
                GroupKey.Account(secondAccount),
            ),
            currency = currency,
        )

        assertEquals(amount("50"), result.values.debit?.values?.single()?.value?.amount)
        assertEquals(GroupKey.Account(account), result.values.debit?.values?.single()?.key)
    }
}
