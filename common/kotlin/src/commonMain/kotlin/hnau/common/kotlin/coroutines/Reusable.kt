package hnau.common.kotlin.coroutines

@PublishedApi
internal class Reusable<K, V> {

    private var cached: Pair<K, V>? = null

    fun getCachedOrCreateNew(
        key: K,
        createNew: () -> V,
    ): V = when (val suitableCached = cached?.takeIf { it.first == key }) {
        null -> {
            val value = createNew()
            cached = key to value
            value
        }

        else -> suitableCached.second
    }
}

@Suppress("FunctionName")
@PublishedApi
internal inline fun <K, V> Reusable(
    crossinline createNew: (K) -> V,
): (K) -> V {
    val reusable = Reusable<K, V>()
    return { key ->
        reusable.getCachedOrCreateNew(
            key = key,
            createNew = { createNew(key) },
        )
    }
}
