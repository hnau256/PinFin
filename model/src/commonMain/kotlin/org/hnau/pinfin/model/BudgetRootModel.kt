package org.hnau.pinfin.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budgetstack.BudgetStackModel

class BudgetRootModel(
    scope: CoroutineScope,
    skeleton: Skeleton,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun stack(): BudgetStackModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val stack: BudgetStackModel.Skeleton = BudgetStackModel.Skeleton()
    )

    val stack = BudgetStackModel(
        scope = scope,
        skeleton = skeleton.stack,
        dependencies = dependencies.stack(),
    )

    val goBackHandler: GoBackHandler
        get() = stack.goBackHandler
}