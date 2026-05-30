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

        fun sync(): BudgetSyncDelegate.Dependencies

        fun stack(
            sync: BudgetSyncDelegate,
        ): BudgetStackModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val sync: BudgetSyncDelegate.Skeleton = BudgetSyncDelegate.Skeleton(),
        val stack: BudgetStackModel.Skeleton = BudgetStackModel.Skeleton(),
    )

    private val sync = BudgetSyncDelegate(
        scope = scope,
        dependencies = dependencies.sync(),
        skeleton = skeleton.sync,
    )

    val stack = BudgetStackModel(
        scope = scope,
        skeleton = skeleton.stack,
        dependencies = dependencies.stack(
            sync = sync,
        ),
    )

    val goBackHandler: GoBackHandler
        get() = stack.goBackHandler
}