package hnau.pinfin.projector.manage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.common.kotlin.coroutines.flow.state.mapWithScope
import hnau.pinfin.model.manage.ManageModel
import hnau.pinfin.model.manage.fold
import hnau.pinfin.projector.IconProjector
import hnau.pinfin.projector.budgetsstack.BudgetsStackProjector
import hnau.pinfin.projector.budgetstack.BudgetStackProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

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
