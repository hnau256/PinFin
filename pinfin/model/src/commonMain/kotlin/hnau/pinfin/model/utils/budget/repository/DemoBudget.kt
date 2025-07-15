package hnau.pinfin.model.utils.budget.repository

import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.sumOf
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.Amount
import hnau.pinfin.data.BudgetConfig
import hnau.pinfin.data.BudgetId
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.data.CategoryId
import hnau.pinfin.data.Comment
import hnau.pinfin.data.Record
import hnau.pinfin.data.Transaction
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.budget.state.updateTypeMapper
import hnau.pinfin.model.utils.budget.upchain.Update
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Random
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds


object DemoBudget {

    val id: BudgetId = "bca3f6b1-3ffc-40a9-bd50-a42150b6abd4".let(BudgetId.stringMapper.direct)

    private val accountIdBankCard = AccountId("Карточка")
    private val accountIdCash = AccountId("Наличка")

    private val categoryIdHome: CategoryId = CategoryId(
        direction = AmountDirection.Debit,
        title = "Дом",
    )

    private val categoryIdServices: CategoryId = CategoryId(
        direction = AmountDirection.Debit,
        title = "ЖКХ",
    )

    private val categoryIdTransport: CategoryId = CategoryId(
        direction = AmountDirection.Debit,
        title = "Транспорт",
    )

    private val categoryIdClothes: CategoryId = CategoryId(
        direction = AmountDirection.Debit,
        title = "Одежда",
    )

    private val categoryIdHealth: CategoryId = CategoryId(
        direction = AmountDirection.Debit,
        title = "Здоровье",
    )

    private val categoryIdFood: CategoryId = CategoryId(
        direction = AmountDirection.Debit,
        title = "Еда",
    )

    private val categoryIdDelicious: CategoryId = CategoryId(
        direction = AmountDirection.Debit,
        title = "Вкусности",
    )

    private val categoryIdFun: CategoryId = CategoryId(
        direction = AmountDirection.Debit,
        title = "Развлечения",
    )

    private val categoryIdGifts: CategoryId = CategoryId(
        direction = AmountDirection.Debit,
        title = "Подарки",
    )

    private val categoryIdSalary: CategoryId = CategoryId(
        direction = AmountDirection.Credit,
        title = "Зарплата",
    )

    val updates: List<Update>
        get() = buildList<Transaction> {
            val end = Clock.System.now()
            val start = end.minus((6 * 365).days)
            addAll(createSalaryTransactions(start, end))
            addAll(createOutcomeTransactions(start, end))
            addAll(createTransferTransactions(start, end))
        }
            .sortedBy(Transaction::timestamp)
            .let { transactions ->
                buildList {
                    add(
                        UpdateType.Config(
                            BudgetConfig(
                                title = "Demo бюджет",
                            )
                        )
                    )
                    addAll(
                        transactions
                            .map { transaction ->
                                UpdateType.Transaction(
                                    id = Transaction.Id.new(),
                                    transaction = transaction,
                                )
                            }
                    )
                }.map(UpdateType.updateTypeMapper.reverse)
            }

    private val random = Random(0)

    private fun nextNormalDistributionRandom(
        min: Number,
        max: Number,
    ): Float {
        val minFloat = min.toFloat()
        val maxFloat = max.toFloat()
        val delta = maxFloat - minFloat
        return (minFloat + random.nextGaussian() / 3 * delta).toFloat().coerceIn(minFloat, maxFloat)
    }

    private fun createTransferTransactions(
        start: Instant,
        end: Instant,
    ): List<Transaction> = emptyList()//TODO

