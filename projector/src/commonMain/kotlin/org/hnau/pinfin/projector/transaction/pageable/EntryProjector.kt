package org.hnau.pinfin.projector.transaction.pageable

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.SlideOrientation
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.transaction.pageable.EntryModel
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import org.hnau.pinfin.projector.transaction.utils.createPagesTransitionSpec
import org.hnau.pinfin.projector.utils.ArrowDirection
import org.hnau.pinfin.projector.utils.ArrowIcon
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class EntryProjector(
    scope: CoroutineScope,
    private val model: EntryModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter

        val localization: Localization

        fun account(): AccountProjector.Dependencies

        fun records(): RecordsProjector.Dependencies
    }

    class Page(
        scope: CoroutineScope,
        model: EntryModel.Page,
        private val dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            fun records(): RecordsProjector.Page.Dependencies

            fun accountProjectorCompanion(): AccountProjector.Companion.Dependencies

            val localization: Localization
        }

        sealed interface PageType {

            val key: Int

            @Composable
            fun Content(
                modifier: Modifier,
                contentPadding: PaddingValues,
                dependencies: Dependencies,
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
                    dependencies: Dependencies,
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
                    dependencies: Dependencies,
                ) {
                    projector.Content(
                        modifier = modifier.padding(contentPadding),
                        messages = AccountProjector.chooseMessages(
                            dependencies = dependencies.accountProjectorCompanion(),
                        ),
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
                            dependencies = dependencies.accountProjectorCompanion(),
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
                        dependencies = dependencies,
                    )
                }
        }
    }

    private val records = RecordsProjector(
        scope = scope,
        model = model.records,
        dependencies = dependencies.records(),
    )

    private val account = AccountProjector(
        model = model.account,
        dependencies = dependencies.account(),
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
            org.hnau.pinfin.projector.utils.AmountContent(
                value = model
                    .amountOrZero
                    .collectAsState()
                    .value,
                amountFormatter = dependencies.amountFormatter,
            )
        }
    }
}