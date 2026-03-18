package org.hnau.pinfin.projector.utils.formatter

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.mapFirst
import org.hnau.commons.kotlin.mapSecond
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection

interface AmountFormatter {
    fun format(
        amount: Amount,
        alwaysShowSign: Boolean = false,
        alwaysShowCents: Boolean = true,
    ): String

    fun parse(input: String): Amount?

    companion object {
        val test: AmountFormatter =
            object : AmountFormatter {
                private val decimalLength = 2

                override fun format(
                    amount: Amount,
                    alwaysShowSign: Boolean,
                    alwaysShowCents: Boolean,
                ): String {

                    val (direction, absolute) = amount
                        .splitToDirectionAndRaw()
                        .map { amount ->
                            amount
                                .value
                                .roundToDigitPositionAfterDecimalPoint(
                                    digitPosition = decimalLength.toLong(),
                                    roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                                )
                        }

                    val absoluteString = absolute
                        .toStringExpanded()
                        .split('.')
                        .let { parts -> parts[0] to parts.getOrNull(1) }
                        .mapFirst { integerStr ->
                            integerStr
                                .reversed()
                                .windowed(
                                    size = 3,
                                    step = 3,
                                    partialWindows = true,
                                )
                                .joinToString(separator = '\u00A0'.toString())
                                .reversed()
                        }
                        .mapSecond { fractionStr ->
                            fractionStr
                                .orEmpty()
                                .padEnd(decimalLength, '0')
                                .takeIf { alwaysShowCents || it.any { c -> c != '0' } }
                        }
                        .toList()
                        .filterNotNull()
                        .joinToString(".")

                    return when (direction) {
                        AmountDirection.Debit -> "-${absoluteString}"
                        AmountDirection.Credit -> alwaysShowSign.foldBoolean(
                            ifTrue = { "+${absoluteString}" },
                            ifFalse = { absoluteString }
                        )
                    }
                }

                override fun parse(input: String): Amount? =
                    input
                        .filterNot(Char::isWhitespace)
                        .let { runCatching { BigDecimal.parseString(it) }.getOrNull() }
                        ?.let(::Amount)
            }
    }
}