    private fun createOutcomeTransactions(
        start: Instant,
        end: Instant,
    ): List<Transaction> {

        data class AvailableItem(
            val title: String,
            val probability: Float,
            val minAmount: Long,
            val maxAmount: Long,
        )

        data class CategoryInfo(
            val id: CategoryId,
            val probability: Float,
            val minComponentsCount: Int,
            val maxComponentsCount: Int,
            val availableItems: List<AvailableItem>,
        )

        val categories = listOf(
            CategoryInfo(
                id = categoryIdFood,
                probability = 0.3f,
                minComponentsCount = 1,
                maxComponentsCount = 15,
                availableItems = listOf(
                    AvailableItem("Bread", 0.1f, 2000, 6000),
                    AvailableItem("Meat", 0.03f, 8000, 50000),
                    AvailableItem("Juice", 0.02f, 5000, 10000),
                    AvailableItem("Pasta", 0.04f, 3000, 9000),
                    AvailableItem("Cabbage", 0.01f, 1000, 3000),
                    AvailableItem("Onion", 0.01f, 1000, 3000),
                    AvailableItem("Jam", 0.01f, 4000, 8000),
                    AvailableItem("Milk", 0.05f, 5000, 7000),
                    AvailableItem("Sausages", 0.02f, 7000, 9000),
                    AvailableItem("Ketchup", 0.03f, 10000, 15000),
                    AvailableItem("Cheese", 0.05f, 13000, 17000),
                    AvailableItem("Potato", 0.08f, 1000, 2000),
                    AvailableItem("Tea", 0.02f, 20000, 25000),
                    AvailableItem("Carrot", 0.01f, 2000, 2500),
                    AvailableItem("Fish", 0.02f, 15000, 25000),
                    AvailableItem("Apples", 0.06f, 15000, 20000),
                    AvailableItem("Bananas", 0.04f, 10000, 15000)
                )
            ),
            CategoryInfo(
                id = categoryIdDelicious,
                probability = 0.2f,
                minComponentsCount = 1,
                maxComponentsCount = 5,
                availableItems = listOf(
                    AvailableItem("Sweets", 0.1f, 5000, 16000),
                    AvailableItem("Chocolate", 0.1f, 8000, 10000),
                    AvailableItem("Condensed milk", 0.05f, 8000, 20000),
                    AvailableItem("Icecream", 0.07f, 15000, 25000),
                    AvailableItem("Yogurt", 0.09f, 4000, 6000),
                    AvailableItem("Cake", 0.04f, 20000, 40000),
                    AvailableItem("Cookies", 0.1f, 3000, 9000)
                )
            ),
            CategoryInfo(
                id = categoryIdFun,
                probability = 0.1f,
                minComponentsCount = 1,
                maxComponentsCount = 3,
                availableItems = listOf(
                    AvailableItem("Air hockey", 0.1f, 10000, 13000),
                    AvailableItem("Theatre", 0.1f, 20000, 40000),
                    AvailableItem("Toy", 0.05f, 10000, 30000),
                    AvailableItem("Zoo", 0.07f, 10000, 15000)
                )
            ),
            CategoryInfo(
                id = categoryIdGifts,
                probability = 0.1f,
                minComponentsCount = 1,
                maxComponentsCount = 2,
                availableItems = listOf(
                    AvailableItem("Sweets", 0.1f, 10000, 20000),
                    AvailableItem("Fruits", 0.07f, 8000, 25000),
                    AvailableItem("Toy", 0.05f, 7000, 20000)
                )
            ),
            CategoryInfo(
                id = categoryIdHealth,
                probability = 0.05f,
                minComponentsCount = 1,
                maxComponentsCount = 2,
                availableItems = listOf(
                    AvailableItem("Medical tests", 0.1f, 10000, 50000),
                    AvailableItem("Drugs", 0.2f, 5000, 100000),
                    AvailableItem("Doctor", 0.1f, 70000, 120000)
                )
            ),
            CategoryInfo(
                id = categoryIdClothes,
                probability = 0.05f,
                minComponentsCount = 1,
                maxComponentsCount = 5,
                availableItems = listOf(
                    AvailableItem("Jeans", 0.2f, 60000, 150000),
                    AvailableItem("Pullover", 0.2f, 80000, 200000),
                    AvailableItem("Socks", 0.5f, 9000, 20000),
                    AvailableItem("Shirt", 0.4f, 70000, 180000),
                    AvailableItem("Jacket", 0.1f, 200000, 400000),
                    AvailableItem("Boots", 0.3f, 100000, 300000)
                )
            ),
            CategoryInfo(
                id = categoryIdTransport,
                probability = 0.1f,
                minComponentsCount = 1,
                maxComponentsCount = 1,
                availableItems = listOf(
                    AvailableItem("Fare", 0.5f, 3000, 9000),
                    AvailableItem("Petrol", 0.2f, 200000, 270000),
                    AvailableItem("Taxi", 0.3f, 15000, 50000)
                )
            ),
            CategoryInfo(
                id = categoryIdHome,
                probability = 0.2f,
                minComponentsCount = 1,
                maxComponentsCount = 10,
                availableItems = listOf(
                    AvailableItem("Cutting board", 0.01f, 8000, 10000),
                    AvailableItem("Chear", 0.01f, 180000, 200000),
                    AvailableItem("Bed linen", 0.01f, 200000, 250000),
                    AvailableItem("Curtains", 0.01f, 100000, 150000),
                    AvailableItem("Soap", 0.1f, 10000, 12000),
                    AvailableItem("Toothpaste", 0.1f, 8000, 10000),
                    AvailableItem("Toothbrush", 0.1f, 5000, 7000),
                    AvailableItem("Pot", 0.03f, 50000, 80000),
                    AvailableItem("Plates", 0.05f, 10000, 100000),
                    AvailableItem("Cups", 0.08f, 15000, 120000),
                    AvailableItem("Lamps", 0.1f, 12000, 50000),
                    AvailableItem("Threads", 0.05f, 2000, 5000),
                    AvailableItem("Fabric", 0.05f, 20000, 60000)
                )
            ),
            CategoryInfo(
                id = categoryIdServices,
                probability = 0.1f,
                minComponentsCount = 1,
                maxComponentsCount = 4,
                availableItems = listOf(
                    AvailableItem("Water", 0.1f, 30000, 50000),
                    AvailableItem("Gas", 0.1f, 8000, 10000),
                    AvailableItem("Electricity", 0.1f, 40000, 60000),
                    AvailableItem("Internet", 0.1f, 70000, 80000),
                    AvailableItem("Waste disposal", 0.1f, 8000, 9000),
                    AvailableItem("Phone", 0.1f, 30000, 40000)
                )
            )
        )

        val inflationMin = 0.02f
        val inflationMax = 0.4f
        var lastInflationYear: Long? = null
        var infaltion = 1f

        val minDelta = 1.hours
        val maxDelta = 2.days

        val maxCategoriesCount = 3

        fun <T> Iterable<T>.randomByProbability(
            extractProbability: T.() -> Float,
            normalize: Boolean = true,
        ): T {
            val totalProbability = sumOf { it.extractProbability() }
            var sum = 0f
            val value = when (normalize) {
                true -> nextNormalDistributionRandom(0f, totalProbability)
                false -> random.nextFloat() * totalProbability
            }
            forEach { item ->
                val probability = item.extractProbability()
                sum += probability
                if (value <= sum) {
                    return item
                }
            }
            error("Total probability less than probabilities sum")
        }


        fun <T> Iterable<T>.extractRandomItems(
            minCount: Int,
            maxCount: Int,
            extractProbability: T.() -> Float,
            normalize: Boolean = true,
        ): List<T> = nextNormalDistributionRandom(
            min = minCount,
            max = maxCount,
        )
            .roundToInt()
            .let { categoriesCount -> 0 until categoriesCount }
            .let { indexes ->
                val items = toMutableSet()
                indexes.map {
                    val item = items.randomByProbability(
                        extractProbability = extractProbability,
                        normalize = normalize,
                    )
                    items -= item
                    item
                }
            }

        val result = ArrayList<Transaction>()
        var now = start
        while (now < end) {
            val year = (now - start).inWholeDays / 365
            if (lastInflationYear != year) {
                infaltion *= 1f + nextNormalDistributionRandom(inflationMin, inflationMax)
                lastInflationYear = year
            }
            val records = categories
                .extractRandomItems(
                    minCount = 1,
                    maxCount = maxCategoriesCount,
                    extractProbability = CategoryInfo::probability,
                    normalize = false,
                )
                .flatMap { info ->
                    info
                        .availableItems
                        .extractRandomItems(
                            minCount = info.minComponentsCount,
                            maxCount = info.maxComponentsCount,
                            extractProbability = AvailableItem::probability,
                        )
                        .map { info.id to it }
                }
                .shuffled()
                .map { (category, item) ->
                    Record(
                        category = category,
                        amount = nextNormalDistributionRandom(
                            item.minAmount,
                            item.maxAmount
                        ).roundToInt().toUInt().let(::Amount),
                        comment = Comment(item.title),
                    )
                }
                .toNonEmptyListOrNull()!!
            result.add(
                Transaction(
                    timestamp = now,
                    comment = Comment(""),
                    type = Transaction.Type.Entry(
                        account = accountIdBankCard,
                        records = records,
                    ),
                )
            )
            val delta = nextNormalDistributionRandom(
                minDelta.inWholeMilliseconds,
                maxDelta.inWholeMilliseconds
            ).toInt().milliseconds
            now += delta
        }
        return result
    }

