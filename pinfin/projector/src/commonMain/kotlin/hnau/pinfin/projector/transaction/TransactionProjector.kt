package hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import hnau.pinfin.model.transaction.TransactionModel
import hnau.pinfin.model.utils.TemplateModel
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.new_transaction
import hnau.pinfin.projector.resources.transaction
import hnau.pinfin.projector.transaction.page.DatePageProjector
import hnau.pinfin.projector.transaction.part.DateProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class TransactionProjector(
    scope: CoroutineScope,
    private val model: TransactionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun info(): InfoProjector.Dependencies

        fun page(): CurrentPageProjector.Dependencies

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies
        .globalGoBackHandler
        .resolve(scope)

    private val info = InfoProjector(
        scope = scope,
        dependencies = dependencies.info(),
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
                    title = {
                        Text(
                            stringResource(
                                when (model.isNewTransaction) {
                                    true -> Res.string.new_transaction
                                    false -> Res.string.transaction
                                }
                            )
                        )
                    },
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