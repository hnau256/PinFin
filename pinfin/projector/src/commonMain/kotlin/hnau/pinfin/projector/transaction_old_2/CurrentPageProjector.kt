package hnau.pinfin.projector.transaction_old_2

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.utils.getTransitionSpecForHorizontalSlide
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.transaction_old_2.Part
import hnau.pinfin.model.transaction_old_2.TransactionModel
import hnau.pinfin.model.transaction_old_2.page.CommentPageModel
import hnau.pinfin.model.transaction_old_2.page.DatePageModel
import hnau.pinfin.model.transaction_old_2.page.TimePageModel
import hnau.pinfin.projector.transaction_old_2.page.CommentPageProjector
import hnau.pinfin.projector.transaction_old_2.page.DatePageProjector
import hnau.pinfin.projector.transaction_old_2.page.PageProjector
import hnau.pinfin.projector.transaction_old_2.page.TimePageProjector
import hnau.pinfin.projector.transaction_old_2.part.TypeProjector
import hnau.pinfin.projector.utils.SlideOrientation
import hnau.pinfin.projector.utils.getTransitionSpecForSlide
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sign

class CurrentPageProjector(
    scope: CoroutineScope,
    model: TransactionModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val typeProjector: TypeProjector

        fun date(): DatePageProjector.Dependencies

        fun time(): TimePageProjector.Dependencies

        fun comment(): CommentPageProjector.Dependencies
    }

    private val page: StateFlow<Pair<Part, PageProjector>> = TODO()/*model
        .page
        .mapWithScope(scope) { pageScope, (page, model) ->
            val projector = when (model) {
                is DatePageModel -> DatePageProjector(
                    scope = pageScope,
                    model = model,
                    dependencies = dependencies.date(),
                )
                is TimePageModel -> TimePageProjector(
                    scope = pageScope,
                    model = model,
                    dependencies = dependencies.time(),
                )
                is CommentPageModel -> CommentPageProjector(
                    scope = pageScope,
                    model = model,
                    dependencies = dependencies.comment(),
                )
            }
            page to projector
        }*/

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
                transitionSpec = getTransitionSpecForSlide(
                    orientation = SlideOrientation.Horizontal,
                ) {
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