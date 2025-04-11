package hnau.common.kotlin.tokenize

import arrow.core.None
import arrow.core.Some

internal fun <I, T> Sequence<I>.tokenize(
    tokenizerFactory: Tokenizer.Factory<I, T>,
): Sequence<T> = object : Sequence<T> {

    override fun iterator(): Iterator<T> = object : Iterator<T> {

        private val source: DoubtingIterator<I> =
            this@tokenize.iterator().doubting()

        override fun hasNext(): Boolean = when (source.current) {
            None -> false
            is Some -> true
        }

        override fun next(): T {
            val firstItem = when (val firstItemOrNone = source.current) {
                None -> throw NoSuchElementException()
                is Some -> firstItemOrNone.value
            }
            source.switchToNext()
            var tokenizer = tokenizerFactory.createTokenizer(firstItem)
            do {
                val nextItem = when (val nextItemOrNone = source.current) {
                    None -> return tokenizer.collect()
                    is Some -> nextItemOrNone.value
                }
                when (val nextTokenizer = tokenizer.tryConsume(nextItem)) {
                    null -> return tokenizer.collect()
                    else -> {
                        tokenizer = nextTokenizer
                        source.switchToNext()
                    }
                }
            } while (true)
        }
    }
}