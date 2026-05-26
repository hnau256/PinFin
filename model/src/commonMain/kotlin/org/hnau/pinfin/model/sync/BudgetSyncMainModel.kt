@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import kotlin.time.Duration.Companion.seconds

class BudgetSyncMainModel(
    scope: CoroutineScope,
    val config: StateFlow<SyncConfig>,
    val openConfig: () -> Unit,
) {

    val sync = actionOrCancelIfExecuting(scope) {
        delay(3.seconds)
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}