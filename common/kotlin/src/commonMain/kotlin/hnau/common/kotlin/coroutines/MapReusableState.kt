package hnau.common.kotlin.coroutines

import arrow.core.NonEmptyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

interface ReusableStateScope<in K, ITEM> {

    fun getOrPutItem(
        key: K,
        build: (CoroutineScope) -> ITEM,
    ): ITEM
}

fun <I, K, O> StateFlow<NonEmptyList<I>>.mapNonEmptyListReusable(
    scope: CoroutineScope,
    extractKey: (I) -> K,
    transform: (CoroutineScope, I) -> O,
): StateFlow<NonEmptyList<O>> = mapReusable(
    scope = scope,
) { nonEmptyItems ->
    nonEmptyItems.map { item ->
        getOrPutItem(
            key = extractKey(item),
            build = { itemScope ->
                transform(itemScope, item)
            }
        )
    }
}

fun <I, K, O> StateFlow<List<I>>.mapListReusable(
    scope: CoroutineScope,
    extractKey: (I) -> K,
    transform: (CoroutineScope, I) -> O,
): StateFlow<List<O>> = mapReusable(
    scope = scope,
) { items ->
    items.map { item ->
        getOrPutItem(
            key = extractKey(item),
            build = { itemScope ->
                transform(itemScope, item)
            }
        )
    }
}

fun <I, K, ITEM, O> StateFlow<I>.mapReusable(
    scope: CoroutineScope,
    buildState: ReusableStateScope<K, ITEM>.(I) -> O,
): StateFlow<O> = this
    .runningFoldState(
        scope = scope,
        createInitial = { items ->
            val newCache = mutableMapOf<K, Scoped<ITEM>>()
            val scope = ReusableStateScopeImpl(
                scope = scope,
                newCache = newCache,
            )
            val initialState = scope.buildState(items)
            newCache to initialState
        },
        operation = { (lastCache, _), items ->
            val newCache = mutableMapOf<K, Scoped<ITEM>>()
            val scope = ReusableStateScopeImpl(
                scope = scope,
                newCache = newCache,
                lastCache = lastCache,
            )
            val initialState = scope.buildState(items)
            lastCache.values.forEach { it.scope.cancel() }
            newCache to initialState
        }
    )
    .mapStateLite { (_, values) -> values }

private class ReusableStateScopeImpl<in K, ITEM>(
    private val scope: CoroutineScope,
    private val newCache: MutableMap<K, Scoped<ITEM>>,
    private val lastCache: MutableMap<K, Scoped<ITEM>> = mutableMapOf(),
) : ReusableStateScope<K, ITEM> {

    override fun getOrPutItem(
        key: K,
        build: (CoroutineScope) -> ITEM,
    ): ITEM {
        val hasInCache = lastCache.containsKey(key)
        val value = when (hasInCache) {
            true -> lastCache.remove(key) as Scoped<ITEM>

            false -> {
                val itemScope = scope.createChild()
                val item = build(itemScope)
                Scoped(itemScope, item)
            }
        }
        newCache[key] = value
        return value.value
    }
}
