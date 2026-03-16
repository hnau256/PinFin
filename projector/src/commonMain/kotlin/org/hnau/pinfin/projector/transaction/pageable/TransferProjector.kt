package org.hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.ItemsRow
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.SlideOrientation
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.transaction.pageable.TransferModel
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import org.hnau.pinfin.projector.transaction.utils.createPagesTransitionSpec
import org.hnau.pinfin.projector.utils.ArrowDirection
import org.hnau.pinfin.projector.utils.ArrowIcon

class TransferProjector(
    model: TransferModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun amount(): AmountProjector.Dependencies

        fun account(): AccountProjector.Dependencies
    }

    class Page(
        scope: CoroutineScope,
        model: TransferModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            fun amount(): AmountProjector.Page.Dependencies

            fun accountCompanion(): AccountProjector.Companion.Dependencies
        }

        sealed interface PageType {

            val key: Int

            @Composable
            fun Content(
                modifier: Modifier,
                contentPadding: PaddingValues,
            )

            data class From(
                val projector: ChooseOrCreateProjector<AccountInfo>,
                private val dependencies: Dependencies,
            ) : PageType {
                override val key: Int
                    get() = 0

                @Composable
                override fun Content(
                    modifier: Modifier,
                    contentPadding: PaddingValues,
                ) {
                    projector.Content(
                        modifier = modifier.padding(contentPadding),
                        messages = AccountProjector.chooseMessages(
                            dependencies = dependencies.accountCompanion(),
                        ),
                    )
                }
            }

            data class To(
                val projector: ChooseOrCreateProjector<AccountInfo>,
                private val dependencies: Dependencies,
            ) : PageType {
                override val key: Int
                    get() = 1

                @Composable
                override fun Content(
                    modifier: Modifier,
                    contentPadding: PaddingValues,
                ) {
                    projector.Content(
                        modifier = modifier.padding(contentPadding),
                        messages = AccountProjector.chooseMessages(
                            dependencies = dependencies.accountCompanion(),
                        ),
                    )
                }
            }

            data class Amount(
                val projector: AmountProjector.Page,
            ) : PageType {
                override val key: Int
                    get() = 2

                @Composable
                override fun Content(
                    modifier: Modifier,
                    contentPadding: PaddingValues,
                ) {
                    projector.Content(
                        modifier = modifier,
                        contentPadding = contentPadding,
                    )
                }
            }
        }

        private val type: StateFlow<PageType> = model
            .page
            .mapWithScope(scope) { scope, type ->
                when (type) {
                    is TransferModel.PageType.Amount -> PageType.Amount(
                        projector = AmountProjector.Page(
                            scope = scope,
                            model = type.model,
                            dependencies = dependencies.amount(),
                        )
                    )

                    is TransferModel.PageType.From -> PageType.From(
                        projector = AccountProjector.createPage(
                            model = type.model,
                            dependencies = dependencies.accountCompanion(),
                        ),
                        dependencies = dependencies,
                    )

                    is TransferModel.PageType.To -> PageType.To(
                        projector = AccountProjector.createPage(
                            model = type.model,
                            dependencies = dependencies.accountCompanion(),
                        ),
                        dependencies = dependencies,
                    )
                }
            }

        @Composable
        fun Content(
            contentPadding: PaddingValues,
            modifier: Modifier = Modifier,
        ) {
            type
                .collectAsState()
                .value
                .StateContent(
                    modifier = modifier,
                    label = "TransferPage",
                    contentKey = PageType::key,
                    transitionSpec = createPagesTransitionSpec(
                        orientation = SlideOrientation.Horizontal,
                        extractIndex = PageType::key,
                    )
                ) { type ->
                    type.Content(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = contentPadding,
                    )
                }
        }
    }

    private val from = AccountProjector(
        model = model.from,
        dependencies = dependencies.account(),
    )

    private val to = AccountProjector(
        model = model.to,
        dependencies = dependencies.account(),
    )

    private val amount = AmountProjector(
        model = model.amount,
        dependencies = dependencies.amount(),
    )

    @Composable
    fun MainContent(
        modifier: Modifier = Modifier,
    ) {
        ItemsRow(
            modifier = modifier,
        ) {
            from.Content(
                modifier = Modifier.weight(1f),
            )
            Icon(
                icon = ArrowIcon[ArrowDirection.StartToEnd],
            )
            to.Content(
                modifier = Modifier.weight(1f),
            )
        }
    }

    @Composable
    fun AmountContent(
        modifier: Modifier = Modifier,
    ) {
        amount.Content(
            modifier = modifier,
        )
    }
}