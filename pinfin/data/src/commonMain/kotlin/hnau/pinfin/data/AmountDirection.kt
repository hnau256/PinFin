package hnau.pinfin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AmountDirection {

    @SerialName("credit")
    Credit,

    @SerialName("debit")
    Debit,
    ;

    companion object {

        val default: AmountDirection = Debit
    }
}

@Serializable
data class CategoryDirectionValues<T>(
    val credit: T,
    val debit: T,
) {

    operator fun get(
        direction: AmountDirection,
    ): T = when (direction) {
        AmountDirection.Credit -> credit
        AmountDirection.Debit -> debit
    }

    companion object {

        inline fun <T> create(
            create: (AmountDirection) -> T,
        ): CategoryDirectionValues<T> = CategoryDirectionValues(
            credit = create(AmountDirection.Credit),
            debit = create(AmountDirection.Debit),
        )
    }
}