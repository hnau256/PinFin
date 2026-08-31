package org.hnau.pinfin.projector.transaction.delegates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.getTransitionSpecForSlideByCompare
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.transaction.TransactionModel
import org.hnau.pinfin.model.transaction.fold
import org.hnau.pinfin.projector.transaction.pageable.CommentProjector
import org.hnau.pinfin.projector.transaction.pageable.DateProjector
import org.hnau.pinfin.projector.transaction.pageable.TimeProjector

class PageProjector(
    scope: CoroutineScope,
    model: TransactionModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun type(): TypeProjector.Page.Dependencies
    }

    @Fold
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
            val projector = model.fold(
                ifComment = { model ->
                    Part.Comment(
                        projector = CommentProjector.Page(
                            model = model,
                        )
                    )
                },
                ifDate = { model ->
                    Part.Date(
                        projector = DateProjector.Page(
                            model = model,
                        )
                    )
                },
                ifTime = { model ->
                    Part.Time(
                        projector = TimeProjector.Page(
                            model = model,
                        )
                    )
                },
                ifType = { model ->
                    Part.Type(
                        projector = TypeProjector.Page(
                            scope = scope,
                            model = model,
                            dependencies = dependencies.type(),
                        )
                    )
                },
            )
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
                transitionSpec = getTransitionSpecForSlideByCompare(
                    orientation = Orientation.Horizontal,
                    extractComparable = { it.first },
                )
            ) { (_, page) ->
                page.Content(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                )
            }
    }
}