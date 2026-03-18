package org.hnau.pinfin.data

import org.hnau.pinfin.data.utils.DecimalScale

data class Currency(
    val scale: DecimalScale,
) {

    companion object {

        val default: Currency = Currency(
            scale = DecimalScale(
                scale = 2L,
            ),
        )
    }
}
