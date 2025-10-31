package hnau.pinfin.model.utils

@PublishedApi
internal data class DeferredList<out T>(
    override val size: Int,
    private val resolve: (Int) -> T,
) : AbstractList<T>() {

    override fun get(
        index: Int,
    ): T = resolve(index)
}