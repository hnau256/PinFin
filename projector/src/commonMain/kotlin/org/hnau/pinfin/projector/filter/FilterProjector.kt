package org.hnau.pinfin.projector.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.TopBarAction
import org.hnau.commons.app.projector.uikit.TopBarDefaults
import org.hnau.commons.app.projector.uikit.backbutton.LocalBackButtonWidth
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.state.StateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.uikit.transition.getTransitionSpecForSlideByCompare
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.pinfin.model.filter.FilterModel
import org.hnau.pinfin.model.filter.fold
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.Label
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter

class FilterProjector(
    scope: CoroutineScope,
    private val model: FilterModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val dateTimeFormatter: DateTimeFormatter

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

        @SealUp(
            variants = [
                Variant(
                    type = SelectCategoriesProjector.Page::class,
                    identifier = "categories",
                ),
                Variant(
                    type = SelectAccountsProjector.Page::class,
                    identifier = "accounts",
                ),
            ],
            wrappedValuePropertyName = "projector",
            sealedInterfaceName = "FilterConfigPage",
        )
        interface Page {

            @Composable
            fun Content()

            companion object
        }

        private val categories = SelectCategoriesProjector(
            model = model.categories,
            dependencies = dependencies.selectCategories(),
        )

        private val accounts = SelectAccountsProjector(
            model = model.accounts,
            dependencies = dependencies.selectAccounts(),
        )

        private val page: StateFlow<Pair<FilterModel.Tab, FilterConfigPage>> = model
            .type
            .mapState(scope) { (tab, type) ->
                val projector = type.fold(
                    ifCategories = { model ->
                        Page.categories(
                            SelectCategoriesProjector.Page(
                                model = model,
                                dependencies = dependencies.selectCategoriesPage(),
                            )
                        )
                    },
                    ifAccounts = { model ->
                        Page.accounts(
                            SelectAccountsProjector.Page(
                                model = model,
                                dependencies = dependencies.selectAccountsPage(),
                            )
                        )
                    },
                )
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
                        horizontal = Dimens.separation,
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
                            horizontal = Dimens.separation,
                        ),
                        contentKey = Pair<FilterModel.Tab, *>::first,
                        transitionSpec = getTransitionSpecForSlideByCompare(
                            orientation = Orientation.Horizontal,
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
    fun ContentAsTopBar(
        contentPadding: PaddingValues,
    ) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(top = TopBarDefaults.separationTop)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .height(TopBarDefaults.height)
                    .fillMaxWidth()
                    .padding(
                        start =  LocalBackButtonWidth.current + Dimens.smallSeparation,
                        end = Dimens.horizontalDisplayPadding,
                    ),
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
            model
                .period
                .collectAsState()
                .value
                .NullableStateContent(
                    modifier = Modifier.fillMaxWidth(),
                    transitionSpec = TransitionSpec.remember(
                        showAlignment = Alignment.CenterStart,
                    ),
                ) { period ->
                    Row(
                        modifier = Modifier.padding(
                            horizontal = Dimens.separation,
                            vertical = Dimens.extraSmallSeparation,
                        ),
                    ) {
                        Label(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
                            ) {
                                Text(
                                    dependencies.localization.filterPeriod(
                                        dependencies.dateTimeFormatter.formatDate(period.start),
                                        dependencies.dateTimeFormatter.formatDate(period.endInclusive),
                                    )
                                )
                                Icon(
                                    icon = Icons.Default.Close,
                                    modifier = Modifier
                                        .size(Dimens.smallSeparation * 2)
                                        .clickable(onClick = model::clearPeriod),
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
                    transitionSpec = TransitionSpec.remember(
                        showAlignment = Alignment.BottomCenter,
                    ),
                ) { config ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = Dimens.separation,
                                end = Dimens.separation,
                                top = Dimens.separation,
                            ),
                    ) {
                        config.Content()
                    }
                }
        }
    }
}