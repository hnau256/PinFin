package org.hnau.pinfin.model.budget.manage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifTrue

class BudgetMCPModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies

    @Serializable
    data class Skeleton(
        val mcpIsEnabled: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    val mcpIsEnabled: MutableStateFlow<Boolean>
        get() = skeleton.mcpIsEnabled

    init {
        scope.launch {
            mcpIsEnabled.collectLatest { mcpIsEnabled ->
                mcpIsEnabled.ifTrue {
                    launchMCP()
                }
            }
        }
    }

    private suspend fun launchMCP(): Nothing {
        TODO()
    }
}