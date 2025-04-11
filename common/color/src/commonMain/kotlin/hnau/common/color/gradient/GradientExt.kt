package hnau.common.color.gradient

inline fun <I, O> Gradient<I>.map(
    transform: (I) -> O,
): Gradient<O> = Gradient(
    head = transform(head),
    tail = tail.map { step -> step.map(transform) },
)

inline fun <I, O> Gradient.Step<I>.map(
    transform: (I) -> O,
): Gradient.Step<O> = Gradient.Step(
    weight = weight,
    color = transform(color),
)
