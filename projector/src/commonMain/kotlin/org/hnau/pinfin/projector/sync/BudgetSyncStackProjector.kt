package org.hnau.pinfin.projector.sync

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.stack.Content
import org.hnau.commons.app.projector.stack.StackProjectorTail
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.pinfin.model.sync.BudgetSyncStackElementModel
import org.hnau.pinfin.model.sync.BudgetSyncStackModel
import org.hnau.pinfin.model.sync.fold

class BudgetSyncStackProjector(
    private val scope: CoroutineScope,
    private val model: BudgetSyncStackModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun main(): BudgetSyncMainProjector.Dependencies

        fun config(): BudgetSyncConfigProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = BudgetSyncMainProjector::class,
                identifier = "main",
            ),
            Variant(
                type = BudgetSyncConfigProjector::class,
                identifier = "config",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "BudgetSyncStackElementProjector",
    )
    interface Element {

        @Composable
        fun Content(
            contentPadding: PaddingValues,
        )

        companion object
    }

    private val tail: StateFlow<StackProjectorTail<Int, BudgetSyncStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = BudgetSyncStackElementModel::ordinal,
            createProjector = { _, model ->
                model.fold(
                    ifMain = { mainModel ->
                        Element.main(
                            model = mainModel,
                            dependencies = dependencies.main(),
                        )
                    },
                    ifConfig = { configModel ->
                        Element.config(
                            model = configModel,
                            dependencies = dependencies.config(),
                        )
                    },
                )
            }
        )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        tail.Content { elementProjector ->
            elementProjector.Content(
                contentPadding = contentPadding,
            )
        }
    }
}