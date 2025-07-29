package hnau.pinfin.projector.categorystack

import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.pinfin.model.categorystack.CategoryStackElementModel
import hnau.pinfin.model.categorystack.CategoryStackModel
import hnau.pinfin.projector.IconProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class CategoryStackProjector(
    private val scope: CoroutineScope,
    private val model: CategoryStackModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun info(): CategoryProjector.Dependencies

        fun icon(): IconProjector.Dependencies
    }

    private val tail: StateFlow<StackProjectorTail<Int, CategoryStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is CategoryStackElementModel.Info -> CategoryStackElementProjector.Info(
                        CategoryProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.info(),
                        )
                    )

                    is CategoryStackElementModel.Icon -> CategoryStackElementProjector.Icon(
                        IconProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.icon(),
                        )
                    )
                }
            }
        )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.Content()
        }
    }
}