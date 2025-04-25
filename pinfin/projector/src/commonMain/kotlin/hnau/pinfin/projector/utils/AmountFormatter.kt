package hnau.pinfin.projector.utils

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

            private val factor = 100f

            override fun format(
                amount: Amount,
            ): String = amount
                .value
                .toLong()
                .div(factor)
                .toString()
                .dropLastWhile { it == '0' }
                .removeSuffix(".")

            override fun parse(
                input: String,
            ): Amount? = input
                .toFloatOrNull()
                ?.times(factor)
                ?.toLong()
                ?.takeIf { it >= 0 }
                ?.toUInt()
                ?.let(::Amount)
        }
    }
}