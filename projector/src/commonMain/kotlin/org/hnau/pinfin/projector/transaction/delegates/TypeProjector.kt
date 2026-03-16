package org.hnau.pinfin.projector.transaction.delegates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.Tabs
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.app.projector.utils.SlideOrientation
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.model.transaction.TransactionModel
import org.hnau.pinfin.model.transaction.pageable.TypeModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.pageable.EntryProjector
import org.hnau.pinfin.projector.transaction.pageable.TransferProjector
import org.hnau.pinfin.projector.transaction.utils.createPagesTransitionSpec
import org.hnau.pinfin.projector.utils.title

class TypeProjector(
    scope: CoroutineScope,
    private val model: TransactionModel,
    private val dependencies: Dependencies,
) {

    sealed interface Type {

        val key: TransactionType

        @Composable
        fun MainContent(
            modifier: Modifier = Modifier,
        )

        @Composable
        fun AmountContent(
            modifier: Modifier = Modifier,
        )

        data class Entry(
            val projector: EntryProjector,
        ) : Type {

            override val key: TransactionType
                get() = TransactionType.Entry

            @Composable
            override fun MainContent(
                modifier: Modifier,
            ) {
                projector.MainContent(
                    modifier = modifier,
                )
            }

            @Composable
            override fun AmountContent(
                modifier: Modifier,
            ) {
                projector.AmountContent(
                    modifier = modifier,
                )
            }
        }

        data class Transfer(
            val projector: TransferProjector,
        ) : Type {

            override val key: TransactionType
                get() = TransactionType.Transfer

            @Composable
            override fun MainContent(
                modifier: Modifier,
            ) {
                projector.MainContent(
                    modifier = modifier,
                )
            }

            @Composable
            override fun AmountContent(
                modifier: Modifier,
            ) {
                projector.AmountContent(
                    modifier = modifier,
                )
            }
        }
    }

    @Pipe
    interface Dependencies {

        val localization: Localization


        fun entry(): EntryProjector.Dependencies


        fun transfer(): TransferProjector.Dependencies
    }

    class Page(
        scope: CoroutineScope,
        private val model: TypeModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            fun entry(): EntryProjector.Page.Dependencies

            fun transfer(): TransferProjector.Page.Dependencies
        }

        sealed interface Type {

            val key: TransactionType

            @Composable
            fun Content(
                modifier: Modifier = Modifier,
                contentPadding: PaddingValues,
            )

            data class Entry(
                val projector: EntryProjector.Page,
            ) : Type {

                override val key: TransactionType
                    get() = TransactionType.Entry

                @Composable
                override fun Content(
                    modifier: Modifier,
                    contentPadding: PaddingValues
                ) {
                    projector.Content(
                        modifier = modifier,
                        contentPadding = contentPadding,
                    )
                }
            }

            data class Transfer(
                val projector: TransferProjector.Page,
            ) : Type {

                override val key: TransactionType
                    get() = TransactionType.Transfer

                @Composable
                override fun Content(
                    modifier: Modifier,
                    contentPadding: PaddingValues
                ) {
                    projector.Content(
                        modifier = modifier,
                        contentPadding = contentPadding,
                    )
                }
            }
        }

        private val type: StateFlow<Type> = model
            .page
            .mapWithScope(scope) { scope, type ->
                when (type) {
                    is TypeModel.Page.Type.Entry -> Type.Entry(
                        projector = EntryProjector.Page(
                            scope = scope,
                            dependencies = dependencies.entry(),
                            model = type.model,
                        )
                    )

                    is TypeModel.Page.Type.Transfer -> Type.Transfer(
                        projector = TransferProjector.Page(
                            scope = scope,
                            dependencies = dependencies.transfer(),
                            model = type.model,
                        )
                    )
                }
            }

        @Composable
        fun Content(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            type
                .collectAsState()
                .value
                .StateContent(
                    modifier = modifier,
                    label = "TransactionPage",
                    contentKey = { it.key },
                    transitionSpec = createPagesTransitionSpec(
                        orientation = SlideOrientation.Horizontal,
                    ) { type ->
                        type.key.ordinal
                    }
                ) { type ->
                    type.Content(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = contentPadding,
                    )
                }
        }
    }

    @Composable
    fun HeaderContent(
        modifier: Modifier = Modifier,
    ) {
        Tabs(
            modifier = modifier,
            items = remember { TransactionType.entries.toImmutableList() },
            selected = model.type.variant.collectAsState().value,
            onSelectedChanged = { model.type.variant.value = it },
        ) { type ->
            Text(
                text = type.title(
                    localization = dependencies.localization,
                ),
            )
        }
    }

    private val type: StateFlow<Type> = model
        .type
        .typeModel
        .mapWithScope(scope) { scope, model ->
            when (model) {
                is TypeModel.Type.Entry -> Type.Entry(
                    projector = EntryProjector(
                        scope = scope,
                        model = model.model,
                        dependencies = dependencies.entry(),
                    )
                )

                is TypeModel.Type.Transfer -> Type.Transfer(
                    projector = TransferProjector(
                        model = model.model,
                        dependencies = dependencies.transfer(),
                    )
                )
            }
        }

    @Composable
    fun MainContent(
        modifier: Modifier = Modifier,
    ) {
        type
            .collectAsState()
            .value
            .StateContent(
                transitionSpec = TransitionSpec.crossfade(),
                label = "TransactionTypeMainContent",
                contentKey = { typeProjector -> typeProjector.key },
            ) { typeProjector ->
                typeProjector.MainContent(
                    modifier = modifier,
                )
            }
    }

    @Composable
    fun AmountContent(
        modifier: Modifier = Modifier,
    ) {
        type
            .collectAsState()
            .value
            .StateContent(
                transitionSpec = TransitionSpec.crossfade(),
                label = "TransactionTypeAmountContent",
                contentKey = { typeProjector -> typeProjector.key },
            ) { typeProjector ->
                typeProjector.AmountContent(
                    modifier = modifier,
                )
            }
    }
}