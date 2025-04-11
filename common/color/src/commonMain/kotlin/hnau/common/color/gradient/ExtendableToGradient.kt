package hnau.common.color.gradient

interface ExtendableToGradient<C> {

    fun step(
        color: C,
        weight: Float = 1f,
    ): Gradient<C>
}
