@file:UseSerializers(
    MutableStateFlowSerializer::class
)

package hnau.pinfin.model.budget

import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.budget.config.BudgetConfigModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        fun transactions(): TransactionsModel.Dependencies

        fun analytics(): AnalyticsModel.Dependencies

        fun config(): BudgetConfigModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val selectedTab: MutableStateFlow<BudgetTab> =
            BudgetTab.default.toMutableStateFlowAsInitial(),
        val pages: MutableList<BudgetPageModel.Skeleton> = mutableListOf(),
    )

    private val tabsCache: MutableList<BudgetPageModel> = mutableListOf()

    private fun getModel(
        tab: BudgetTab,
    ): BudgetPageModel {

        fun getSkeleton(): BudgetPageModel.Skeleton = skeleton
            .pages
            .firstOrNull { it.tab == tab }
            .ifNull {
                when (tab) {
                    BudgetTab.Transactions -> BudgetPageModel.Skeleton.Transactions()
                    BudgetTab.Analytics -> BudgetPageModel.Skeleton.Analytics()
                    BudgetTab.Config -> BudgetPageModel.Skeleton.Config()
                }.also(skeleton.pages::add)
            }

        return tabsCache
            .firstOrNull { it.tab == tab }
            .ifNull {
                val skeleton = getSkeleton()
                when (skeleton) {
                    is BudgetPageModel.Skeleton.Transactions -> BudgetPageModel.Transactions(
                        TransactionsModel(
                            scope = scope,
                            skeleton = skeleton.skeleton,
                            dependencies = dependencies.transactions(),
                        )
                    )

                    is BudgetPageModel.Skeleton.Analytics -> BudgetPageModel.Analytics(
                        AnalyticsModel(
                            scope = scope,
                            skeleton = skeleton.skeleton,
                            dependencies = dependencies.analytics(),
                        )
                    )

                    is BudgetPageModel.Skeleton.Config -> BudgetPageModel.Config(
                        BudgetConfigModel(
                            scope = scope,
                            skeleton = skeleton.skeleton,
                            dependencies = dependencies.config(),
                        )
                    )
                }.also(tabsCache::add)
            }
    }

    fun selectTab(
        tab: BudgetTab,
    ) {
        skeleton.selectedTab.value = tab
    }

    val currentModel: StateFlow<BudgetPageModel> = skeleton
        .selectedTab
        .mapState(scope, ::getModel)

    override val goBackHandler: GoBackHandler = skeleton
        .selectedTab
        .mapState(scope) { selectedTab ->
            when (selectedTab) {
                BudgetTab.default -> null
                else -> {
                    { skeleton.selectedTab.value = BudgetTab.default }
                }
            }
        }
}