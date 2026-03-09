package hnau.pinfin.model

import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.kotlin.getOrInit
import org.hnau.commons.kotlin.toAccessor
import hnau.pinfin.model.loadbudgets.LoadBudgetsModel
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class RootModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) {

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

    val goBackHandler: GoBackHandler
        get() = loadBudgets.goBackHandler
}