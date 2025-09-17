package hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.transaction.pageable.EntryModel
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import hnau.pinfin.projector.transaction.utils.createPagesTransitionSpec
import hnau.pinfin.projector.utils.ArrowDirection
import hnau.pinfin.projector.utils.ArrowIcon
import hnau.pinfin.projector.utils.formatter.AmountFormatter
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class EntryProjector(
    scope: CoroutineScope,
    private val model: EntryModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter
    }

    class Page(
        scope: CoroutineScope,
        model: EntryModel.Page,
        dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            fun records(): RecordsProjector.Page.Dependencies
        }

        sealed interface PageType {

            val key: Int

            @Composable
            fun Content(
                modifier: Modifier,
                contentPadding: PaddingValues,
            )

            data class Records(
                val projector: RecordsProjector.Page,
            ) : PageType {
                override val key: Int
                    get() = 0

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

            data class Account(
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
        }

        private val type: StateFlow<PageType> = model
            .page
            .mapWithScope(scope) { scope, type ->
                when (type) {
                    is EntryModel.PageType.Records -> PageType.Records(
                        projector = RecordsProjector.Page(
                            scope = scope,
                            model = type.model,
                            dependencies = dependencies.records(),
                        )
                    )

                    is EntryModel.PageType.Account -> PageType.Account(
                        projector = AccountProjector.createPage(
                            model = type.model,
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
                    label = "EntryPage",
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

    private val records = RecordsProjector(
        scope = scope,
        model = model.records,
    )

    private val account = AccountProjector(
        model = model.account,
    )

    @Composable
    fun MainContent(
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            records.Content(
                modifier = Modifier
                    .fillMaxHeight(),
            )
            Icon(
                icon = ArrowIcon[ArrowDirection.Both],
            )
            account.Content(
                modifier = Modifier
                    .fillMaxSize(),
            )
        }
    }

    @Composable
    fun AmountContent(
        modifier: Modifier = Modifier,
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            hnau.pinfin.projector.utils.AmountContent(
                value = model
                    .amountOrZero
                    .collectAsState()
                    .value,
                amountFormatter = dependencies.amountFormatter,
            )
        }
    }
}