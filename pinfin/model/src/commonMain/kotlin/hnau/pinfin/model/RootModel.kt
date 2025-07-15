package hnau.pinfin.model

import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.pinfin.model.loadbudgets.LoadBudgetsModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class RootModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        fun loadBudgets(): LoadBudgetsModel.Dependencies

        companion object
    }

    @Serializable
    data class Skeleton(
        var loadBudgets: LoadBudgetsModel.Skeleton? = null,
    )

    val loadBudgets = LoadBudgetsModel(
        scope = scope,
        dependencies = dependencies.loadBudgets(),
        skeleton = skeleton::loadBudgets
            .toAccessor()
            .getOrInit { LoadBudgetsModel.Skeleton() },
    )

    override val goBackHandler: GoBackHandler
        get() = loadBudgets.goBackHandler
}