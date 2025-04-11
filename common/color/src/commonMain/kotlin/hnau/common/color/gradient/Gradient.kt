package hnau.common.color.gradient

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Gradient<C>(
    val head: C,
    @Serializable(NonEmptyListSerializer::class)
    val tail: NonEmptyList<Step<C>>,
) : ExtendableToGradient<C> {

    override fun step(
        color: C,
        weight: Float,
    ): Gradient<C> = copy(
        tail = tail + Step(
            weight = weight,
            color = color,
        ),
    )

    @Serializable
    data class Step<C>(
        val weight: Float = 1f,
        val color: C,
    )

    companion object {

        fun <C> create(
            head: C,
            tail: NonEmptyList<C>,
        ): Gradient<C> = Gradient(
            head = head,
            tail = tail.map { color ->
                Step(
                    weight = 1f,
                    color = color,
                )
            }
        )
    }
}
