package org.hnau.pinfin.data

import kotlinx.serialization.Serializable
import org.hnau.pinfin.data.utils.DecimalScale

@Serializable
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
