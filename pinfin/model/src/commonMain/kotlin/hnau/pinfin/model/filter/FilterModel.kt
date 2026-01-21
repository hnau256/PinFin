@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.filter

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flow.state.combineState
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.coroutines.flow.state.stickNotNull
import hnau.common.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.LocalDateRangeSerializer
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.filter.pageable.SelectAccountsModel
import hnau.pinfin.model.filter.pageable.SelectCategoriesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class FilterModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun categories(): SelectCategoriesModel.Dependencies

        fun accounts(): SelectAccountsModel.Dependencies
    }

    enum class Tab {
        SelectedCategories,
        SelectedAccounts;

        companion object {

            val default: Tab
                get() = SelectedCategories
        }
    }

    @Serializable
    data class Skeleton(
        val categories: SelectCategoriesModel.Skeleton,
        val accounts: SelectAccountsModel.Skeleton,
        val period: @Serializable(LocalDateRangeSerializer::class) LocalDateRange?,
        val selectedTab: MutableStateFlow<Tab?> =
            null.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun create(
                initialFilters: Filters = Filters.empty,
            ): Skeleton = Skeleton(
                categories = SelectCategoriesModel.Skeleton.create(
                    initialSelectedCategoriesIds = initialFilters.categories,
                ),
                accounts = SelectAccountsModel.Skeleton.create(
                    initialSelectedAccountsIds = initialFilters.accounts,
                ),
                period = initialFilters.period,
            )
        }
    }

    class Config(
        val type: StateFlow<Pair<Tab, Type>>,
        val categories: SelectCategoriesModel,
        val accounts: SelectAccountsModel,
    ) {

        sealed interface Type {

            data class Categories(
                val model: SelectCategoriesModel.Page,
            ) : Type

            data class Accounts(
                val model: SelectAccountsModel.Page,
            ) : Type
        }
    }

    private fun createIsFocused(
        tab: Tab,
    ): StateFlow<Boolean> = skeleton
        .selectedTab
        .mapState(scope) { it == tab }

    private fun createRequestFocus(
        tab: Tab,
    ): () -> Unit = {
        skeleton.selectedTab.value = tab
    }

    val categories = SelectCategoriesModel(
        scope = scope,
        dependencies = dependencies.categories(),
        skeleton = skeleton.categories,
        isFocused = createIsFocused(Tab.SelectedCategories),
        requestFocus = createRequestFocus(Tab.SelectedCategories),
    )

    val accounts = SelectAccountsModel(
        scope = scope,
        dependencies = dependencies.accounts(),
        skeleton = skeleton.accounts,
        isFocused = createIsFocused(Tab.SelectedAccounts),
        requestFocus = createRequestFocus(Tab.SelectedAccounts),
    )

    val filters: StateFlow<Filters> = combineState(
        scope = scope,
        first = categories.selectedCategoriesIds,
        second = accounts.selectedAccountsIds,
    ) { categories, accounts ->
        Filters(
            accounts = accounts,
            categories = categories,
            period = skeleton.period, //TODO
        )
    }

    val config: StateFlow<Config?> = skeleton
        .selectedTab
        .stickNotNull(scope)
        .mapState(scope) { selectedTabOrNull ->
            selectedTabOrNull?.let { selectedTab ->
                Config(
                    type = selectedTab.mapState(scope) { tab ->
                        val model = when (tab) {
                            Tab.SelectedCategories -> Config.Type.Categories(
                                categories.createPage(),
                            )

                            Tab.SelectedAccounts -> Config.Type.Accounts(
                                accounts.createPage(),
                            )
                        }
                        tab to model
                    },
                    categories = categories,
                    accounts = accounts,
                )
            }
        }

    fun switchConfigVisibility() {
        skeleton.selectedTab.update { selected ->
            selected.foldNullable(
                ifNull = { Tab.default },
                ifNotNull = { null }
            )
        }
    }

    val goBackHandler: GoBackHandler = skeleton
        .selectedTab
        .mapState(scope) { selectedTabOeNull ->
            selectedTabOeNull?.let {
                { skeleton.selectedTab.value = null }
            }
        }
}