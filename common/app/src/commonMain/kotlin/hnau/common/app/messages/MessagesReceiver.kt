package hnau.common.app.messages

interface MessagesReceiver<T> {

    fun sendMessage(message: T)
}
