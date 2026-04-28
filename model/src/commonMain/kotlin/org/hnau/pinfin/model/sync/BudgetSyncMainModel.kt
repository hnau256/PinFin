@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.Stickable
import org.hnau.commons.kotlin.coroutines.flow.state.stateFlowOfNotNull
import org.hnau.commons.kotlin.coroutines.flow.state.stick
import org.hnau.commons.kotlin.coroutines.flow.state.stickNotNull
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class BudgetSyncMainModel(
    scope: CoroutineScope,
    config: StateFlow<SyncConfig?>,
    removeConfig: suspend () -> Unit,
    val openConfig: () -> Unit,
) {

    val config: StateFlow<StateFlow<SyncConfig>?> = config.stickNotNull(scope)

    val removeConfig: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> = actionOrCancelIfExecuting(
        scope = scope,
        operation = removeConfig,
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}