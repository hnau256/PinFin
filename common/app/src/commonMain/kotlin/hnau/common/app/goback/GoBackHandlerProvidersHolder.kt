package hnau.common.app.goback

import hnau.common.kotlin.coroutines.flatMapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class GoBackHandlerProvidersHolder(
    scope: CoroutineScope,
) : GoBackHandlerProvider {

    private val providers: MutableStateFlow<GoBackHandlerProvider> =
        MutableStateFlow(GoBackHandlerProvider.never)

    override val goBackHandler: GoBackHandler =
        providers.flatMapState(scope, GoBackHandlerProvider::goBackHandler)

    fun onNewGoBackHandlerProvider(
        provider: GoBackHandlerProvider,
    ) {
        providers.value = provider
    }
}
