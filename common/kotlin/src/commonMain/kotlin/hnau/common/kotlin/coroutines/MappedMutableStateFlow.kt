package hnau.common.kotlin.coroutines

import hnau.common.kotlin.mapper.Mapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MappedMutableStateFlow<I, O>(
    scope: CoroutineScope,
    private val source: MutableStateFlow<I>,
    private val mapper: Mapper<I, O>,
) : MutableStateFlow<O> {

    private val immutable: StateFlow<O> = source.mapState(
        scope = scope,
        transform = mapper.direct,
    )
    override var value: O
        get() = immutable.value
        set(value) {
            source.value = mapper.reverse(value)
        }

    override fun compareAndSet(
        expect: O,
        update: O,
    ): Boolean = source.compareAndSet(
        expect = mapper.reverse(expect),
        update = mapper.reverse(update),
    )

    override val subscriptionCount: StateFlow<Int>
        get() = source.subscriptionCount

    override suspend fun emit(
        value: O,
    ) {
        source.emit(mapper.reverse(value))
    }

    override fun tryEmit(
        value: O,
    ): Boolean = source.tryEmit(
        mapper.reverse(value)
    )

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        source.resetReplayCache()
    }

    override val replayCache: List<O>
        get() = immutable.replayCache

    override suspend fun collect(
        collector: FlowCollector<O>,
    ): Nothing = immutable.collect(
        collector = collector,
    )
}

fun <I, O> MutableStateFlow<I>.mapMutableState(
    scope: CoroutineScope,
    mapper: Mapper<I, O>,
): MutableStateFlow<O> = MappedMutableStateFlow(
    scope = scope,
    source = this,
    mapper = mapper,
)