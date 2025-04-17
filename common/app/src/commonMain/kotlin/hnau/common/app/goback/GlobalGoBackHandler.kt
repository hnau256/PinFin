package hnau.common.app.goback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface GlobalGoBackHandler {

    fun resolve(
        scope: CoroutineScope,
    ): GoBackHandler
}

class GlobalGoBackHandlerImpl(
    private val goBackHandler: GoBackHandler,
) : GlobalGoBackHandler {

    override fun resolve(
        scope: CoroutineScope,
    ): GoBackHandler {
        val result = MutableStateFlow(goBackHandler.value)
        scope.launch {
            goBackHandler
                .onEach { delay(100) /*TODO*/ }
                .collect(result)
        }
        return result
    }
}