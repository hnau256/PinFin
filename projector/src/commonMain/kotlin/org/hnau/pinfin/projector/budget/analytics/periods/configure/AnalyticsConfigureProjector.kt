package org.hnau.pinfin.projector.budget.analytics.periods.configure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.projector.uikit.state.NullableStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.AnalyticsConfigureModel
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.fold
import org.hnau.pinfin.projector.budget.analytics.periods.configure.period.PeriodConfigProjector
import org.hnau.pinfin.projector.filter.SelectAccountsProjector
import org.hnau.pinfin.projector.filter.SelectCategoriesProjector

/**
 * Экран настроек аналитики (docs/analytics-v2-plan.md, "Фаза 3", "Фаза 4"): период + якорь,
 * группировка, операция, ограничение по счетам/категориям (переиспользует
 * [SelectAccountsProjector]/[SelectCategoriesProjector] - те же чипы и `.Page`, что и у
 * `FilterProjector`). FAB "Сохранить" активен только когда конфиг корректен
 * и отличается от исходного (см. [AnalyticsConfigureModel.save]).
 */
class AnalyticsConfigureProjector(
    scope: CoroutineScope,
    private val model: AnalyticsConfigureModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun period(): PeriodConfigProjector.Dependencies

        fun groupBy(): GroupByConfigProjector.Dependencies

        fun operation(): OperationConfigProjector.Dependencies

        fun selectAccounts(): SelectAccountsProjector.Dependencies

        fun selectCategories(): SelectCategoriesProjector.Dependencies

        fun selectAccountsPage(): SelectAccountsProjector.Page.Dependencies

        fun selectCategoriesPage(): SelectCategoriesProjector.Page.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = SelectAccountsProjector.Page::class,
                identifier = "accounts",
            ),
            Variant(
                type = SelectCategoriesProjector.Page::class,
                identifier = "categories",
            ),
        ],
        wrappedValuePropertyName = "projector",
        sealedInterfaceName = "SelectConfigPage",
    )
    interface SelectPage {

        @Composable
        fun Content()

        companion object
    }

    private val period = PeriodConfigProjector(
        model = model.period,
        dependencies = dependencies.period(),
    )

    private val groupBy = GroupByConfigProjector(
        model = model.groupBy,
        dependencies = dependencies.groupBy(),
    )

    private val operation = OperationConfigProjector(
        scope = scope,
        model = model.operation,
        dependencies = dependencies.operation(),
    )

    private val selectAccounts = SelectAccountsProjector(
        model = model.accounts,
        dependencies = dependencies.selectAccounts(),
    )

    private val selectCategories = SelectCategoriesProjector(
        model = model.categories,
        dependencies = dependencies.selectCategories(),
    )

    private val selectPage: StateFlow<SelectConfigPage?> = model
        .selectedPage
        .mapState(scope) { pageOrNull ->
            pageOrNull?.fold(
                ifAccounts = { pageModel ->
                    SelectPage.accounts(
                        SelectAccountsProjector.Page(
                            model = pageModel,
                            dependencies = dependencies.selectAccountsPage(),
                        )
                    )
                },
                ifCategories = { pageModel ->
                    SelectPage.categories(
                        SelectCategoriesProjector.Page(
                            model = pageModel,
                            dependencies = dependencies.selectCategoriesPage(),
                        )
                    )
                },
            )
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(Dimens.separation),
            ) {
                item(key = "Period") {
                    period.Content(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.horizontalDisplayPadding),
                    )
                }
                item(key = "GroupBy") {
                    groupBy.Content(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.horizontalDisplayPadding),
                    )
                }
                item(key = "Operation") {
                    operation.Content(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.horizontalDisplayPadding),
                    )
                }
                item(key = "SelectAccountsCategories") {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.horizontalDisplayPadding),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                        verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
                    ) {
                        selectAccounts.Content()
                        selectCategories.Content()
                    }
                }
                item(key = "SelectAccountsCategoriesPage") {
                    selectPage
                        .collectAsState()
                        .value
                        .NullableStateContent(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.horizontalDisplayPadding),
                            transitionSpec = TransitionSpec.remember(
                                showAlignment = Alignment.TopCenter,
                            ),
                        ) { page ->
                            page.Content()
                        }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(Dimens.largeSeparation),
                contentAlignment = Alignment.BottomEnd,
            ) {
                val save = model.save.collectAsState().value
                FloatingActionButton(
                    onClick = { save?.invoke() },
                ) {
                    Icon(Icons.Filled.Save)
                }
            }
        }
    }
}
