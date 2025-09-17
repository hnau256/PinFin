package hnau.pinfin.model.filter

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.stickNotNull
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.CategoryId
import hnau.pinfin.model.filter.pageable.SelectCategoriesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

class FilterModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun categories(): SelectCategoriesModel.Dependencies

        fun page(): Page.Dependencies
    }

    data class Filters(
        val selectedCategories: List<CategoryId>,
    ) {

        companion object {

            val empty = Filters(
                selectedCategories = emptyList(),
            )
        }
    }

    enum class Tab {
        SelectedCategories;

        companion object {

            val default: Tab
                get() = SelectedCategories
        }
    }

    @Serializable
    data class Skeleton(
        val categories: SelectCategoriesModel.Skeleton,
        val selectedTab: MutableStateFlow<Tab?> =
            null.toMutableStateFlowAsInitial(),
        var page: Page.Skeleton? = null,
    ) {

        companion object {

            fun create(
                initialFilters: Filters = Filters.empty,
            ): Skeleton = Skeleton(
                categories = SelectCategoriesModel.Skeleton.create(
                    initialSelectedCategoriesIds = initialFilters.selectedCategories,
                )
            )
        }
    }

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        skeleton: Skeleton,
        val type: StateFlow<Type>,
    ) {

        sealed interface Type {

            val tab: Tab

            data class Categories(
                val model: SelectCategoriesModel.Page,
            ) : Type {

                override val tab: Tab
                    get() = Tab.SelectedCategories
            }
        }

        @Pipe
        interface Dependencies

        @Serializable
        /*data*/ class Skeleton
    }

    val categories = SelectCategoriesModel(
        scope = scope,
        dependencies = dependencies.categories(),
        skeleton = skeleton.categories,
    )

    val filters: StateFlow<Filters> = categories
        .selectedCategoriesIds
        .mapState(scope) { categories ->
            Filters(
                selectedCategories = categories,
            )
        }

    val page: StateFlow<Page?> = skeleton
        .selectedTab
        .stickNotNull(scope)
        .mapWithScope(scope) { scope, selectedTabOrNull ->
            selectedTabOrNull?.let { selectedTab ->
                Page(
                    scope = scope,
                    dependencies = dependencies.page(),
                    skeleton = skeleton::page
                        .toAccessor()
                        .getOrInit { Page.Skeleton() },
                    type = selectedTab.mapWithScope(scope) { scope, tab ->
                        when (tab) {
                            Tab.SelectedCategories -> Page.Type.Categories(
                                categories.createPage(scope),
                            )
                        }
                    }
                )
            }
        }

    fun switchPageVisibility() {
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