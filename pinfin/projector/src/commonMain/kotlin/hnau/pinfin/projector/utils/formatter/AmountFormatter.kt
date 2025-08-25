package hnau.pinfin.projector.utils.formatter

import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.ifNull
import hnau.pinfin.data.Amount
import kotlin.math.absoluteValue

interface AmountFormatter {

    fun format(
        amount: Amount,
        alwaysShowSign: Boolean = false,
    ): String

    fun parse(
        input: String,
    ): Amount?

    companion object {

        val test: AmountFormatter = object : AmountFormatter {

            private val factor = 100

            override fun format(
                amount: Amount,
                alwaysShowSign: Boolean,//TODO
            ): String = amount
                .value
                .toLong()
                .let {
                    val count = (it / factor)
                        .toString()
                        .reversed()
                        .windowed(
                            size = 3,
                            step = 3,
                            partialWindows = true,
                        )
                        .joinToString(
                            separator = '\u00A0'.toString(),
                        )
                        .reversed()
                    val cents = (it % factor)
                        .absoluteValue
                        .toString()
                        .padStart(2, '0')
                    listOf(count, cents).joinToString(
                        separator = "."
                    )
                }
                .let { raw ->
                    alwaysShowSign.foldBoolean(
                        ifFalse = { raw },
                        ifTrue = {
                            raw
                                .firstOrNull()
                                ?.isDigit()
                                .ifNull { true }
                                .foldBoolean(
                                    ifFalse = { raw },
                                    ifTrue = { "+$raw" }
                                )
                        },
                    )
                }

            override fun parse(
                input: String,
            ): Amount? = input
                .filterNot(Char::isWhitespace)
                .removePrefix("-")
                .split(".")
                .takeIf { it.size in 1..2 }
                ?.let { parts ->
                    val count = parts
                        .firstOrNull()
                        .ifNull { "0" }
                        .toLongOrNull()
                        ?: return@let null
                    val cents = parts
                        .getOrNull(1)
                        .ifNull { "00" }
                        .takeIf { it.length in 1..2 }
                        ?.padEnd(2, '0')
                        ?.toIntOrNull()
                        ?: return@let null
                    count * factor + cents
                }
                ?.takeIf { it >= 0 }
                ?.toInt()
                ?.let { absoluteCents ->
                    when (input.startsWith('-')) {
                        true -> -absoluteCents
                        false -> absoluteCents
                    }
                }
                ?.let(::Amount)
        }
    }
}