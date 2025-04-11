package hnau.common.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun <T> MutableStateFlow<T>.collectAsMutableState(): MutableState<T> {
    val state = collectAsState(Dispatchers.Main.immediate)
    return remember(this, state) {

        object : MutableState<T> {

            override var value: T
                get() = state.value
                set(value) {
                    this@collectAsMutableState.value = value
                }

            override fun component1(): T = value

            override fun component2(): (T) -> Unit = ::value::set
        }
    }
}