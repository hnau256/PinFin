package hnau.common.kotlin.tokenize

internal interface Tokenizer<I, out T> {

    fun collect(): T

    fun tryConsume(
        nextItem: I,
    ): Tokenizer<I, T>?

    fun interface Factory<I, out T> {

        fun createTokenizer(
            firstItem: I,
        ): Tokenizer<I, T>

        companion object
    }

    fun interface OptionFactory<I, out T> {

        fun tryCreateTokenizer(
            firstItem: I,
        ): Tokenizer<I, T>?

        companion object
    }

    companion object
}