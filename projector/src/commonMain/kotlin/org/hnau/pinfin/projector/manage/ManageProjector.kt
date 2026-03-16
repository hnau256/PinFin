package org.hnau.pinfin.projector.manage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.manage.ManageModel
import org.hnau.pinfin.model.manage.fold
import org.hnau.pinfin.projector.IconProjector
import org.hnau.pinfin.projector.budgetsstack.BudgetsStackProjector
import org.hnau.pinfin.projector.budgetstack.BudgetStackProjector

class ManageProjector(
    scope: CoroutineScope,
    model: ManageModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun budgetsStack(): BudgetsStackProjector.Dependencies

        fun budgetStack(): BudgetStackProjector.Dependencies

        fun icon(): IconProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = BudgetsStackProjector::class,
                identifier = "budgetsStack",
            ),
            Variant(
                type = BudgetStackProjector::class,
                identifier = "budgetStack",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "ManageElementProjector",
    )
    interface StateProjector {

        @Composable
        fun Content()

        companion object
    }

    private val state: StateFlow<ManageElementProjector> = model
        .state
        .mapWithScope(scope) { scope, state ->
            state.fold(
                ifBudgetsStack = { budgetsStackModel ->
                    StateProjector.budgetsStack(
                        scope = scope,
                        dependencies = dependencies.budgetsStack(),
                        model = budgetsStackModel,
                    )
                },
                ifBudgetStack = { budgetStackModel ->
                    StateProjector.budgetStack(
                        scope = scope,
                        dependencies = dependencies.budgetStack(),
                        model = budgetStackModel,
                    )
                },
            )
        }

    @Composable
    fun Content() {
        state
            .collectAsState()
            .value
            .StateContent(
                modifier = Modifier.fillMaxSize(),
                label = "Manage",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = ManageElementProjector::ordinal,
            ) { elementProjector ->
                elementProjector.Content()
            }
    }
}
