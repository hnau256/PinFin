package org.hnau.pinfin.projector.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
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
import org.hnau.commons.app.projector.uikit.TopBarAction
import org.hnau.commons.app.projector.uikit.TopBarDefaults
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.state.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.SlideOrientation
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.filter.FilterModel
import org.hnau.pinfin.projector.transaction.utils.createPagesTransitionSpec
import org.hnau.pinfin.projector.utils.BackButtonWidth
import org.hnau.commons.gen.pipe.annotations.Pipe
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

        fun selectCategories(): SelectCategoriesProjector.Dependencies

        fun selectAccounts(): SelectAccountsProjector.Dependencies

        fun selectCategoriesPage(): SelectCategoriesProjector.Page.Dependencies

        fun selectAccountsPage(): SelectAccountsProjector.Page.Dependencies
    }

    class Config(
        scope: CoroutineScope,
        model: FilterModel.Config,
        dependencies: Dependencies,
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

            data class Accounts(
                val projector: SelectAccountsProjector.Page,
            ) : Page {

                override val tab: FilterModel.Tab
                    get() = FilterModel.Tab.SelectedAccounts

                @Composable
                override fun Content() {
                    projector.Content()
                }
            }
        }

        private val categories = SelectCategoriesProjector(
            model = model.categories,
            dependencies = dependencies.selectCategories(),
        )

        private val accounts = SelectAccountsProjector(
            model = model.accounts,
            dependencies = dependencies.selectAccounts(),
        )

        private val page: StateFlow<Pair<FilterModel.Tab, Page>> = model
            .type
            .mapState(scope) { (tab, type) ->
                val projector = when (type) {
                    is FilterModel.Config.Type.Categories -> Page.Categories(
                        SelectCategoriesProjector.Page(
                            model = type.model,
                            dependencies = dependencies.selectCategoriesPage(),
                        )
                    )

                    is FilterModel.Config.Type.Accounts -> Page.Accounts(
                        SelectAccountsProjector.Page(
                            model = type.model,
                            dependencies = dependencies.selectAccountsPage(),
                        )
                    )
                }
                tab to projector
            }

        @Composable
        fun Content() {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.smallSeparation),
                verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
            ) {
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                    contentPadding = PaddingValues(
                        horizontal = Dimens.smallSeparation,
                    ),
                ) {
                    item(
                        key = "categories",
                    ) {
                        categories.Content()
                    }
                    item(
                        key = "accounts",
                    ) {
                        accounts.Content()
                    }
                }
                page
                    .collectAsState()
                    .value
                    .StateContent(
                        modifier = Modifier.fillMaxWidth().padding(
                            horizontal = Dimens.smallSeparation,
                        ),
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
                    dependencies = dependencies,
                )
            }
        }

    @Composable
    fun ContentAsTopBar() {
        Column(
            modifier = Modifier
                .systemBarsPadding()
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
                                start = Dimens.separation,
                                end = Dimens.separation,
                                top = Dimens.smallSeparation,
                            ),
                    ) {
                        config.Content()
                    }
                }
        }
    }
}