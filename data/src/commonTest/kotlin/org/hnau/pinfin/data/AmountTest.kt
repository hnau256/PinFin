package org.hnau.pinfin.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AmountTest {

    @Test
    fun negativeAmountThrows() {
        assertFailsWith<IllegalArgumentException> {
            Amount(
                value = BigDecimal.fromInt(-5),
            )
        }
    }
}
