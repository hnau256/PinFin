package hnau.common.compose.uikit.bubble

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlin.time.Duration.Companion.milliseconds

class SharedBubblesHolder(
    scope: CoroutineScope,
) : BubblesShower, BubblesProvider {

    private val bubbles: MutableSharedFlow<Bubble> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override val visibleBubble: StateFlow<Bubble?> = bubbles
        .transformLatest { bubble ->
            emit(bubble)
            delay(bubble.duration.duration)
            emit(null)
        }
        .debounce(100.milliseconds)
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )

    override fun showBubble(
        bubble: Bubble,
    ) {
        bubbles.tryEmit(bubble)
    }
}
