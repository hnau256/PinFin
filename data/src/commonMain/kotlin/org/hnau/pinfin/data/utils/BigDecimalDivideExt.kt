package org.hnau.pinfin.data.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode

private const val DIVISION_PRECISION_MARGIN = 10L

/**
 * [BigDecimal.divide] с [decimalMode], у которого `decimalPrecision` выведен из реальных
 * операндов текущего деления, а не задан константой заранее.
 *
 * У ionspin bignum 0.3.10 `decimalPrecision` в [DecimalMode] - это рабочая точность,
 * используемая ДО деления: если она меньше, чем нужно для операндов, делимое усекается
 * заранее и результат неверен (проверено вручную: `decimalPrecision=0`, т.е. значение по
 * умолчанию, даёт `90/1=10`, `810/8=0`), а любая фиксированная константа рано или поздно
 * не хватит для операндов с большим числом цифр. Формула
 * `decimalPrecision = this.precision + other.precision + scale + запас` гарантированно
 * достаточна независимо от величины операндов (проверено на 15-значных числах) - после этого
 * `scale` в [decimalMode] сам корректно округляет результат до нужного числа знаков.
 */
fun BigDecimal.dividePrecisely(
    other: BigDecimal,
    decimalMode: DecimalMode,
): BigDecimal = divide(
    other = other,
    decimalMode = decimalMode.copy(
        decimalPrecision = precision + other.precision + decimalMode.scale.coerceAtLeast(0) + DIVISION_PRECISION_MARGIN,
    ),
)
