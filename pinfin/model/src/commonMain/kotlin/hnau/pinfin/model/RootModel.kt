package hnau.pinfin.model

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.loadbudgets.LoadBudgetsModel
import hnau.pinfin.model.utils.icons.IconVariant
import hnau.pinfin.model.utils.icons.icon
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

        fun icon(): IconModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var loadBudgets: LoadBudgetsModel.Skeleton? = null,
        var icon: IconModel.Skeleton? = null,
    )

    val loadBudgets = LoadBudgetsModel(
        scope = scope,
        dependencies = dependencies.loadBudgets(),
        skeleton = skeleton::loadBudgets
            .toAccessor()
            .getOrInit { LoadBudgetsModel.Skeleton() },
    )

    val icon = IconModel(
        scope = scope,
        dependencies = dependencies.icon(),
        skeleton = skeleton::icon
            .toAccessor()
            .getOrInit { IconModel.Skeleton() },
        selected = IconVariant.entries[12].icon,
        onSelect = {},
    )

    override val goBackHandler: GoBackHandler
        get() = loadBudgets.goBackHandler
}