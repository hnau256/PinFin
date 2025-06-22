package hnau.pinfin.projector.utils.formatter

import hnau.pinfin.data.Amount

interface AmountFormatter {

    fun format(
        amount: Amount,
    ): String

    fun parse(
        input: String,
    ): Amount?

    companion object {

        val test: AmountFormatter = object : AmountFormatter {

            private val factor = 100

            override fun format(
                amount: Amount,
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
                        .takeIf { it > 0 }
                        ?.toString()
                        ?.padEnd(2, '0')
                    listOfNotNull(
                        count,
                        cents
                    ).joinToString(
                        separator = "."
                    )
                }

            override fun parse(
                input: String,
            ): Amount? = input
                .filterNot(Char::isWhitespace)
                .toFloatOrNull()
                ?.times(factor)
                ?.toLong()
                ?.takeIf { it >= 0 }
                ?.toUInt()
                ?.let(::Amount)
        }
    }
}