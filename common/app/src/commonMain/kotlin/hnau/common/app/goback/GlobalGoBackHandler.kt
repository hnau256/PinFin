package hnau.common.app.goback

import hnau.common.kotlin.coroutines.flatMapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

interface GlobalGoBackHandler {

    fun resolve(
        scope: CoroutineScope,
    ): GoBackHandler
}

class GlobalGoBackHandlerImpl() : GlobalGoBackHandler {

    private val lazy: MutableStateFlow<GoBackHandler> =
        MutableStateFlow(GoBackHandlerProvider.never.goBackHandler)

    fun init(
        goBackHandler: GoBackHandler,
    ) {
        lazy.value = goBackHandler
    }

    override fun resolve(
        scope: CoroutineScope,
    ): GoBackHandler = lazy.flatMapState(scope) { it }
}