package hnau.common.color.gradient

import arrow.core.NonEmptyList

fun <C> Gradient.Companion.create(
    head: C,
    tailHead: C,
    vararg tailTail: C,
): Gradient<C> = Gradient(
    head = head,
    tail = NonEmptyList(
        head = tailHead,
        tail = tailTail.toList(),
    ).map {
        Gradient.Step(
            weight = 1f,
            color = it,
        )
    },
)

inline fun <C> Gradient.Companion.create(
    stops: Int,
    get: (Float) -> C,
): Gradient<C> {
    val normalizedSize = stops.coerceAtLeast(2)
    val delta: Float = 1f / (normalizedSize - 1)
    return create(
        head = get(0f),
        tail = NonEmptyList(
            head = get(delta),
            tail = (2 until normalizedSize).map { index ->
                get(delta * index)
            }
        )
    )
}

fun <C> Gradient.Companion.build(
    head: C,
): ExtendableToGradient<C> = object : ExtendableToGradient<C> {

    override fun step(
        color: C,
        weight: Float,
    ): Gradient<C> = Gradient(
        head = head,
        tail = NonEmptyList(
            head = Gradient.Step(
                weight = weight,
                color = color,
            ),
            tail = emptyList(),
        ),
    )
}