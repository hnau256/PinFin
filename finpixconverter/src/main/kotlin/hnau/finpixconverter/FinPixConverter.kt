package hnau.finpixconverter

import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.ifNull
import hnau.pinfin.repository.dto.AccountId
import hnau.pinfin.repository.dto.Amount
import hnau.pinfin.repository.dto.CategoryDirection
import hnau.pinfin.repository.dto.CategoryId
import hnau.pinfin.repository.dto.Comment
import hnau.pinfin.repository.dto.Transaction
import hnau.pinfin.repository.dto.UpdateType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigDecimal
import java.nio.charset.Charset
import kotlin.uuid.Uuid
import kotlin.jvm.JvmInline

enum class FinPixAccount(
    val key: String,
    val exportKey: String? = null,
) {
    Cash(
        key = "Наличка",
    ),
    CardFreedomFinance(
        key = "Карточка freedom finance",
        exportKey = "Freedom finance",
    ),
    CardBankOfCyprus(
        key = "Карточка Bank of Cyprus",
        exportKey = "Τράπεζα Κύπρου",
    ),
    ;

    val accountId: AccountId
        get() = AccountId(exportKey ?: key)

    companion object {


        val parse: (RowNumber, String) -> FinPixAccount = createParser { key }
    }
}


enum class FinPixTransactionType(
    val key: String,
) {
    Outcome(
        key = "EXPENDITURE",
    ),
    Income(
        key = "INCOME",
    ),
    Transfer(
        key = "TRANSFER",
    );

    companion object {

        val parse: (RowNumber, String) -> FinPixTransactionType = createParser { key }
    }
}

@JvmInline
value class RowNumber(
    val number: Int,
)

fun throwParseError(
    rowNumber: RowNumber,
    message: String,
): Nothing = error("Error on row ${rowNumber.number}: $message")

inline fun <reified E : Enum<E>> createParser(
    extractKey: E.() -> String,
): (RowNumber, String) -> E = enumValues<E>()
    .associateBy(extractKey)
    .let { map ->
        { rowNumber, string ->
            map[string.trim()].ifNull {
                throwParseError(
                    rowNumber,
                    "Unknown ${E::class.java.simpleName} '$string'"
                )
            }
        }
    }

enum class Category(
    val key: String,
) {
    Return(
        key = "Возврат"
    ),
    Other(
        key = "Другое"
    ),
    Salary(
        key = "Зарплата"
    ),
    Gift(
        key = "Подарки"
    ),
    Bonus(
        key = "Премия"
    ),
    Home(
        key = "Для дома"
    ),
    Clothes(
        key = "Одежда"
    ),
    Tastes(
        key = "Вкусности"
    ),
    Food(
        key = "Еда"
    ),
    Transport(
        key = "Транспорт"
    ),
    Fun(
        key = "Развлечения"
    ),
    Rent(
        key = "Аренда"
    ),
    Services(
        key = "ЖКХ"
    ),
    Gifts(
        key = "Подарки"
    ),
    Helth(
        key = "Здоровье"
    ),
    Correct(
        key = "Корректировка"
    ),
    Church(
        key = "Храм"
    ),
    Learning(
        key = "Учеба"
    ),
    ;

    fun toCategoryId(
        direction: CategoryDirection,
    ): CategoryId = CategoryId(
        id = CategoryId.directionPrefixes[direction] + key,
    )

    companion object {

        val parse: (RowNumber, String) -> Category = createParser { key }
    }
}

@JvmInline
value class FinPixAmount(
    val cents: Int,
) {

    fun format(): String {
        if (cents < 0) {
            error("Unable format negative amount cents:$cents")
        }
        return ((cents / 100).toString() +
                "." +
                (cents % 100).toString().padStart(2, '0')).simplifyNumber()
    }

    val amount: Amount
        get() = Amount(
            value = cents.toUInt(),
        )

    operator fun unaryMinus() = FinPixAmount(-cents)

    companion object {

        fun parse(
            rowNumber: RowNumber,
            string: String,
        ): FinPixAmount = string
            .takeIf { it.isNotEmpty() }
            .ifNull { "0" }.toBigDecimal()
            .multiply(BigDecimal(100))
            .toInt()
            .let(::FinPixAmount)

        private fun String.simplifyNumber(): String = this
            .dropLastWhile { it == '0' }
            .removeSuffix(".")
    }
}