    private fun createSalaryTransactions(
        start: Instant,
        end: Instant,
    ): List<Transaction> {
        data class Part(
            val monthOffset: Duration,
            val parcentage: Float,
        )

        val parts = listOf(
            Part(5.days, 0.4f),
            Part(20.days, 0.6f)
        )
        val maxTimestampOffset = 3.days
        val nextMonthIncreaseProbability = 0.2f
        val increaseMinPercentage = 0.05f
        val increaseMaxPercentage = 0.4f

        var currentMonth = start.toLocalDateTime(TimeZone.UTC).date.let { date ->
            LocalDate(
                year = date.year,
                month = date.month,
                dayOfMonth = 1,
            )
        }
        val lastDay = end.toLocalDateTime(TimeZone.UTC).date
        var salary: Long = 37243 * 100
        var result = ArrayList<Transaction>()
        while (currentMonth < lastDay) {
            parts.forEachIndexed { i, (offset, percentage) ->
                val delta = offset.inWholeMilliseconds + nextNormalDistributionRandom(
                    -maxTimestampOffset.inWholeMilliseconds,
                    maxTimestampOffset.inWholeMilliseconds
                )
                result.add(
                    Transaction(
                        timestamp = currentMonth.atTime(0, 0)
                            .toInstant(TimeZone.UTC) + offset + delta.toInt().milliseconds,
                        comment = Comment(""),
                        type = Transaction.Type.Entry(
                            account = accountIdBankCard,
                            records = nonEmptyListOf(
                                Record(
                                    category = categoryIdSalary,
                                    comment = Comment(""),
                                    amount = (salary * percentage).toUInt().let(::Amount),
                                )
                            ),
                        ),
                    ),
                )
            }
            currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH)
            if (nextNormalDistributionRandom(0, 1) <= nextMonthIncreaseProbability) {
                salary += salary * (increaseMinPercentage + (increaseMaxPercentage - increaseMinPercentage) * random.nextFloat()).toLong()
            }
        }
        return result
    }

}