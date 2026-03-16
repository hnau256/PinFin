package org.hnau.pinfin.projector.accountstack

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.stack.Content
import org.hnau.commons.app.projector.stack.StackProjectorTail
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.pinfin.model.accountstack.AccountStackElementModel
import org.hnau.pinfin.model.accountstack.AccountStackModel
import org.hnau.pinfin.model.accountstack.fold
import org.hnau.pinfin.projector.IconProjector

class AccountStackProjector(
    private val scope: CoroutineScope,
    private val model: AccountStackModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun info(): AccountProjector.Dependencies

        fun icon(): IconProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = AccountProjector::class,
                identifier = "info",
            ),
            Variant(
                type = IconProjector::class,
                identifier = "icon",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "AccountStackElementProjector",
    )
    interface PageProjector {

        @Composable
        fun Content()

        companion object
    }

    private val tail: StateFlow<StackProjectorTail<Int, AccountStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = AccountStackElementModel::ordinal,
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