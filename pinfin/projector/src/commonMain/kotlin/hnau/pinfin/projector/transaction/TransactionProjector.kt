package hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.NavigationIcon
import hnau.common.app.projector.utils.Overcompose
import hnau.common.app.projector.utils.combineWith
import hnau.common.app.projector.utils.copy
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.projector.transaction.delegates.InfoProjector
import hnau.pinfin.projector.transaction.delegates.PageProjector
import hnau.pinfin.projector.transaction.delegates.TypeProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class TransactionProjector(
    scope: CoroutineScope,
    model: TransactionModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun type(): TypeProjector.Dependencies

        fun info(
            type: TypeProjector,
        ): InfoProjector.Dependencies

        fun page(): PageProjector.Dependencies

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies
        .globalGoBackHandler
        .resolve(scope)

    private val type = TypeProjector(
        scope = scope,
        model = model,
        dependencies = dependencies.type(),
    )

    private val info = InfoProjector(
        scope = scope,
        model = model,
        dependencies = dependencies.info(
            type = type,
        ),
    )

    private val page = PageProjector(
        scope = scope,
        model = model,
        dependencies = dependencies.page(),
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
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
            ) {
                info.Content(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = contentPadding.copy(bottom = 0.dp),
                )
                page.Content(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding.copy(top = 0.dp),
                )
            }
        }
    }
}