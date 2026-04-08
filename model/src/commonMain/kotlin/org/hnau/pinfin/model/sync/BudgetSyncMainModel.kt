@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class BudgetSyncMainModel(
    scope: CoroutineScope,
    val config: StateFlow<SyncConfig?>,
    removeConfig: suspend () -> Unit,
    openConfig: () -> Unit,
) {

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}