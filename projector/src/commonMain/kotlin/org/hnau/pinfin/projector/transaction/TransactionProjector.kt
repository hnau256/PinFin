package org.hnau.pinfin.projector.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hnau.commons.app.projector.uikit.FullScreen
import org.hnau.commons.app.projector.uikit.TopBar
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.copy
import org.hnau.pinfin.model.transaction.TransactionModel
import org.hnau.pinfin.projector.transaction.delegates.DialogsProjector
import org.hnau.pinfin.projector.transaction.delegates.InfoProjector
import org.hnau.pinfin.projector.transaction.delegates.PageProjector
import org.hnau.pinfin.projector.transaction.delegates.TopBarActionsProjector
import org.hnau.pinfin.projector.transaction.delegates.TypeProjector
import org.hnau.pinfin.projector.utils.BackButtonWidth
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class TransactionProjector(
    scope: CoroutineScope,
    model: TransactionModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun type(): TypeProjector.Dependencies

        fun info(
            type: TypeProjector,
        ): InfoProjector.Dependencies

        fun page(): PageProjector.Dependencies

        val backButtonWidth: BackButtonWidth
    }

    private val type = TypeProjector(
        scope = scope,
        model = model,
        dependencies = dependencies.type(),
    )

    private val info = InfoProjector(
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

    private val dialogs = DialogsProjector(
        model = model,
    )

    private val topBarActions = TopBarActionsProjector(
        model = model,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        FullScreen(
            backButtonWidth = dependencies.backButtonWidth.width,
            top = { contentPadding ->
                TopBar(
                    modifier = Modifier.padding(contentPadding),
                ) {
                    type.HeaderContent()
                    Spacer(Modifier.weight(1f))
                    topBarActions.Content()
                }
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
            dialogs.Content()
        }
    }
}