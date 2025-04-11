package hnau.common.app.messages

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class SharedMessagesHolder<T> : MessagesReceiver<T> {

    private val _messages: MutableSharedFlow<T> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val messages: Flow<T>
        get() = _messages

    override fun sendMessage(message: T) {
        _messages.tryEmit(message)
    }
}
