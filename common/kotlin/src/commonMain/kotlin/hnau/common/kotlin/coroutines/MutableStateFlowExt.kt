package hnau.common.kotlin.coroutines

import kotlinx.coroutines.flow.MutableStateFlow

fun <T> T.toMutableStateFlowAsInitial(): MutableStateFlow<T> =
    MutableStateFlow(this)

inline fun <T> MutableStateFlow<T>.transformSet(
    crossinline transform: (T, (T) -> Unit) -> Unit,
): MutableStateFlow<T> = object : MutableStateFlow<T> by this {

    private val receiver: MutableStateFlow<T>
        get() = this@transformSet

    override var value: T
        get() = receiver.value
        set(newValue) {
            transform(
                newValue,
            ) { newNewValue ->
                receiver.value = newNewValue
            }
        }
}

inline fun <T> MutableStateFlow<T>.filterSet(
    crossinline predicate: (T) -> Boolean,
): MutableStateFlow<T> = transformSet { value, setValue ->
    if (predicate(value)) {
        setValue(value)
    }
}

inline fun <T> MutableStateFlow<T>.onSet(
    crossinline handler: (T) -> Unit,
): MutableStateFlow<T> = transformSet { value, setValue ->
    handler(value)
    setValue(value)
}