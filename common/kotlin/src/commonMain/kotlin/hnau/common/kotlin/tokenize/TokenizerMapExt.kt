package hnau.common.kotlin.tokenize

import arrow.core.Either

internal fun <I, TI, TO> Tokenizer<I, TI>.map(
    transform: (TI) -> TO,
): Tokenizer<I, TO> = object : Tokenizer<I, TO> {

    private val source: Tokenizer<I, TI>
        get() = this@map

    override fun collect(): TO =
        source.collect().let(transform)

    override fun tryConsume(
        nextItem: I,
    ): Tokenizer<I, TO>? = source
        .tryConsume(
            nextItem = nextItem,
        )
        ?.map(transform)
}

internal fun <I, TI, TO> Tokenizer.OptionFactory<I, TI>.map(
    transform: (TI) -> TO,
): Tokenizer.OptionFactory<I, TO> = Tokenizer.OptionFactory { firstItem ->
    tryCreateTokenizer(firstItem)?.map(transform)
}

private fun <I, TI, TO> Either<TI, Tokenizer<I, TI>>.mapTokenizerOrValue(
    transform: (TI) -> TO,
): Either<TO, Tokenizer<I, TO>> = this
    .mapLeft(transform)
    .map { tokenizer ->
        tokenizer.map(
            transform = transform,
        )
    }