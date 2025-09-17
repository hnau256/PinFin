package hnau.pinfin.model.utils

import arrow.core.Option
import arrow.core.Some
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

//TODO extract to common-kotlin
fun <T> StateFlow<T>.toMutableStateFlow(
    scope: CoroutineScope,
    set: (T) -> Unit,
) {

}

private class MutableStateFlowBasedOnImmutable<T>(
    private val source: StateFlow<T>,
    private val set: (T) -> Unit,
    private val compareAndSet: (expect: T, update: T) -> Boolean,
) : StateFlow<T> by source, MutableStateFlow<T> {

    override var value: T
        get() = source.value
        set(value) = set(value)

    override fun compareAndSet(
        expect: T,
        update: T,
    ): Boolean = compareAndSet.invoke(
        expect,
        update,
    )

    override val subscriptionCount: StateFlow<Int>
        get() = shared.subscriptionCount

    override suspend fun emit(value: T) {
        set(value)
    }

    override fun tryEmit(value: T): Boolean {
        set(value)
        return true
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        sdc
    }

}