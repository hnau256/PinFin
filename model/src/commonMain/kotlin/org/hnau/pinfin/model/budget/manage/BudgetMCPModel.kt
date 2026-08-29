package org.hnau.pinfin.model.budget.manage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial

class BudgetMCPModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

    }

    @Serializable
    data class Skeleton(
        val mcpIsEnabled: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    val mcpIsEnabled: MutableStateFlow<Boolean>
        get() = skeleton.mcpIsEnabled

}