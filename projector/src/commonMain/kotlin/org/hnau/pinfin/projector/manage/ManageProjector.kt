package org.hnau.pinfin.projector.manage

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.manage.ManageModel
import org.hnau.pinfin.model.manage.fold
import org.hnau.pinfin.projector.BudgetRootProjector
import org.hnau.pinfin.projector.CreateBudgetProjector
import org.hnau.pinfin.projector.IconProjector

class ManageProjector(
    scope: CoroutineScope,
    model: ManageModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun createBudget(): CreateBudgetProjector.Dependencies

        fun budget(): BudgetRootProjector.Dependencies

        fun icon(): IconProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = CreateBudgetProjector::class,
                identifier = "createBudget",
            ),
            Variant(
                type = BudgetRootProjector::class,
                identifier = "budget",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "ManageElementProjector",
    )
    interface StateProjector {

        @Composable
        fun Content(
            contentPadding: PaddingValues,
        )

        companion object
    }

    private val state: StateFlow<ManageElementProjector> = model
        .state
        .mapWithScope(scope) { scope, state ->
            state.fold(
                ifCreateBudget = { createBudgetModel ->
                    StateProjector.createBudget(
                        scope = scope,
                        dependencies = dependencies.createBudget(),
                        model = createBudgetModel,
                    )
                },
                ifBudget = { budgetModel ->
                    StateProjector.budget(
                        scope = scope,
                        dependencies = dependencies.budget(),
                        model = budgetModel,
                    )
                },
            )
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        state
            .collectAsState()
            .value
            .StateContent(
                modifier = Modifier.fillMaxSize(),
                label = "Manage",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = ManageElementProjector::ordinal,
            ) { elementProjector ->
                elementProjector.Content(
                    contentPadding = contentPadding,
                )
            }
    }
}
