package hnau.pinfin.projector.transaction.delegates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.kotlin.coroutines.flow.state.mapWithScope
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.transaction.pageable.CommentProjector
import hnau.pinfin.projector.transaction.pageable.DateProjector
import hnau.pinfin.projector.transaction.pageable.TimeProjector
import hnau.pinfin.projector.transaction.utils.createPagesTransitionSpec
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class PageProjector(
    scope: CoroutineScope,
    model: TransactionModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun type(): TypeProjector.Page.Dependencies
    }

    sealed interface Part {

        @Composable
        fun Content(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        )

        data class Date(
            val projector: DateProjector.Page,
        ) : Part {

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

        data class Time(
            val projector: TimeProjector.Page,
        ) : Part {

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

        data class Comment(
            val projector: CommentProjector.Page,
        ) : Part {

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

        data class Type(
            val projector: TypeProjector.Page,
        ) : Part {

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

    private val page: StateFlow<Pair<TransactionModel.Part, Part>> = model
        .pageType
        .mapWithScope(scope) { scope, (page, model) ->
            val projector = when (model) {
                is TransactionModel.PageType.Date -> Part.Date(
                    projector = DateProjector.Page(
                        model = model.model,
                    )
                )

                is TransactionModel.PageType.Comment -> Part.Comment(
                    projector = CommentProjector.Page(
                        model = model.model,
                    )
                )

                is TransactionModel.PageType.Time -> Part.Time(
                    projector = TimeProjector.Page(
                        model = model.model,
                    )
                )

                is TransactionModel.PageType.Type -> Part.Type(
                    projector = TypeProjector.Page(
                        scope = scope,
                        model = model.model,
                        dependencies = dependencies.type(),
                    )
                )
            }
            page to projector
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
        modifier: Modifier = Modifier,
    ) {
        page
            .collectAsState()
            .value
            .StateContent(
                modifier = modifier,
                label = "TransactionPage",
                contentKey = Pair<TransactionModel.Part, *>::first,
                transitionSpec = createPagesTransitionSpec(
                    orientation = SlideOrientation.Horizontal,
                ) { it.first.ordinal }
            ) { (_, page) ->
                page.Content(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                )
            }
    }
}