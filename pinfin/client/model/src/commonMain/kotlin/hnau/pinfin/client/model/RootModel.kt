package hnau.pinfin.client.model

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class RootModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        fun loadBudgets(): LoadBudgetsModel.Dependencies
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