package hnau.pinfin.projector

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import hnau.common.app.goback.GlobalGoBackHandler
import hnau.common.app.goback.GoBackHandler
import hnau.common.compose.uikit.ErrorPanel
import hnau.common.compose.uikit.progressindicator.ProgressIndicatorInBox
import hnau.common.compose.uikit.shape.HnauShape
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.NavigationIcon
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.pinfin.model.IconModel
import hnau.pinfin.model.utils.icons.IconVariant
import hnau.pinfin.model.utils.icons.title
import hnau.pinfin.projector.utils.image
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class IconProjector(
    scope: CoroutineScope,
    private val model: IconModel,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        val globalGoBackHandler: GlobalGoBackHandler
    }

    private val globalGoBackHandler: GoBackHandler = dependencies.globalGoBackHandler.resolve(scope)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.select_icon)) },
                    navigationIcon = { globalGoBackHandler.NavigationIcon() },
                )
            },
        ) { contentPadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
            ) {
                Icons(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Categories(
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Search(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }

    @Composable
    private fun Icons(
        modifier: Modifier = Modifier,
    ) {
        model
            .icons
            .collectAsState()
            .value
            .StateContent(
                modifier = modifier,
                label = "LoadingOrEmptyOrIcons",
                transitionSpec = TransitionSpec.crossfade(),
                contentKey = { state ->
                    when (state) {
                        Loading -> 0
                        is Ready -> state
                            .value
                            .foldNullable(
                                ifNull = { 1 },
                                ifNotNull = { 2 },
                            )
                    }
                },
            ) { state ->
                when (state) {
                    Loading -> ProgressIndicatorInBox()
                    is Ready -> state
                        .value
                        .foldNullable(
                            ifNull = {
                                ErrorPanel(
                                    title = { Text(stringResource(Res.string.no_icons_found)) }
                                )
                            },
                            ifNotNull = { icons ->
                                Icons(
                                    icons = icons,
                                )
                            }
                        )
                }
            }
    }

    @Composable
    private fun Icons(
        icons: NonEmptyList<Pair<IconVariant, Boolean>>,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(
                minSize = 72.dp,
            ),
            reverseLayout = true,
            contentPadding = PaddingValues(
                horizontal = Dimens.separation,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
            horizontalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            items(icons) { (icon, selected) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
                    modifier = Modifier
                        .then(
                            selected.foldBoolean(
                                ifFalse = { Modifier },
                                ifTrue = {
                                    val shape = HnauShape()
                                    Modifier
                                        .background(
                                            shape = shape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                        )
                                        .border(
                                            width = Dimens.border,
                                            shape = shape,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                },
                            ),
                        )
                        .padding(
                            vertical = Dimens.smallSeparation
                        ),
                ) {
                    val tint = selected.foldBoolean(
                        ifFalse = { LocalContentColor.current },
                        ifTrue = { MaterialTheme.colorScheme.primary },
                    )
                    Icon(
                        icon = icon.image,
                        tint = tint,
                    )
                    Text(
                        textAlign = TextAlign.Center,
                        text = icon.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        maxLines = 1,
                    )
                }
            }
        }
    }

    @Composable
    private fun Categories(
        modifier: Modifier = Modifier,
    ) {

    }

    @Composable
    private fun Search(
        modifier: Modifier = Modifier,
    ) {

    }
}