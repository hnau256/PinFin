package hnau.pinfin.projector.transaction_old_2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import hnau.common.app.model.goback.GlobalGoBackHandler
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.projector.utils.NavigationIcon
import hnau.common.app.projector.utils.Overcompose
import hnau.common.app.projector.utils.combineWith
import hnau.common.app.projector.utils.copy
import hnau.pinfin.model.transaction_old_2.TransactionModel
import hnau.pinfin.projector.transaction_old_2.part.TypeProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class TransactionProjector(
    scope: CoroutineScope,
    private val model: TransactionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun type(): TypeProjector.Dependencies

            fun info(
                typeProjector: TypeProjector,
            ): InfoProjector.Dependencies

            fun page(): CurrentPageProjector.Dependencies

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies
        .globalGoBackHandler
        .resolve(scope)

    private val type = TypeProjector(
        scope = scope,
        dependencies = dependencies.type(),
        model = model.type,
    )

    private val info = InfoProjector(
        scope = scope,
        dependencies = dependencies.info(
            typeProjector = type,
        ),
        model = model,
    )

    private val page = CurrentPageProjector(
        scope = scope,
        dependencies = dependencies.page(),
        model = model,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { type.HeaderContent() },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                )
            },
        ) { contentPadding ->
            Overcompose(
                modifier = Modifier.fillMaxSize(),
                top = {
                    info.Content(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = contentPadding.copy(bottom = 0.dp),
                    )
                },
            ) { pagePadding ->
                page.Content(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = contentPadding.combineWith(
                        other = pagePadding,
                    ) { content, page ->
                        max(content, page)
                    },
                )
            }
        }
    }
}