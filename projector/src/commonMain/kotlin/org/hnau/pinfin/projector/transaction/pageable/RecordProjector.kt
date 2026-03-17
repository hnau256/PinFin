package org.hnau.pinfin.projector.transaction.pageable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.ItemsRow
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.SlideOrientation
import org.hnau.commons.app.projector.utils.copy
import org.hnau.commons.app.projector.utils.getTransitionSpecForSlideByCompare
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.model.transaction.pageable.RecordModel
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.utils.ChooseOrCreateMessages
import org.hnau.pinfin.projector.transaction.utils.ChooseOrCreateProjector
import org.hnau.pinfin.projector.utils.CategoryContent
import org.hnau.pinfin.projector.utils.Label
import org.hnau.pinfin.projector.utils.UIConstants
import org.hnau.pinfin.projector.utils.ViewMode
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class RecordProjector(
    private val model: RecordModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val amountFormatter: AmountFormatter

        val localization: Localization

        fun amount(): AmountWithDirectionProjector.Dependencies
    }

    class Page(
        scope: CoroutineScope,
        private val model: RecordModel.Page,
        private val dependencies: Dependencies,
    ) {

        @Pipe
        interface Dependencies {

            val localization: Localization

            fun amountPage(): AmountProjector.Page.Dependencies

            fun amount(): AmountWithDirectionProjector.Dependencies

            fun comment(): CommentProjector.Dependencies

            fun categoryCompanion(): CategoryProjector.Companion.Dependencies

            fun category(): CategoryProjector.Dependencies
        }

        sealed interface PageType {

            val key: Int

            @Composable
            fun Content(
                modifier: Modifier,
                contentPadding: PaddingValues,
                localization: Localization,
            )

            data class Comment(
                val projector: CommentProjector.Page,
            ) : PageType {
                override val key: Int
                    get() = 0

                @Composable
                override fun Content(
                    modifier: Modifier,
                    contentPadding: PaddingValues,
                    localization: Localization,
                ) {
                    projector.Content(
                        modifier = modifier,
                        contentPadding = contentPadding,
                    )
                }
            }

            data class Category(
                val projector: ChooseOrCreateProjector<CategoryInfo>,
            ) : PageType {
                override val key: Int
                    get() = 1

                @Composable
                override fun Content(
                    modifier: Modifier,
                    contentPadding: PaddingValues,
                    localization: Localization,
                ) {
                    projector.Content(
                        modifier = modifier.padding(contentPadding),
                        messages = ChooseOrCreateMessages(
                            createNew = localization.createNewCategory,
                            notFound = localization.categoriesNotFound,
                            noVariants = localization.thereAreNoCategories,
                        )
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
                    localization: Localization,
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
                    is RecordModel.PageType.Amount -> PageType.Amount(
                        projector = AmountProjector.Page(
                            scope = scope,
                            dependencies = dependencies.amountPage(),
                            model = type.model,
                        )
                    )

                    is RecordModel.PageType.Category -> PageType.Category(
                        projector = CategoryProjector.createPage(
                            model = type.model,
                            dependencies = dependencies.categoryCompanion(),
                        )
                    )

                    is RecordModel.PageType.Comment -> PageType.Comment(
                        projector = CommentProjector.Page(
                            model = type.model,
                        )
                    )
                }
            }

        @Composable
        private fun Type(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            type
                .collectAsState()
                .value
                .StateContent(
                    modifier = modifier,
                    label = "TransferPage",
                    contentKey = { it.key },
                    transitionSpec = getTransitionSpecForSlideByCompare(
                        orientation = SlideOrientation.Horizontal,
                        extractComparable = PageType::key,
                    )
                ) { type ->
                    type.Content(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = contentPadding,
                        localization = dependencies.localization,
                    )
                }
        }

        private val comment = CommentProjector(
            model = model.comment,
            dependencies = dependencies.comment(),
        )

        private val category = CategoryProjector(
            model = model.category,
            dependencies = dependencies.category(),
        )

        private val amount = AmountWithDirectionProjector(
            dependencies = dependencies.amount(),
            model = model.amount,
        )

        @Composable
        private fun Top(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            OutlinedCard(
                modifier = modifier
                    .padding(contentPadding)
                    .padding(horizontal = Dimens.separation),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.smallSeparation),
                    verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                    ) {
                        comment.Content(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                        model
                            .remove
                            .collectAsState()
                            .value
                            .NullableStateContent(
                                modifier = Modifier.fillMaxHeight(),
                                transitionSpec = TransitionSpec.horizontal(),
                            ) { remove ->
                                IconButton(
                                    onClick = remove,
                                    modifier = Modifier.padding(
                                        start = Dimens.separation,
                                    )
                                ) {
                                    Icon(Icons.Filled.Delete)
                                }
                            }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        category.Content(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                        )
                        Spacer(Modifier.width(Dimens.smallSeparation))
                        amount.Content(
                            modifier = Modifier
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }

        @Composable
        fun Content(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        ) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
            ) {
                Top(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = contentPadding.copy(bottom = 0.dp),
                )
                Type(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding.copy(top = 0.dp),
                )
            }
        }
    }

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {

        val onClick = model.requestFocus
        val selected = model.isFocused.collectAsState().value

        model
            .categoryWithAmount
            .collectAsState()
            .value
            .foldNullable(
                ifNull = {
                    Label(
                        modifier = modifier,
                        containerColor = UIConstants.absentValueColor,
                        onClick = onClick,
                        selected = selected,
                    ) {
                        Icon(
                            icon = UIConstants.absentValueIcon
                        )
                    }
                },
                ifNotNull = { (category, amount) ->
                    CategoryContent(
                        modifier = modifier,
                        info = category,
                        onClick = onClick,
                        selected = selected,
                        localization = dependencies.localization,
                    ) { category ->
                        ItemsRow {
                            category()
                            Text(
                                text = dependencies
                                    .amountFormatter
                                    .format(
                                        amount = amount,
                                        alwaysShowSign = false,
                                        alwaysShowCents = false,
                                    ),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
            )
    }

    companion object {

        val size: Dp = 48.dp
    }
}