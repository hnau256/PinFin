package hnau.common.compose.utils

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf

inline fun <T, R> State<T>.map(
    crossinline transform: (T) -> R,
): State<R> = derivedStateOf {
    transform(value)
}