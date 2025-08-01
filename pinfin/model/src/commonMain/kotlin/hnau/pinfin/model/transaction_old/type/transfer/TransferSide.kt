package hnau.pinfin.model.transaction_old.type.transfer

import kotlinx.serialization.Serializable

enum class TransferSide {
    From, To
}

@Serializable
data class TransferSideValues<out T>(
    val from: T,
    val to: T,
) {

    operator fun get(
        side: TransferSide,
    ): T = when (side) {
        TransferSide.From -> from
        TransferSide.To -> to
    }

    inline fun <O> mapFull(
        transform: (TransferSide, T) -> O,
    ): TransferSideValues<O> = TransferSideValues(
        from = transform(TransferSide.From, from),
        to = transform(TransferSide.To, to),
    )

    inline fun <O> map(
        transform: (T) -> O,
    ): TransferSideValues<O> = mapFull { _, value ->
        transform(value)
    }
}