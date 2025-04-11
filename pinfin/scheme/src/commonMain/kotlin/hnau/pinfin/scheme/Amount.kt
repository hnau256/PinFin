package hnau.pinfin.scheme

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.Serializable

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