data class FinPixTransaction(
    val type: Type,
    val timstamp: Instant,
    val comment: String,
) {

    sealed interface Type {

        data class Income(
            val category: Category,
            val to: FinPixAccount,
            val comment: String,
            val amount: FinPixAmount,
        ) : Type

        data class Transfer(
            val from: FinPixAccount,
            val amountFrom: FinPixAmount,
            val to: FinPixAccount,
            val amountTo: FinPixAmount,
        ) : Type

        data class Outcome(
            val category: Category,
            val from: FinPixAccount,
            val comment: String,
            val amount: FinPixAmount,
        ) : Type
    }
}

private val dataDir = File("data")

operator fun Row.get(column: Int): String =
    getCell(column)?.toString() ?: ""

fun Row.getDateTime(column: Int): Instant =
    getCell(column).localDateTimeCellValue.toKotlinLocalDateTime()
        .toInstant(TimeZone.currentSystemDefault())

private fun readTransactions(): List<FinPixTransaction> = File(dataDir, "in/transactions.xlsx")
    .let(::FileInputStream)
    .use { transactionsFile ->
        XSSFWorkbook(transactionsFile).use { transactionsBook ->
            transactionsBook
                .getSheetAt(0)
                .drop(2)
                .mapIndexedNotNull { index, row ->
                    val rowNumber = RowNumber(index + 3)
                    if (row[8] == "<DELETED>") {
                        return@mapIndexedNotNull null
                    }
                    val type = FinPixTransactionType.parse(rowNumber, row[0])
                    val transactionType = when (type) {

                        FinPixTransactionType.Outcome -> FinPixTransaction.Type.Outcome(
                            category = row[13]
                                .takeIf(String::isNotEmpty)
                                ?.let { Category.parse(rowNumber, it) }
                                ?: Category.Other,
                            from = FinPixAccount.parse(rowNumber, row[9]),
                            comment = row[14].takeIf { it.isNotEmpty() } ?: row[12],
                            amount = FinPixAmount.parse(rowNumber, row[17])
                        )

                        FinPixTransactionType.Income -> FinPixTransaction.Type.Income(
                            category = row[4]
                                .takeIf(String::isNotEmpty)
                                ?.let { Category.parse(rowNumber, it) }
                                ?: Category.Food,
                            to = FinPixAccount.parse(rowNumber, row[5]),
                            comment = row[8],
                            amount = FinPixAmount.parse(rowNumber, row[7]),
                        )

                        FinPixTransactionType.Transfer -> FinPixTransaction.Type.Transfer(
                            to = FinPixAccount.parse(rowNumber, row[5]),
                            amountFrom = FinPixAmount.parse(rowNumber, row[11]),
                            from = FinPixAccount.parse(rowNumber, row[9]),
                            amountTo = FinPixAmount.parse(rowNumber, row[7]),
                        )
                    }
                    FinPixTransaction(
                        timstamp = row.getDateTime(3),
                        type = transactionType,
                        comment = row[12],
                    )
                }
        }
    }

