package hnau.common.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import hnau.common.kotlin.mapper.Mapper

inline fun <T, R> State<T>.map(
    crossinline transform: (T) -> R,
): State<R> = derivedStateOf {
    transform(value)
}

@Deprecated("Try use mapRemembered instead")
fun <I, O> MutableState<I>.map(
    mapper: Mapper<I, O>,
): MutableState<O> = object : MutableState<O> {

    private val source: MutableState<I>
        get() = this@map

    override var value: O
        get() = source.value.let(mapper.direct)
        set(value) {
            source.value = value.let(mapper.reverse)
        }

    override fun component1(): O = value

    override fun component2(): (O) -> Unit = ::value::set
}

@Composable
fun <I, O> MutableState<I>.mapRemembered(
    mapper: Mapper<I, O>,
): MutableState<O> = remember(this, mapper) {
    @Suppress("DEPRECATION")
    map(mapper)
}