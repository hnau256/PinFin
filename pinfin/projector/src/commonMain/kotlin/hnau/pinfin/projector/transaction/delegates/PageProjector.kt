package hnau.pinfin.projector.transaction.delegates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.utils.getTransitionSpecForHorizontalSlide
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.transaction.pageable.CommentProjector
import hnau.pinfin.projector.transaction.pageable.DateProjector
import hnau.pinfin.projector.transaction.pageable.TimeProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sign

class PageProjector(
    scope: CoroutineScope,
    model: TransactionModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun date(): DateProjector.Page.Dependencies

        fun time(): TimeProjector.Page.Dependencies

        fun comment(): CommentProjector.Page.Dependencies

        fun type(): TypeProjector.Page.Dependencies
    }

    sealed interface Part {

        @Composable
        fun Content(
            modifier: Modifier,
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
        .mapWithScope(scope) { pageScope, (page, model) ->
            val projector = when (model) {
                is TransactionModel.PageType.Date -> Part.Date(
                    projector = DateProjector.Page(
                        scope = pageScope,
                        model = model.model,
                        dependencies = dependencies.date(),
                    )
                )

                is TransactionModel.PageType.Comment -> Part.Comment(
                    projector = CommentProjector.Page(
                        scope = pageScope,
                        model = model.model,
                        dependencies = dependencies.comment(),
                    )
                )
                is TransactionModel.PageType.Time -> Part.Time(
                    projector = TimeProjector.Page(
                        scope = pageScope,
                        model = model.model,
                        dependencies = dependencies.time(),
                    )
                )
                is TransactionModel.PageType.Type -> Part.Type(
                    projector = TypeProjector.Page(
                        scope = pageScope,
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
                contentKey = { it.first },
                transitionSpec = getTransitionSpecForHorizontalSlide {
                    (targetState.first.ordinal - initialState.first.ordinal).sign * 0.5
                }
            ) { (_, page) ->
                page.Content(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                )
            }
    }
}