package hnau.pinfin.data

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Amount(
    val value: UInt,
) {

    inline fun map(
        transform: (UInt) -> UInt,
    ): Amount = Amount(
        value = transform(value),
    )

    companion object {

        val zero: Amount = Amount(0u)

        val uintMapper: Mapper<UInt, Amount> = Mapper(
            direct = ::Amount,
            reverse = Amount::value,
        )
    }
}