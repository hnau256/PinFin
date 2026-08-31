package org.hnau.pinfin.data

import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.expression.AmountExpression

val Record.directionedAmount: KeyValue<AmountDirection, AmountExpression>
    get() = KeyValue(
        key = category.direction,
        value = amount,
    )