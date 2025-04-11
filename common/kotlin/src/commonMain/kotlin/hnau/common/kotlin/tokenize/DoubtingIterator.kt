package hnau.common.kotlin.tokenize

import arrow.core.None
import arrow.core.Option
import arrow.core.Some

internal interface DoubtingIterator<T> {

    val current: Option<T>

    fun switchToNext()
}


internal fun <T> Iterator<T>.doubting(): DoubtingIterator<T> = object : DoubtingIterator<T> {

    private val source: Iterator<T>
        get() = this@doubting

    private var _current: Option<T>? = null

    override val current: Option<T>
        get() = resolveCurrent()

    private fun resolveCurrent(): Option<T> = synchronized(this) {
        var result = _current
        if (result == null) {
            result = when (source.hasNext()) {
                true -> Some(source.next())
                false -> None
            }
            _current = result
        }
        result
    }

    override fun switchToNext() {
        _current = null
    }

}