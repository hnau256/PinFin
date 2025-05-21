package hnau.pinfin.projector

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import hnau.common.model.goback.GlobalGoBackHandler
import hnau.common.model.goback.GoBackHandler
import hnau.common.compose.uikit.ContainerStyle
import hnau.common.compose.uikit.ErrorPanel
import hnau.common.compose.uikit.HnauButton
import hnau.common.compose.uikit.TextInput
import hnau.common.compose.uikit.TripleRow
import hnau.common.compose.uikit.progressindicator.ProgressIndicatorInBox
import hnau.common.compose.uikit.shape.HnauShape
import hnau.common.compose.uikit.shape.create
import hnau.common.compose.uikit.shape.inRow
import hnau.common.compose.uikit.state.StateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.NavigationIcon
import hnau.common.compose.utils.horizontalDisplayPadding
import hnau.common.compose.utils.map
import hnau.common.compose.utils.plus
import hnau.common.compose.utils.verticalDisplayPadding
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifTrue
import hnau.pinfin.model.IconModel
import hnau.pinfin.model.utils.icons.IconCategory
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
                modifier = Modifier.fillMaxSize(),
            ) {
                Search(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = contentPadding.map(
                        bottom = { 0.dp },
                        top = { it + Dimens.separation },
                    ),
                )
                Categories(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = contentPadding.map(
                        top = { Dimens.smallSeparation },
                        bottom = { 0.dp },
                    ),
                )
                Icons(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = contentPadding.map(
                        top = { Dimens.separation },
                    ),
                )
            }
        }
    }

    @Composable
    private fun Icons(
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues,
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
                                    contentPadding = contentPadding,
                                    icons = icons,
                                )
                            }
                        )
                }
            }
    }

    @Composable
    private fun Icons(
        contentPadding: PaddingValues,
        icons: NonEmptyList<Pair<IconVariant, Boolean>>,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(
                minSize = 72.dp,
            ),
            contentPadding = contentPadding + PaddingValues(
                horizontal = Dimens.smallSeparation,
            ),
            verticalArrangement = Arrangement.spacedBy(
                space = Dimens.smallSeparation,
                alignment = Alignment.Top,
            ),
            horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            items(icons) { (variant, selected) ->
                val shape = HnauShape()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
                    modifier = Modifier
                        .then(
                            selected.foldBoolean(
                                ifFalse = { Modifier },
                                ifTrue = {
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
                        .clip(shape)
                        .clickable { model.onSelect(variant) }
                        .padding(Dimens.smallSeparation),
                ) {
                    val tint = selected.foldBoolean(
                        ifFalse = { LocalContentColor.current },
                        ifTrue = { MaterialTheme.colorScheme.primary },
                    )
                    Icon(
                        icon = variant.image,
                        tint = tint,
                    )
                    Text(
                        textAlign = TextAlign.Center,
                        text = variant.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                    )
                }
            }
        }
    }

    @Composable
    private fun Categories(
        contentPadding: PaddingValues,
        modifier: Modifier = Modifier,
    ) {
        val selectedCategory by model.selectedCategory.collectAsState()
        val categories = IconCategory.entries
        val categoriesCount = categories.size
        LazyRow(
            modifier = modifier,
            contentPadding = contentPadding +
                    PaddingValues(horizontal = Dimens.horizontalDisplayPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.chipsSeparation),
        ) {
            items(
                count = categoriesCount,
            ) { i ->
                val category = categories[i]
                val selected = category == selectedCategory
                HnauButton(
                    onClick = { model.onCategoryClick(category) },
                    style = selected.foldBoolean(
                        ifTrue = { ContainerStyle.Primary },
                        ifFalse = { ContainerStyle.Neutral }
                    ),
                    shape = HnauShape.inRow.create(
                        index = i,
                        totalCount = categoriesCount,
                    ),
                ) {
                    TripleRow(
                        leading = selected.ifTrue {
                            { Icon(Icons.Default.Done) }
                        },
                        content = { Text(category.name) }
                    )
                }
            }
        }
    }

    @Composable
    private fun Search(
        contentPadding: PaddingValues,
        modifier: Modifier = Modifier,
    ) {
        val focusRequester = remember { FocusRequester() }
        TextInput(
            value = model.query,
            placeholder = { Text(stringResource(Res.string.search)) },
            modifier = modifier
                .padding(contentPadding)
                .padding(
                    start = Dimens.horizontalDisplayPadding,
                    end = Dimens.horizontalDisplayPadding,
                    bottom = Dimens.verticalDisplayPadding,
                )
                .focusRequester(focusRequester),
            leadingIcon = { Icon(Icons.Default.Search) },
        )
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}