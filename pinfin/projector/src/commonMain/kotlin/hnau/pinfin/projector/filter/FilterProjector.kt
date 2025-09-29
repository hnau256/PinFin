package hnau.pinfin.projector.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.uikit.TopBarAction
import hnau.common.app.projector.uikit.TopBarDefaults
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.pinfin.model.filter.FilterModel
import hnau.pinfin.projector.transaction.utils.createPagesTransitionSpec
import hnau.pinfin.projector.utils.BackButtonWidth
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class FilterProjector(
    scope: CoroutineScope,
    private val model: FilterModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth
    }

    class Config(
        scope: CoroutineScope,
        model: FilterModel.Config,
    ) {

        sealed interface Page {

            val tab: FilterModel.Tab

            @Composable
            fun Content()

            data class Categories(
                val projector: SelectCategoriesProjector.Page,
            ) : Page {

                override val tab: FilterModel.Tab
                    get() = FilterModel.Tab.SelectedCategories

                @Composable
                override fun Content() {
                    projector.Content()
                }
            }
        }

        private val categories = SelectCategoriesProjector(
            model = model.categories,
        )

        private val page: StateFlow<Pair<FilterModel.Tab, Page>> = model
            .type
            .mapState(scope) { (tab, type) ->
                val projector = when (type) {
                    is FilterModel.Config.Type.Categories -> Page.Categories(
                        SelectCategoriesProjector.Page(
                            model = type.model,
                        )
                    )
                }
                tab to projector
            }

        @Composable
        fun Content() {
            Column(
                modifier = Modifier.fillMaxWidth().padding(Dimens.smallSeparation),
                verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            ) {
                categories.Content()
                page
                    .collectAsState()
                    .value
                    .StateContent(
                        modifier = Modifier.fillMaxWidth(),
                        contentKey = Pair<FilterModel.Tab, *>::first,
                        transitionSpec = createPagesTransitionSpec(
                            orientation = SlideOrientation.Horizontal,
                        ) { it.first.ordinal },
                        label = "FiltersPage",
                    ) { (_, type) ->
                        type.Content()
                    }
            }
        }
    }

    private val config: StateFlow<Config?> = model
        .config
        .mapWithScope(scope) { scope, configOrNull ->
            configOrNull?.let { config ->
                Config(
                    scope = scope,
                    model = config,
                )
            }
        }

    @Composable
    fun ContentAsTopBar() {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .height(TopBarDefaults.height)
                    .fillMaxWidth()
                    .padding(
                        start = dependencies.backButtonWidth.width,
                    )
                    .padding(horizontal = Dimens.smallSeparation),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = Dimens.smallSeparation,
                    alignment = Alignment.Start,
                ),
            ) {
                Spacer(Modifier.weight(1f))
                TopBarAction(
                    onClick = model::switchConfigVisibility,
                ) {
                    Box {
                        Icon(Icons.Default.FilterAlt)
                        val hasFilters = model.filters.collectAsState().value.any
                        if (hasFilters) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                    )
                            )
                        }
                    }
                }
            }
            config
                .collectAsState()
                .value
                .NullableStateContent(
                    modifier = Modifier.fillMaxWidth(),
                    transitionSpec = TransitionSpec.vertical(),
                ) { config ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = Dimens.smallSeparation,
                                end = Dimens.smallSeparation,
                                top = Dimens.smallSeparation,
                            ),
                    ) {
                        config.Content()
                    }
                }
        }
    }
}