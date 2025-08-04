package hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.utils.getTransitionSpecForHorizontalSlide
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.transaction.Part
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.model.transaction.part.page.DatePageModel
import hnau.pinfin.model.transaction.part.page.TimePageModel
import hnau.pinfin.projector.transaction.page.DatePageProjector
import hnau.pinfin.projector.transaction.page.PageProjector
import hnau.pinfin.projector.transaction.page.TimePageProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.format.Padding
import kotlin.math.sign

class CurrentPageProjector(
    scope: CoroutineScope,
    model: TransactionModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun date(): DatePageProjector.Dependencies

        fun time(): TimePageProjector.Dependencies
    }

    private val page: StateFlow<Pair<Part, PageProjector>> = model
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