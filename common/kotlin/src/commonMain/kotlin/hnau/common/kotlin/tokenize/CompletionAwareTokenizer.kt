package hnau.common.kotlin.tokenize

import arrow.core.Either

internal abstract class CompletionAwareTokenizer<I, T> : Tokenizer<I, T> {

    protected abstract fun consumeOrCollect(
        nextItem: I,
    ): Either<T, Tokenizer<I, T>>

    override fun collect(): T = throw IllegalStateException("Not completed yet")

    override fun tryConsume(
        nextItem: I,
    ): Tokenizer<I, T>? = this
        .consumeOrCollect(nextItem)
        .fold(
            ifLeft = Tokenizer.Companion::alreadyCompleted,
            ifRight = { it },
        )
}

@Suppress("FunctionName")
internal inline fun <I, T> CompletionAwareTokenizer(
    crossinline consumeOrCollect: (item: I) -> Either<T, Tokenizer<I, T>>,
): Tokenizer<I, T> = object : CompletionAwareTokenizer<I, T>() {

    override fun consumeOrCollect(
        nextItem: I,
    ): Either<T, Tokenizer<I, T>> = consumeOrCollect.invoke(nextItem)
}