package hnau.pinfin.model.utils

import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.foldNullable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import hnau.common.kotlin.coroutines.Delayed
import hnau.common.kotlin.coroutines.mapStateDelayed

inline fun <S, T, R> resolveSuggests(
    scope: CoroutineScope,
    source: StateFlow<S>,
    searchQuery: StateFlow<String>,
    crossinline extractItems: suspend (S) -> Collection<T>,
    crossinline extractText: (T) -> String,
    crossinline extractTimestamp: (T) -> Instant,
    crossinline convertToResult: (T) -> R,
    limit: Int = 64,
): StateFlow<Loadable<Delayed<List<R>>>> = searchQuery
    .combineStateWith(
        scope = scope,
        other = source,
    ) { query, source ->
        source to query
    }
    .mapStateDelayed(scope) { (source, queryOrEmpty) ->
        withContext(Dispatchers.Default) {

            val queryOrNull = queryOrEmpty
                .trim()
                .takeIf(String::isNotEmpty)

            extractItems(source)
                .mapNotNull { item ->

                    val text = item
                        .let(extractText)
                        .trim()
                        .takeIf(String::isNotEmpty)
                        ?: return@mapNotNull null

                    val equalsFromFirstChar = queryOrNull
                        .foldNullable(
                            ifNull = { true },
                            ifNotNull = { query ->
                                text
                                    .takeIf { it.length != query.length }
                                    ?.indexOf(
                                        string = query,
                                        ignoreCase = true,
                                    )
                                    ?.takeIf { it >= 0 }
                                    ?.let { it == 0 }
                            }
                        )
                        ?: return@mapNotNull null

                    ItemWrapper(
                        item = item,
                        equalsFromFirstChar = equalsFromFirstChar,
                        timestamp = item.let(extractTimestamp),
                        text = text,
                    )
                }
                .sortedWith(ItemWrapper.comparator)
                .asReversed()
                .distinctBy(ItemWrapper<*>::text)
                .take(limit)
                .map { wrapper ->
                    wrapper
                        .item
                        .let(convertToResult)
                }
        }
    }

@PublishedApi
internal data class ItemWrapper<out T>(
    val item: T,
    val equalsFromFirstChar: Boolean,
    val timestamp: Instant,
    val text: String,
) {

    companion object {

        private val equalsFromFirstCharComparator = Comparator<ItemWrapper<Any?>> { a, b ->
            a.equalsFromFirstChar.compareTo(b.equalsFromFirstChar)
        }

        private val timestampComparator = Comparator<ItemWrapper<Any?>> { a, b ->
            a.timestamp.compareTo(b.timestamp)
        }

        val comparator: Comparator<ItemWrapper<Any?>> =
            equalsFromFirstCharComparator.then(timestampComparator)
    }
}