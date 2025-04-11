package hnau.common.kotlin.tokenize

internal fun <I, T> Tokenizer.Companion.alreadyCompleted(
    value: T,
): Tokenizer<I, T> = object : Tokenizer<I, T> {

    override fun collect(): T = value

    override fun tryConsume(
        nextItem: I,
    ): Tokenizer<I, T>? = null
}