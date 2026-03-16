package org.hnau.pinfin.projector.categorystack

import androidx.compose.runtime.Composable
import org.hnau.commons.app.projector.stack.Content
import org.hnau.commons.app.projector.stack.StackProjectorTail
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.pinfin.model.categorystack.CategoryStackElementModel
import org.hnau.pinfin.model.categorystack.CategoryStackModel
import org.hnau.pinfin.model.categorystack.fold
import org.hnau.pinfin.projector.IconProjector
import org.hnau.commons.gen.pipe.annotations.Pipe
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