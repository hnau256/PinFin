@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.filter

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.stickNotNull
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.filter.pageable.SelectCategoriesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
        .mapState(scope) { selectedTabOrNull ->
            selectedTabOrNull?.let { selectedTab ->
                Page(
                    type = selectedTab.mapState(scope) { tab ->
                        when (tab) {
                            Tab.SelectedCategories -> Page.Type.Categories(
                                categories.createPage(),
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