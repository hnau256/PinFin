package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.stack.Content
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.BudgetRootModel
import org.hnau.pinfin.projector.budgetstack.BudgetStackProjector

class BudgetRootProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    model: BudgetRootModel,
) {

    @Pipe
    interface Dependencies {

        fun stack(): BudgetStackProjector.Dependencies
    }

    private val stack = BudgetStackProjector(
        scope = scope,
        model = model.stack,
        dependencies = dependencies.stack(),
    )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        stack.Content(
            contentPadding = contentPadding,
        )
    }
}