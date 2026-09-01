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
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
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

    @SealUp(
        variants = [
            Variant(
                type = DateProjector.Page::class,
                identifier = "date",
            ),
            Variant(
                type = TimeProjector.Page::class,
                identifier = "time",
            ),
            Variant(
                type = CommentProjector.Page::class,
                identifier = "comment",
            ),
            Variant(
                type = TypeProjector.Page::class,
                identifier = "type",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "PageProjectorPart",
    )
    interface Part {

        @Composable
        fun Content(
            modifier: Modifier = Modifier,
            contentPadding: PaddingValues,
        )

        companion object
    }

    private val page: StateFlow<Pair<TransactionModel.Part, PageProjectorPart>> = model
        .pageType
        .mapWithScope(scope) { scope, (page, model) ->
            val projector = model.fold(
                ifComment = { model ->
                    Part.comment(
                        CommentProjector.Page(
                            model = model,
                        )
                    )
                },
                ifDate = { model ->
                    Part.date(
                        DateProjector.Page(
                            model = model,
                        )
                    )
                },
                ifTime = { model ->
                    Part.time(
                        TimeProjector.Page(
                            model = model,
                        )
                    )
                },
                ifType = { model ->
                    Part.type(
                        TypeProjector.Page(
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