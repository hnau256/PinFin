package hnau.common.kotlin.tokenize

private val floatTokenizerOptionFactory = Tokenizer.OptionFactory<Char, Float> { firstChar ->
    when {
        firstChar.isDigit() || firstChar == '.' || firstChar == '-' -> FloatTokenizer(
            collectedChars = firstChar.toString(),
        )

        else -> null
    }
}

private class FloatTokenizer(
    private val collectedChars: String,
) : Tokenizer<Char, Float> {

    override fun collect(): Float = collectedChars.toFloat()

    override fun tryConsume(
        nextItem: Char,
    ): Tokenizer<Char, Float>? = nextItem
        .takeIf { char -> char.isDigit() || char == '.' }
        ?.let { FloatTokenizer(collectedChars = collectedChars + it) }
}

internal val Tokenizer.OptionFactory.Companion.float: Tokenizer.OptionFactory<Char, Float>
    get() = floatTokenizerOptionFactory