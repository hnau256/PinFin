package hnau.pinfin.projector.categorystack

import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.pinfin.model.categorystack.CategoryStackElementModel
import hnau.pinfin.model.categorystack.CategoryStackModel
import hnau.pinfin.model.categorystack.fold
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

    @SealUp(
        variants = [
            Variant(
                type = CategoryProjector::class,
                identifier = "info",
            ),
            Variant(
                type = IconProjector::class,
                identifier = "icon",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "CategoryStackElementProjector",
    )
    interface PageProjector {

        @Composable
        fun Content()

        companion object
    }

    private val tail: StateFlow<StackProjectorTail<Int, CategoryStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = CategoryStackElementModel::ordinal,
            createProjector = { scope, model ->
                model.fold(
                    ifInfo = { infoModel ->
                        PageProjector.info(
                            scope = scope,
                            model = infoModel,
                            dependencies = dependencies.info(),
                        )
                    },
                    ifIcon = { iconModel ->
                        PageProjector.icon(
                            scope = scope,
                            model = iconModel,
                            dependencies = dependencies.icon(),
                        )
                    },
                )
            }
        )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.Content()
        }
    }
}