package org.hnau.pinfin.model.utils.budget.repository.demo

import arrow.core.toNonEmptyListOrNull
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.data.Hue
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.expression.Expression
import org.hnau.pinfin.model.utils.icons.IconVariant
import org.hnau.pinfin.model.utils.icons.icon
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Instant

class DemoBudgetGenerator(
    private val config: DemoBudgetConfig,
    seed: Long = 0,
) {

    private val random = Random(seed)
    private var spareGaussian = Double.NaN

    private val loc get() = config.localization

    private fun nextGaussian(): Double {
        if (!spareGaussian.isNaN()) {
            val spare = spareGaussian
            spareGaussian = Double.NaN
            return spare
        }
        var u: Double
        var v: Double
        var s: Double
        do {
            u = random.nextDouble() * 2 - 1
            v = random.nextDouble() * 2 - 1
            s = u * u + v * v
        } while (s >= 1 || s == 0.0)
        val factor = sqrt(-2.0 * ln(s) / s)
        spareGaussian = v * factor
        return u * factor
    }

    private fun nextNormal(mean: Double, std: Double): Double =
        mean + nextGaussian() * std

    private fun nextBetween(min: Double, max: Double): Double {
        val mean = (min + max) / 2.0
        val std = (max - min) / 5.0
        return nextNormal(mean, std).coerceIn(min, max)
    }

    private fun nextInt(min: Int, max: Int): Int =
        random.nextInt(min, max + 1)

    private fun shouldHappen(probability: Double): Boolean =
        random.nextDouble() < probability

    private fun <T> weightedPick(items: List<Pair<T, Double>>): T {
        val total = items.sumOf { it.second }
        var r = random.nextDouble() * total
        for ((item, weight) in items) {
            r -= weight
            if (r <= 0) return item
        }
        return items.last().first
    }

    private fun nextPoisson(mean: Double): Int {
        val l = exp(-mean)
        var k = 0
        var p = 1.0
        while (p > l) {
            k++
            p *= random.nextDouble()
        }
        return k - 1
    }

    private fun randomTimestamp(date: LocalDate, fromHour: Int, toHour: Int): Instant {
        val hour = nextInt(fromHour, toHour - 1)
        val minute = nextInt(0, 59)
        val second = nextInt(0, 59)
        return date.atTime(hour, minute, second).toInstant(TimeZone.UTC)
    }

    private fun Instant.toLocalDate(): LocalDate =
        toLocalDateTime(TimeZone.UTC).date

    private fun LocalDate.isWeekend(): Boolean =
        dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

    private fun daysBetween(a: LocalDate, b: LocalDate): Int {
        val aInstant = a.atTime(0, 0).toInstant(TimeZone.UTC)
        val bInstant = b.atTime(0, 0).toInstant(TimeZone.UTC)
        return ((bInstant - aInstant).inWholeDays).toInt()
    }

    private fun isLeapYear(year: Int): Boolean =
        year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    private fun monthNumber(month: Month): Int = month.ordinal + 1

    private fun monthLength(year: Int, monthNumber: Int): Int = when (monthNumber) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }

    private sealed class Pricing {
        data class Fixed(val cents: Long) : Pricing()
        data class Weight(val meanCents: Long) : Pricing()
        data class Range(val minCents: Long, val maxCents: Long) : Pricing()
    }

    private data class Product(val name: String, val pricing: Pricing)

    private data class ProductGroup(val products: List<Product>)

    private sealed class StoreDef {
        abstract val name: String
        abstract val weekdayProb: Double
        abstract val weekendProb: Double
        abstract val isLargePurchase: Boolean

        data class MultiItem(
            override val name: String,
            override val weekdayProb: Double,
            override val weekendProb: Double,
            val markup: Double,
            val pools: List<Pair<ProductGroup, CategoryId>>,
            val poolWeights: List<Double>,
            val minItems: Int,
            val maxItems: Int,
        ) : StoreDef() {
            override val isLargePurchase: Boolean = false
        }

        data class SingleAmount(
            override val name: String,
            override val weekdayProb: Double,
            override val weekendProb: Double,
            val markup: Double,
            val categoryId: CategoryId,
            val minCents: Long,
            val maxCents: Long,
        ) : StoreDef() {
            override val isLargePurchase: Boolean = false
        }

        data class LargePurchase(
            override val name: String,
            override val weekdayProb: Double,
            override val weekendProb: Double,
            val categoryId: CategoryId,
            val minCents: Long,
            val maxCents: Long,
        ) : StoreDef() {
            override val isLargePurchase: Boolean = true
        }
    }

    private data class Job(
        val from: LocalDate,
        val to: LocalDate,
        var monthlySalary: Double,
    )

    private class BalanceTracker {
        var card: Long = 0
        var savings: Long = 0
    }

    private val cardAccount = AccountId(loc.cardAccount)
    private val savingsAccount = AccountId(loc.savingsAccount)

    private val catSalary = CategoryId(loc.salaryCategory)
    private val catBonus = CategoryId(loc.bonusCategory)
    private val catTax = CategoryId(loc.taxCategory)
    private val catFood = CategoryId(loc.foodCategory)
    private val catHome = CategoryId(loc.homeCategory)
    private val catClothes = CategoryId(loc.clothesCategory)
    private val catLeisure = CategoryId(loc.leisureCategory)
    private val catUtilities = CategoryId(loc.utilitiesCategory)
    private val catSubscriptions = CategoryId(loc.subscriptionsCategory)
    private val catHealth = CategoryId(loc.healthCategory)
    private val catTransport = CategoryId(loc.transportCategory)
    private val catRent = CategoryId(loc.rentCategory)
    private val catGifts = CategoryId(loc.giftsCategory)
    private val catCar = CategoryId(loc.carCategory)
    private val catTransfer = CategoryId(loc.transferCategory)

    private val foodFixed = ProductGroup(
        listOf(
            Product(loc.bread, Pricing.Fixed(249)),
            Product(loc.milk, Pricing.Fixed(189)),
            Product(loc.eggs, Pricing.Fixed(399)),
            Product(loc.cheese, Pricing.Fixed(549)),
            Product(loc.butter, Pricing.Fixed(329)),
            Product(loc.yogurt, Pricing.Fixed(149)),
            Product(loc.pasta, Pricing.Fixed(199)),
            Product(loc.rice, Pricing.Fixed(349)),
            Product(loc.tea, Pricing.Fixed(499)),
            Product(loc.coffee, Pricing.Fixed(899)),
            Product(loc.sugar, Pricing.Fixed(229)),
            Product(loc.oil, Pricing.Fixed(449)),
            Product(loc.chickenFillet, Pricing.Fixed(799)),
            Product(loc.sausages, Pricing.Fixed(549)),
            Product(loc.ketchup, Pricing.Fixed(299)),
            Product(loc.jam, Pricing.Fixed(399)),
            Product(loc.cookies, Pricing.Fixed(249)),
            Product(loc.chocolate, Pricing.Fixed(399)),
            Product(loc.chips, Pricing.Fixed(299)),
            Product(loc.soda, Pricing.Fixed(199)),
            Product(loc.juice, Pricing.Fixed(399)),
        )
    )

    private val foodWeight = ProductGroup(
        listOf(
            Product(loc.potatoes, Pricing.Weight(350)),
            Product(loc.apples, Pricing.Weight(450)),
            Product(loc.tomatoes, Pricing.Weight(300)),
            Product(loc.bananas, Pricing.Weight(250)),
            Product(loc.oranges, Pricing.Weight(400)),
            Product(loc.cabbage, Pricing.Weight(200)),
            Product(loc.carrots, Pricing.Weight(200)),
            Product(loc.onions, Pricing.Weight(150)),
            Product(loc.meat, Pricing.Weight(1200)),
            Product(loc.fish, Pricing.Weight(1000)),
            Product(loc.cheeseByWeight, Pricing.Weight(700)),
            Product(loc.sweetsBulk, Pricing.Weight(500)),
        )
    )

    private val homeSmall = ProductGroup(
        listOf(
            Product(loc.soap, Pricing.Fixed(349)),
            Product(loc.toothpaste, Pricing.Fixed(499)),
            Product(loc.shampoo, Pricing.Fixed(699)),
            Product(loc.detergent, Pricing.Fixed(899)),
            Product(loc.sponges, Pricing.Fixed(249)),
            Product(loc.toiletPaper, Pricing.Fixed(599)),
            Product(loc.lightBulbs, Pricing.Fixed(499)),
            Product(loc.trashBags, Pricing.Fixed(399)),
        )
    )

    private val homeLarge = ProductGroup(
        listOf(
            Product(loc.pot, Pricing.Range(2500, 6000)),
            Product(loc.plates, Pricing.Range(1500, 4000)),
            Product(loc.cups, Pricing.Range(1000, 3000)),
            Product(loc.towels, Pricing.Range(1500, 3500)),
            Product(loc.lamp, Pricing.Range(3000, 8000)),
            Product(loc.curtains, Pricing.Range(3000, 10000)),
            Product(loc.chair, Pricing.Range(5000, 20000)),
            Product(loc.bedLinen, Pricing.Range(3000, 8000)),
        )
    )

    private val clothingProds = ProductGroup(
        listOf(
            Product(loc.socks, Pricing.Range(800, 2000)),
            Product(loc.tshirt, Pricing.Range(1500, 4000)),
            Product(loc.shirt, Pricing.Range(3000, 8000)),
            Product(loc.jeans, Pricing.Range(4000, 10000)),
            Product(loc.jacket, Pricing.Range(8000, 25000)),
            Product(loc.sweater, Pricing.Range(3000, 8000)),
            Product(loc.boots, Pricing.Range(6000, 15000)),
            Product(loc.sneakers, Pricing.Range(5000, 12000)),
        )
    )

    private val healthProds = ProductGroup(
        listOf(
            Product(loc.painkillers, Pricing.Fixed(799)),
            Product(loc.vitamins, Pricing.Fixed(1499)),
            Product(loc.bandAids, Pricing.Fixed(399)),
            Product(loc.coldMedicine, Pricing.Fixed(999)),
            Product(loc.doctorVisit, Pricing.Range(5000, 20000)),
            Product(loc.dentistVisit, Pricing.Range(8000, 30000)),
        )
    )

    private val stores = listOf(
        StoreDef.MultiItem(
            name = loc.cheapSupermarket,
            weekdayProb = 0.25,
            weekendProb = 0.30,
            markup = 0.85,
            pools = listOf(foodFixed to catFood, foodWeight to catFood, homeSmall to catHome),
            poolWeights = listOf(1.0, 0.7, 0.2),
            minItems = 1,
            maxItems = 12,
        ),
        StoreDef.MultiItem(
            name = loc.midSupermarket,
            weekdayProb = 0.20,
            weekendProb = 0.25,
            markup = 1.0,
            pools = listOf(foodFixed to catFood, foodWeight to catFood, homeSmall to catHome),
            poolWeights = listOf(1.0, 0.8, 0.3),
            minItems = 1,
            maxItems = 15,
        ),
        StoreDef.MultiItem(
            name = loc.expensiveSupermarket,
            weekdayProb = 0.08,
            weekendProb = 0.12,
            markup = 1.25,
            pools = listOf(foodFixed to catFood, foodWeight to catFood, homeSmall to catHome),
            poolWeights = listOf(1.0, 0.6, 0.25),
            minItems = 1,
            maxItems = 8,
        ),
        StoreDef.MultiItem(
            name = loc.cheapClothingStore,
            weekdayProb = 0.04,
            weekendProb = 0.10,
            markup = 0.8,
            pools = listOf(clothingProds to catClothes),
            poolWeights = listOf(1.0),
            minItems = 1,
            maxItems = 4,
        ),
        StoreDef.MultiItem(
            name = loc.expensiveClothingStore,
            weekdayProb = 0.02,
            weekendProb = 0.06,
            markup = 1.5,
            pools = listOf(clothingProds to catClothes),
            poolWeights = listOf(1.0),
            minItems = 1,
            maxItems = 3,
        ),
        StoreDef.MultiItem(
            name = loc.homeStore,
            weekdayProb = 0.04,
            weekendProb = 0.08,
            markup = 1.0,
            pools = listOf(homeLarge to catHome, homeSmall to catHome),
            poolWeights = listOf(0.7, 0.3),
            minItems = 1,
            maxItems = 6,
        ),
        StoreDef.SingleAmount(
            name = loc.cheapRestaurant,
            weekdayProb = 0.15,
            weekendProb = 0.10,
            markup = 0.85,
            categoryId = catFood,
            minCents = 500,
            maxCents = 1500,
        ),
        StoreDef.SingleAmount(
            name = loc.expensiveRestaurant,
            weekdayProb = 0.04,
            weekendProb = 0.10,
            markup = 1.6,
            categoryId = catFood,
            minCents = 2000,
            maxCents = 8000,
        ),
        StoreDef.SingleAmount(
            name = loc.cinema,
            weekdayProb = 0.02,
            weekendProb = 0.08,
            markup = 1.0,
            categoryId = catLeisure,
            minCents = 1000,
            maxCents = 1500,
        ),
        StoreDef.SingleAmount(
            name = loc.theatre,
            weekdayProb = 0.01,
            weekendProb = 0.04,
            markup = 1.0,
            categoryId = catLeisure,
            minCents = 3000,
            maxCents = 8000,
        ),
        StoreDef.SingleAmount(
            name = loc.bar,
            weekdayProb = 0.03,
            weekendProb = 0.10,
            markup = 1.0,
            categoryId = catLeisure,
            minCents = 2000,
            maxCents = 6000,
        ),
        StoreDef.SingleAmount(
            name = loc.concertVenue,
            weekdayProb = 0.01,
            weekendProb = 0.04,
            markup = 1.0,
            categoryId = catLeisure,
            minCents = 4000,
            maxCents = 15000,
        ),
        StoreDef.MultiItem(
            name = loc.pharmacy,
            weekdayProb = 0.05,
            weekendProb = 0.03,
            markup = 1.0,
            pools = listOf(healthProds to catHealth),
            poolWeights = listOf(1.0),
            minItems = 1,
            maxItems = 3,
        ),
        StoreDef.SingleAmount(
            name = loc.gasStation,
            weekdayProb = 0.08,
            weekendProb = 0.12,
            markup = 1.0,
            categoryId = catCar,
            minCents = 3000,
            maxCents = 6000,
        ),
        StoreDef.SingleAmount(
            name = loc.busFare,
            weekdayProb = 0.15,
            weekendProb = 0.05,
            markup = 1.0,
            categoryId = catTransport,
            minCents = 200,
            maxCents = 500,
        ),
        StoreDef.SingleAmount(
            name = loc.taxiRide,
            weekdayProb = 0.05,
            weekendProb = 0.08,
            markup = 1.0,
            categoryId = catTransport,
            minCents = 1500,
            maxCents = 4000,
        ),
    )

    private fun centsToAmountExpr(cents: Long): AmountExpression {
        val bd = cents.toBigDecimal().div(100.toBigDecimal())
        return AmountExpression(Expression.Value(bd))
    }

    private fun generatePriceCents(product: Product, markup: Double, inflation: Double): Long {
        return when (val p = product.pricing) {
            is Pricing.Fixed -> {
                val base = p.cents * markup * inflation
                val noise = base * 0.03
                val raw = nextNormal(base, noise)
                val dollars = (raw / 100).toLong()
                val withPsych = dollars * 100 + 49 + if (shouldHappen(0.5)) 50 else 0
                if (withPsych >= (base * 0.7).toLong() && withPsych <= (base * 1.35).toLong()) withPsych
                else raw.toLong().coerceAtLeast(1)
            }
            is Pricing.Weight -> {
                val mean = p.meanCents * markup * inflation
                val std = mean * 0.28
                nextNormal(mean, std).toLong().coerceAtLeast(1)
            }
            is Pricing.Range -> {
                val min = p.minCents * markup * inflation
                val max = p.maxCents * markup * inflation
                nextBetween(min, max).toLong()
            }
        }
    }

    private fun mkRecord(
        category: CategoryId,
        comment: String,
        amountCents: Long,
        isExpense: Boolean = true,
    ): Record = Record(
        category = category,
        amount = centsToAmountExpr(if (isExpense) -amountCents else amountCents),
        comment = Comment(comment),
    )

    private fun mkEntry(
        timestamp: Instant,
        account: AccountId,
        records: List<Record>,
        comment: String,
    ): Transaction = Transaction(
        timestamp = timestamp,
        comment = Comment(comment),
        type = Transaction.Type.Entry(
            account = account,
            records = records.toNonEmptyListOrNull()!!,
        ),
    )

    private fun mkTransfer(
        timestamp: Instant,
        from: AccountId,
        to: AccountId,
        amountCents: Long,
        comment: String,
    ): Transaction = Transaction(
        timestamp = timestamp,
        comment = Comment(comment),
        type = Transaction.Type.Transfer(
            from = from,
            to = to,
            amount = centsToAmountExpr(amountCents),
        ),
    )

    private fun generateConfigUpdates(): List<UpdateType> = buildList {
        add(
            UpdateType.Config(
                config = BudgetConfig(
                    title = loc.budgetTitle,
                    currency = null,
                    sync = BudgetConfig.Sync.empty,
                )
            )
        )
        add(
            UpdateType.AccountConfig(
                id = cardAccount,
                config = AccountConfig(
                    title = loc.cardAccount,
                    hue = Hue(210),
                    icon = IconVariant.CreditCard.icon,
                )
            )
        )
        add(
            UpdateType.AccountConfig(
                id = savingsAccount,
                config = AccountConfig(
                    title = loc.savingsAccount,
                    hue = Hue(130),
                    icon = IconVariant.Savings.icon,
                )
            )
        )
        val categoryConfigs = listOf(
            catSalary to Pair(Hue(120), IconVariant.Work.icon),
            catBonus to Pair(Hue(50), IconVariant.Star.icon),
            catTax to Pair(Hue(0), IconVariant.Percent.icon),
            catFood to Pair(Hue(35), IconVariant.ShoppingCart.icon),
            catHome to Pair(Hue(200), IconVariant.Home.icon),
            catClothes to Pair(Hue(280), IconVariant.Checkroom.icon),
            catLeisure to Pair(Hue(320), IconVariant.ConfirmationNumber.icon),
            catUtilities to Pair(Hue(45), IconVariant.ElectricBolt.icon),
            catSubscriptions to Pair(Hue(190), IconVariant.Repeat.icon),
            catHealth to Pair(Hue(350), IconVariant.Favorite.icon),
            catTransport to Pair(Hue(25), IconVariant.DirectionsBus.icon),
            catRent to Pair(Hue(170), IconVariant.Apartment.icon),
            catGifts to Pair(Hue(310), IconVariant.CardGiftcard.icon),
            catCar to Pair(Hue(15), IconVariant.DirectionsCar.icon),
            catTransfer to Pair(Hue(260), IconVariant.SwapHoriz.icon),
        )
        for ((cat, cfg) in categoryConfigs) {
            add(
                UpdateType.CategoryConfig(
                    id = cat,
                    config = CategoryConfig(
                        title = cat.id,
                        hue = cfg.first,
                        icon = cfg.second,
                    ),
                )
            )
        }
    }

    private fun generateEmployment(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Job> {
        val jobs = mutableListOf<Job>()
        var current = startDate
        var careerLevel = 0
        var lastSalary: Double? = null

        while (current < endDate) {
            val unemployedGap = if (jobs.isEmpty()) nextInt(0, 2) else nextInt(1, 5)
            val gapEnd = current.plus(unemployedGap.toLong(), DateTimeUnit.MONTH)
            val clampedGapEnd = if (gapEnd > endDate) endDate else gapEnd

            if (clampedGapEnd < endDate) {
                val jobStart = clampedGapEnd
                val jobDurationMonths = nextInt(8, 60)
                val jobEnd = jobStart.plus(jobDurationMonths.toLong(), DateTimeUnit.MONTH)
                val clampedJobEnd = if (jobEnd > endDate) endDate else jobEnd

                val baseMin = 3000.0 + careerLevel * 200
                val baseMax = 5500.0 + careerLevel * 400
                val salary = lastSalary?.let {
                    it * nextBetween(0.85, 1.25)
                } ?: nextBetween(baseMin, baseMax)

                jobs.add(Job(from = jobStart, to = clampedJobEnd, monthlySalary = salary))
                lastSalary = salary
                careerLevel++
                current = clampedJobEnd
            } else {
                current = endDate
            }
        }

        return jobs
    }

    private fun findJob(date: LocalDate, jobs: List<Job>): Job? =
        jobs.firstOrNull { date >= it.from && date < it.to }

    private data class ScheduledBill(
        val date: LocalDate,
        val category: CategoryId,
        val name: String,
        val minCents: Long,
        val maxCents: Long,
    )

    private fun generateScheduledBillDays(
        start: LocalDate,
        end: LocalDate,
    ): List<ScheduledBill> {
        val result = mutableListOf<ScheduledBill>()
        var date = start
        while (date < end) {
            val y = date.year
            val m = date.month
            val daysInMonth = monthLength(y, monthNumber(m))

            val eDay = nextInt(5, 8).coerceAtMost(daysInMonth)
            val wDay = nextInt(12, 18).coerceAtMost(daysInMonth)
            val hDay = nextInt(15, 22).coerceAtMost(daysInMonth)
            val iDay = nextInt(3, 7).coerceAtMost(daysInMonth)
            val pDay = nextInt(8, 12).coerceAtMost(daysInMonth)
            val wasteDay = nextInt(18, 24).coerceAtMost(daysInMonth)

            val rate = config.currencyRate

            result.add(ScheduledBill(
                LocalDate(y, m, eDay), catUtilities, loc.electricity,
                (5000 * rate).toLong(), (6500 * rate).toLong(),
            ))
            result.add(ScheduledBill(
                LocalDate(y, m, wDay), catUtilities, loc.water,
                (2500 * rate).toLong(), (3000 * rate).toLong(),
            ))
            result.add(ScheduledBill(
                LocalDate(y, m, hDay), catUtilities, loc.heating,
                (6000 * rate).toLong(), (8400 * rate).toLong(),
            ))
            result.add(ScheduledBill(
                LocalDate(y, m, iDay), catUtilities, loc.internet,
                (5000 * rate).toLong(), (5500 * rate).toLong(),
            ))
            result.add(ScheduledBill(
                LocalDate(y, m, pDay), catUtilities, loc.phone,
                (2500 * rate).toLong(), (2750 * rate).toLong(),
            ))
            result.add(ScheduledBill(
                LocalDate(y, m, wasteDay), catUtilities, loc.wasteDisposal,
                (800 * rate).toLong(), (920 * rate).toLong(),
            ))

            date = LocalDate(y + 1, m, 1)
        }
        return result
    }

    private fun generateRentDays(
        start: LocalDate,
        end: LocalDate,
    ): List<ScheduledBill> {
        val result = mutableListOf<ScheduledBill>()
        var date = start
        while (date < end) {
            val y = date.year
            val m = date.month
            val daysInMonth = monthLength(y, monthNumber(m))
            val rentDay = nextInt(1, 5).coerceAtMost(daysInMonth)
            val rate = config.currencyRate
            val rentDollars = nextBetween(800.0, 2000.0)
            result.add(ScheduledBill(
                LocalDate(y, m, rentDay), catRent, loc.rent,
                (rentDollars * 100 * rate).toLong(),
                (rentDollars * 105 * rate).toLong(),
            ))
            date = date.plus(1, DateTimeUnit.MONTH)
        }
        return result
    }

    private fun generateSubscriptionDays(
        start: LocalDate,
        end: LocalDate,
    ): List<ScheduledBill> {
        val result = mutableListOf<ScheduledBill>()
        val rate = config.currencyRate
        val subDefs = listOf(
            Triple(loc.streamingService, 1000.0, 1500.0),
            Triple(loc.gymMembership, 3000.0, 6000.0),
            Triple(loc.musicService, 500.0, 1000.0),
            Triple(loc.cloudStorage, 300.0, 1000.0),
        )
        var date = start
        while (date < end) {
            val y = date.year
            val m = date.month
            val daysInMonth = monthLength(y, monthNumber(m))
            for ((name, minC, maxC) in subDefs) {
                val subDay = nextInt(1, 28).coerceAtMost(daysInMonth)
                result.add(ScheduledBill(
                    LocalDate(y, m, subDay), catSubscriptions, name,
                    (minC * rate).toLong(), (maxC * rate).toLong(),
                ))
            }
            date = date.plus(1, DateTimeUnit.MONTH)
        }
        return result
    }

    private fun generatePaydays(
        job: Job,
        paydays: MutableSet<LocalDate>,
        end: LocalDate,
    ) {
        var payMonth = LocalDate(job.from.year, job.from.month, 1)
        val endMonth = LocalDate(job.to.year, job.to.month, 1)
        while (payMonth <= endMonth && payMonth < end) {
            val daysInMonth = monthLength(payMonth.year, monthNumber(payMonth.month))
            val center = 15.coerceAtMost(daysInMonth)
            var payDay = center + nextInt(-3, 3)
            payDay = payDay.coerceIn(1, daysInMonth)
            val paydate = LocalDate(payMonth.year, payMonth.month, payDay)
            if (paydate >= job.from && paydate < job.to) {
                paydays.add(paydate)
            }
            payMonth = payMonth.plus(1, DateTimeUnit.MONTH)
        }
    }

    private fun generateBonusMonths(
        start: LocalDate,
        end: LocalDate,
        jobs: List<Job>,
    ): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var year = start.year
        while (year <= end.year) {
            val bonusDay = nextInt(15, 25).coerceAtMost(31)
            val bonusDate = LocalDate(year, Month.DECEMBER, bonusDay)
            val employedAtBonus = jobs.any { bonusDate >= it.from && bonusDate < it.to }
            if (employedAtBonus && bonusDate >= start && bonusDate < end && shouldHappen(0.7)) {
                result.add(bonusDate)
            }
            year++
        }
        return result
    }

    private fun generateVacationPeriods(
        jobs: List<Job>,
    ): List<Pair<LocalDate, LocalDate>> {
        val result = mutableListOf<Pair<LocalDate, LocalDate>>()
        for (job in jobs) {
            val jobDuration = daysBetween(job.from, job.to)
            val yearsInJob = jobDuration / 365
            for (yi in 0 until yearsInJob.coerceAtLeast(1)) {
                if (shouldHappen(0.6)) continue
                val vacStartMonth = nextInt(5, 9)
                val vacYear = job.from.year + yi
                val vacStart = LocalDate(vacYear, Month.entries[vacStartMonth - 1], nextInt(1, 20))
                if (vacStart >= job.from && vacStart < job.to) {
                    val vacDuration = nextInt(7, 14)
                    val vacEnd = vacStart.plus(vacDuration.toLong(), DateTimeUnit.DAY)
                    val effectiveEnd = if (vacEnd >= job.to) job.to else vacEnd
                    result.add(vacStart to effectiveEnd)
                }
            }
        }
        return result
    }

    private fun generateLargePurchases(
        start: LocalDate,
        end: LocalDate,
    ): List<ScheduledBill> {
        val result = mutableListOf<ScheduledBill>()
        val totalDays = daysBetween(start, end).coerceAtLeast(1)
        val numPurchases = nextInt(totalDays / 80, totalDays / 60)
        val rate = config.currencyRate
        for (i in 0 until numPurchases) {
            val offset = nextInt(0, totalDays - 1)
            val date = start.plus(offset.toLong(), DateTimeUnit.DAY)
            val category = when {
                shouldHappen(0.5) -> catHome
                shouldHappen(0.5) -> catClothes
                else -> catLeisure
            }
            val (minC, maxC) = when (category) {
                catHome -> Pair(15000.0, 80000.0)
                catClothes -> Pair(10000.0, 40000.0)
                else -> Pair(20000.0, 120000.0)
            }
            result.add(ScheduledBill(date, category, loc.bigPurchaseComment,
                (minC * rate).toLong(), (maxC * rate).toLong()))
        }
        return result
    }

    fun generate(start: Instant, end: Instant): List<UpdateType> {
        val startDate = start.toLocalDate()
        val endDate = end.toLocalDate()
        val jobs = generateEmployment(startDate, endDate)

        val configUpdates = generateConfigUpdates()
        val transactions = mutableListOf<Transaction>()

        val balance = BalanceTracker()
        val rate = config.currencyRate
        balance.savings = (5000 * rate).toLong()

        var inflationFactor = 1.0
        var lastInflationYear = startDate.year

        val scheduledBills = generateScheduledBillDays(startDate, endDate)
        val rentDays = generateRentDays(startDate, endDate)
        val subscriptionDays = generateSubscriptionDays(startDate, endDate)
        val bonusDays = generateBonusMonths(startDate, endDate, jobs)
        val vacationPeriods = generateVacationPeriods(jobs)
        val largePurchases = generateLargePurchases(startDate, endDate)

        var daysSinceGas = nextInt(0, 8)
        var daysSinceCarWash = nextInt(0, 20)
        var lastCarInsuranceYear = -1

        val paydays = mutableSetOf<LocalDate>()
        for (job in jobs) {
            generatePaydays(job, paydays, endDate)
        }
        val sortedPaydays = paydays.toList().sorted()
        val paydaysSet = paydays.toSet()

        val bonusSet = bonusDays.toSet()

        var currentDate = startDate
        while (currentDate < endDate) {
            if (currentDate.year != lastInflationYear) {
                val yearlyNoise = nextBetween(-0.01, 0.01)
                inflationFactor *= (1.0 + config.annualInflation + yearlyNoise)
                lastInflationYear = currentDate.year
            }

            val monthNumber = monthNumber(currentDate.month)
            val job = findJob(currentDate, jobs)
            val isEmployed = job != null
            val isWeekendDay = currentDate.isWeekend()
            val isPayday = currentDate in paydaysSet
            val isBonusDay = currentDate in bonusSet

            val nextPaydayIdx = sortedPaydays.indexOfFirst { it > currentDate }
            val nextPayday = if (nextPaydayIdx >= 0) sortedPaydays[nextPaydayIdx] else null
            val isDayBeforePayday = nextPayday != null &&
                    currentDate == nextPayday.plus(-1, DateTimeUnit.DAY)

            val inVacation = vacationPeriods.any { currentDate >= it.first && currentDate <= it.second }

            val dayTimestamp = randomTimestamp(currentDate, 6, 23)

            if (isPayday && job != null) {
                val salaryCents = (job.monthlySalary * 100.0 * rate * inflationFactor).toLong()
                val taxCents = (salaryCents * config.taxRate).toLong()
                val netSalary = salaryCents - taxCents

                job.monthlySalary *= (1.0 + nextBetween(0.001, 0.006))

                balance.card += netSalary

                transactions.add(
                    mkEntry(
                        timestamp = dayTimestamp,
                        account = cardAccount,
                        records = listOf(mkRecord(catSalary, "", salaryCents, isExpense = false)),
                        comment = if (shouldHappen(0.05)) loc.salaryComment else "",
                    )
                )
                if (taxCents > 0) {
                    transactions.add(
                        mkEntry(
                            timestamp = randomTimestamp(currentDate, 9, 12),
                            account = cardAccount,
                            records = listOf(mkRecord(catTax, loc.taxComment, taxCents)),
                            comment = "",
                        )
                    )
                }
            }

            if (isBonusDay && isEmployed && job != null) {
                val bonusMultiplier = nextBetween(0.5, 1.5)
                val bonusCents = (job.monthlySalary * 100.0 * rate * bonusMultiplier).toLong()
                balance.card += bonusCents
                transactions.add(
                    mkEntry(
                        timestamp = dayTimestamp,
                        account = cardAccount,
                        records = listOf(mkRecord(catBonus, loc.bonusComment, bonusCents, isExpense = false)),
                        comment = "",
                    )
                )
            }

            for (bill in scheduledBills) {
                if (bill.date != currentDate) continue
                val amountCents = nextBetween(
                    bill.minCents.toDouble() * inflationFactor,
                    bill.maxCents.toDouble() * inflationFactor,
                ).toLong()
                val adjustedCents = if (bill.category == catUtilities &&
                    (monthNumber in 10..12 || monthNumber in 1..3)) {
                    (amountCents.toDouble() * nextBetween(1.3, 1.8)).toLong()
                } else {
                    amountCents
                }
                val actual = minOf(balance.card, adjustedCents)
                if (actual > 0) {
                    balance.card -= actual
                    transactions.add(
                        mkEntry(
                            timestamp = randomTimestamp(currentDate, 9, 18),
                            account = cardAccount,
                            records = listOf(mkRecord(bill.category, bill.name, actual)),
                            comment = "",
                        )
                    )
                }
            }

            for (rent in rentDays) {
                if (rent.date != currentDate) continue
                val amountCents = nextBetween(
                    rent.minCents.toDouble() * inflationFactor,
                    rent.maxCents.toDouble() * inflationFactor,
                ).toLong()
                val actual = minOf(balance.card, amountCents)
                if (actual > 0) {
                    balance.card -= actual
                    transactions.add(
                        mkEntry(
                            timestamp = randomTimestamp(currentDate, 8, 12),
                            account = cardAccount,
                            records = listOf(mkRecord(catRent, loc.rent, actual)),
                            comment = "",
                        )
                    )
                }
            }

            for (sub in subscriptionDays) {
                if (sub.date != currentDate) continue
                val amountCents = nextBetween(
                    sub.minCents.toDouble() * inflationFactor,
                    sub.maxCents.toDouble() * inflationFactor,
                ).toLong()
                val actual = minOf(balance.card, amountCents)
                if (actual > 0) {
                    balance.card -= actual
                    transactions.add(
                        mkEntry(
                            timestamp = randomTimestamp(currentDate, 0, 23),
                            account = cardAccount,
                            records = listOf(mkRecord(catSubscriptions, sub.name, actual)),
                            comment = "",
                        )
                    )
                }
            }

            for (lp in largePurchases) {
                if (lp.date != currentDate) continue
                val amountCents = nextBetween(
                    lp.minCents.toDouble() * inflationFactor,
                    lp.maxCents.toDouble() * inflationFactor,
                ).toLong()
                val savingsFloor = (200.0 * 100 * rate).toLong()
                val maxSpend = maxOf(balance.savings - savingsFloor, 0L)
                val actual = minOf(maxSpend, amountCents)
                if (actual > 0) {
                    balance.savings -= actual
                    transactions.add(
                        mkEntry(
                            timestamp = dayTimestamp,
                            account = savingsAccount,
                            records = listOf(mkRecord(lp.category, loc.bigPurchaseComment, actual)),
                            comment = loc.bigPurchaseComment,
                        )
                    )
                }
            }

            daysSinceGas++
            if (daysSinceGas >= nextInt(4, 10) && shouldHappen(0.7)) {
                val gasCents = nextBetween(
                    3000.0 * inflationFactor * rate,
                    6000.0 * inflationFactor * rate,
                ).toLong()
                val actual = minOf(balance.card, gasCents)
                if (actual > 0) {
                    balance.card -= actual
                    daysSinceGas = 0
                    transactions.add(
                        mkEntry(
                            timestamp = randomTimestamp(currentDate, 7, 21),
                            account = cardAccount,
                            records = listOf(mkRecord(catCar, loc.gas, actual)),
                            comment = "",
                        )
                    )
                }
            }

            daysSinceCarWash++
            if (daysSinceCarWash >= nextInt(25, 50) && shouldHappen(0.6)) {
                val washCents = nextBetween(
                    800.0 * inflationFactor * rate,
                    2000.0 * inflationFactor * rate,
                ).toLong()
                val actual = minOf(balance.card, washCents)
                if (actual > 0) {
                    balance.card -= actual
                    daysSinceCarWash = 0
                    transactions.add(
                        mkEntry(
                            timestamp = randomTimestamp(currentDate, 8, 19),
                            account = cardAccount,
                            records = listOf(mkRecord(catCar, loc.carWash, actual)),
                            comment = "",
                        )
                    )
                }
            }

            if (monthNumber != lastCarInsuranceYear && (monthNumber == 3 || monthNumber == 9)) {
                if (shouldHappen(0.5)) {
                    val insuranceCents = nextBetween(
                        50000.0 * inflationFactor * rate,
                        100000.0 * inflationFactor * rate,
                    ).toLong()
                    val actual = minOf(balance.card, insuranceCents)
                    if (actual > 0) {
                        balance.card -= actual
                        lastCarInsuranceYear = monthNumber
                        transactions.add(
                            mkEntry(
                                timestamp = randomTimestamp(currentDate, 9, 17),
                                account = cardAccount,
                                records = listOf(mkRecord(catCar, loc.carInsurance, actual)),
                                comment = "",
                            )
                        )
                    }
                }
            }

            if (shouldHappen(0.003)) {
                val repairCents = nextBetween(
                    15000.0 * inflationFactor * rate,
                    50000.0 * inflationFactor * rate,
                ).toLong()
                val actual = minOf(balance.card, repairCents)
                if (actual > 0) {
                    balance.card -= actual
                    transactions.add(
                        mkEntry(
                            timestamp = randomTimestamp(currentDate, 8, 18),
                            account = cardAccount,
                            records = listOf(mkRecord(catCar, loc.carRepair, actual)),
                            comment = "",
                        )
                    )
                }
            }

            if (isDayBeforePayday) {
                val buffer = (700.0 * 100 * rate * inflationFactor).toLong()
                val transferAmount = maxOf(balance.card - buffer, 0L)
                if (transferAmount > 0) {
                    balance.card -= transferAmount
                    balance.savings += transferAmount
                    transactions.add(
                        mkTransfer(
                            timestamp = randomTimestamp(currentDate, 18, 22),
                            from = cardAccount,
                            to = savingsAccount,
                            amountCents = transferAmount,
                            comment = loc.transferToSavingsComment,
                        )
                    )
                }
            }

            val probBase = 1.0
            val probWeekend = if (isWeekendDay) 0.3 else 0.0
            val probEmployed = if (isEmployed) 0.2 else 0.0
            val probVacation = if (inVacation) 0.25 else 0.0
            val probDecember = if (monthNumber == 12) 0.12 else 0.0

            val probMultiplier = (probBase + probWeekend + probEmployed + probVacation + probDecember)
                .coerceIn(0.4, 2.2)
            val meanVisits = 2.8 * probMultiplier
            val numVisits = nextPoisson(meanVisits).coerceIn(0, 8)

            for (v in 0 until numVisits) {
                val probMap = stores.map { store ->
                    var prob = if (isWeekendDay) store.weekendProb else store.weekdayProb
                    if (isEmployed && !inVacation && store.name == loc.cheapRestaurant) {
                        prob *= 1.3
                    }
                    if (inVacation) {
                        if (store is StoreDef.SingleAmount &&
                            (store.categoryId == catLeisure || store.categoryId == catFood)) {
                            prob *= 1.6
                        }
                    }
                    if (monthNumber == 12 && store.name == loc.expensiveSupermarket) {
                        prob *= 1.4
                    }
                    if (store.name == loc.busFare && !isEmployed) {
                        prob *= 0.3
                    }
                    store to prob
                }
                val totalWeight = probMap.sumOf { it.second }
                if (totalWeight <= 0.0) continue

                val picked = weightedPick(probMap.map { it.first to it.second })
                val visitTimestamp = randomTimestamp(currentDate, 7, 22)

                when (picked) {
                    is StoreDef.MultiItem -> {
                        val numItems = nextInt(picked.minItems, picked.maxItems)
                        val records = mutableListOf<Record>()
                        var itemsTotal = 0L
                        for (i in 0 until numItems) {
                            val poolIdx = weightedPick(
                                picked.pools.indices.map { idx ->
                                    idx to picked.poolWeights[idx]
                                }
                            )
                            val (group, categoryId) = picked.pools[poolIdx]
                            if (group.products.isEmpty()) continue
                            val product = group.products.random(random)
                            val priceCents = generatePriceCents(product, picked.markup, inflationFactor)
                            val localPrice = maxOf((priceCents.toDouble() * rate).toLong(), 1L)
                            itemsTotal += localPrice
                            records.add(mkRecord(categoryId, product.name, localPrice))
                        }
                        if (records.isNotEmpty()) {
                            val totalCost = itemsTotal
                            val fromCard = minOf(balance.card, totalCost)
                            if (fromCard > 0 && fromCard >= totalCost) {
                                balance.card -= totalCost
                                transactions.add(
                                    mkEntry(visitTimestamp, cardAccount, records, picked.name)
                                )
                            } else if (totalCost > (100.0 * 100 * rate).toLong()) {
                                val savingsFloor = (200.0 * 100 * rate).toLong()
                                val maxSpend = maxOf(balance.savings - savingsFloor, 0L)
                                if (maxSpend >= totalCost) {
                                    balance.savings -= totalCost
                                    transactions.add(
                                        mkEntry(visitTimestamp, savingsAccount, records, picked.name)
                                    )
                                }
                            }
                        }
                    }
                    is StoreDef.SingleAmount -> {
                        val priceCents = nextBetween(
                            picked.minCents.toDouble() * inflationFactor * rate,
                            picked.maxCents.toDouble() * inflationFactor * rate,
                        ).toLong().coerceAtLeast(1)
                        val fromCard = minOf(balance.card, priceCents)
                        if (fromCard > 0 && fromCard >= priceCents) {
                            balance.card -= priceCents
                            transactions.add(
                                mkEntry(
                                    visitTimestamp, cardAccount,
                                    listOf(mkRecord(picked.categoryId, picked.name, priceCents)),
                                    if (shouldHappen(0.15)) picked.name else "",
                                )
                            )
                        }
                    }
                    is StoreDef.LargePurchase -> {}
                }
            }

            if (shouldHappen(0.015) && isEmployed) {
                val giftCategory = if (shouldHappen(0.5)) catGifts else catLeisure
                val giftCents = nextBetween(
                    2000.0 * inflationFactor * rate,
                    8000.0 * inflationFactor * rate,
                ).toLong()
                val fromCard = minOf(balance.card, giftCents)
                if (fromCard > 0 && fromCard >= giftCents) {
                    balance.card -= giftCents
                    val giftName = if (shouldHappen(0.6)) loc.birthdayGift else loc.holidayGift
                    transactions.add(
                        mkEntry(
                            timestamp = randomTimestamp(currentDate, 10, 20),
                            account = cardAccount,
                            records = listOf(mkRecord(giftCategory, giftName, giftCents)),
                            comment = if (shouldHappen(0.5)) loc.giftComment else "",
                        )
                    )
                }
            }

            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        }

        val sortedTransactions = transactions.sortedBy { it.timestamp }

        return configUpdates + sortedTransactions.map { tx ->
            UpdateType.Transaction(
                id = Transaction.Id.new(),
                transaction = tx,
            )
        }
    }
}