fun main() {
    val transactions = readTransactions().filter {
        when (val type = it.type) {
            is FinPixTransaction.Type.Income -> type.amount.cents != 0
            is FinPixTransaction.Type.Outcome -> type.amount.cents != 0
            is FinPixTransaction.Type.Transfer -> type.amountTo.cents != 0 || type.amountFrom.cents != 0
        }
    }
    val normalizedTransactions = transactions.map { transaction ->
        transaction.copy(
            type = when (val type = transaction.type) {
                is FinPixTransaction.Type.Income -> when (type.amount.cents < 0) {
                    false -> type
                    true -> FinPixTransaction.Type.Outcome(
                        category = type.category,
                        from = type.to,
                        comment = type.comment,
                        amount = -type.amount,
                    )
                }

                is FinPixTransaction.Type.Outcome -> when (type.amount.cents < 0) {
                    false -> type
                    true -> FinPixTransaction.Type.Income(
                        category = type.category,
                        to = type.from,
                        comment = type.comment,
                        amount = -type.amount,
                    )
                }

                is FinPixTransaction.Type.Transfer -> type.takeIf {
                    it.amountTo.cents > 0 && it.amountFrom.cents > 0
                }
            } ?: error("Illegal transaction $transaction")
        )
    }

    val pinFinTransactions = normalizedTransactions
        .groupBy { it.timstamp }
        .map { (timestamp, transactionsOrEmpty) ->
            val transactions = transactionsOrEmpty.toNonEmptyListOrNull()!!

            fun buildEntry(
                account: FinPixAccount,
            ): Transaction.Type.Entry {

                fun checkAccount(
                    transactionAccount: FinPixAccount,
                ) {
                    if (account != transactionAccount) {
                        error("Unable to build composite entry transaction with different accounts $transactionAccount and $account")
                    }
                }

                val records = transactions.map { transaction ->
                    when (val type = transaction.type) {
                        is FinPixTransaction.Type.Income -> {
                            checkAccount(type.to)
                            Record(
                                comment = type.comment.let(::Comment),
                                category = type.category.toCategoryId(CategoryDirection.Credit),
                                amount = type.amount.amount,
                            )
                        }

                        is FinPixTransaction.Type.Outcome -> {
                            checkAccount(type.from)
                            Record(
                                comment = type.comment.let(::Comment),
                                category = type.category.toCategoryId(CategoryDirection.Debit),
                                amount = type.amount.amount,
                            )
                        }

                        is FinPixTransaction.Type.Transfer -> error("Unable build Entry transaction from transfer")
                    }
                }

                return Transaction.Type.Entry(
                    records = records,
                    account = account.accountId,
                )
            }

            val type = when (val type = transactions.head.type) {
                is FinPixTransaction.Type.Transfer -> {
                    if (transactions.tail.isNotEmpty()) {
                        error("Unable build group of transfers")
                    }
                    if (type.amountTo != type.amountFrom) {
                        println("AmountFrom=${type.amountFrom}, amountTo=${type.amountTo}")
                    }
                    Transaction.Type.Transfer(
                        from = type.from.accountId,
                        to = type.to.accountId,
                        amount = type.amountTo.amount,
                    )
                }

                is FinPixTransaction.Type.Income -> buildEntry(type.to)
                is FinPixTransaction.Type.Outcome -> buildEntry(type.from)
            }

            val typeComment = when (type) {
                is Transaction.Type.Entry -> type
                    .records
                    .mapNotNull { it.comment.text.trim().takeIf(String::isNotEmpty) }
                    .joinToString(separator = ", ")

                is Transaction.Type.Transfer -> null
            }

            val comment = transactions
                .asSequence()
                .map { it.comment }
                .filter { it != typeComment }
                .mapNotNull { it.trim().takeIf(String::isNotBlank) }
                .firstOrNull()
                .orEmpty()

            Transaction(
                timestamp = timestamp,
                comment = comment.let(::Comment),
                type = type,
            )
        }

    val charset: Charset = Charsets.UTF_8
    val linesSeparator: ByteArray = "\n".toByteArray(charset)
    val out = File(dataDir, "out")
    out.deleteRecursively()
    out.mkdirs()
    FileOutputStream(File(out, Uuid.randomUuid().toString())).use { output ->
        pinFinTransactions.forEach { transaction ->
            val update = UpdateType.Transaction(
                id = Transaction.Id.new(),
                transaction = transaction,
            )
            val line: ByteArray = Json
                .encodeToString(
                    serializer = UpdateType.serializer(),
                    value = update
                )
                .toByteArray(charset)
            output.write(line)
            output.write(linesSeparator)
        }
    }


}
