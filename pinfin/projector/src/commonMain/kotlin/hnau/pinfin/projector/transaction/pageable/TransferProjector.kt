package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.transaction.pageable.TransferModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import hnau.pinfin.projector.transaction.utils.createPagesTransitionSpec
import hnau.pinfin.projector.utils.ArrowDirection
import hnau.pinfin.projector.utils.ArrowIcon
import hnau.common.app.projector.utils.SlideOrientation
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class TransferProjector(
    scope: CoroutineScope,
    model: TransferModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun account(): AccountProjector.Dependencies

        fun amount(): AmountProjector.Dependencies
    }

    class Page(
        scope: CoroutineScope,
        model: TransferModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            fun chooseOrCreate(): ChooseOrCreateProjector.Dependencies

            fun amount(): AmountProjector.Page.Dependencies
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
                        messages = AccountProjector.chooseMessages,
                    )
                }
            }

            data class To(
                val projector: ChooseOrCreateProjector<AccountInfo>,
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
                        messages = AccountProjector.chooseMessages,
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
                            scope = scope,
                            model = type.model,
                            dependencies = dependencies.chooseOrCreate(),
                        )
                    )

                    is TransferModel.PageType.To -> PageType.To(
                        projector = AccountProjector.createPage(
                            scope = scope,
                            model = type.model,
                            dependencies = dependencies.chooseOrCreate(),
                        )
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
        scope = scope,
        model = model.from,
        dependencies = dependencies.account(),
    )

    private val to = AccountProjector(
        scope = scope,
        model = model.to,
        dependencies = dependencies.account(),
    )

    private val amount = AmountProjector(
        scope = scope,
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