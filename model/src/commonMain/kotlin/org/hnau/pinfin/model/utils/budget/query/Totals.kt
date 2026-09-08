package org.hnau.pinfin.model.utils.budget.query

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.div
import org.hnau.pinfin.data.utils.DecimalScale

@Serializable
data class Totals(
    @SerialName("sum_debit")
    val sumDebit: Amount,
    @SerialName("sum_credit")
    val sumCredit: Amount,
) {

    val sum: SignedAmount = SignedAmount.creditMinusDebit(
        credit = sumCredit,
        debit = sumDebit,
    )

    val isZero: Boolean
        get() = sumDebit == Amount.zero && sumCredit == Amount.zero

    operator fun plus(
        other: Totals,
    ): Totals = Totals(
        sumDebit = sumDebit + other.sumDebit,
        sumCredit = sumCredit + other.sumCredit,
    )

    fun div(
        divisor: Int,
        scale: DecimalScale,
    ): Totals = Totals(
        sumDebit = sumDebit.div(divisor, scale),
        sumCredit = sumCredit.div(divisor, scale),
    )

    companion object {

        val zero: Totals = Totals(
            sumDebit = Amount.zero,
            sumCredit = Amount.zero,
        )
    }
}
