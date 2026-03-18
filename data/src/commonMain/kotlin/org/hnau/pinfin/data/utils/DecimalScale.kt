package org.hnau.pinfin.data.utils

import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import kotlin.jvm.JvmInline

@JvmInline
value class DecimalScale(
    val scale: Long,
)


val DecimalScale.decimalMode: DecimalMode
    get() = DecimalMode(
        scale = scale,
        roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
    )