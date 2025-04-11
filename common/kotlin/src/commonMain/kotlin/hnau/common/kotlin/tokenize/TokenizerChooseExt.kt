package hnau.common.kotlin.tokenize

import arrow.core.NonEmptyList


internal fun <I, T> Tokenizer.Factory.Companion.choose(
    firstOptionFactory: Tokenizer.OptionFactory<I, T>,
    vararg otherOptionFactories: Tokenizer.OptionFactory<I, T>,
): Tokenizer.Factory<I, T> = choose(
    optionFactories = NonEmptyList(
        head = firstOptionFactory,
        tail = otherOptionFactories.toList(),
    ),
)

internal fun <I, T> Tokenizer.Factory.Companion.choose(
    optionFactories: NonEmptyList<Tokenizer.OptionFactory<I, T>>,
): Tokenizer.Factory<I, T> = Tokenizer.Factory { firstItem ->

    optionFactories
        .firstNotNullOfOrNull { optionFactory ->
            optionFactory.tryCreateTokenizer(
                firstItem = firstItem,
            )
        }
        ?: throw IllegalArgumentException("Unable build Tokenizer for $firstItem")
}