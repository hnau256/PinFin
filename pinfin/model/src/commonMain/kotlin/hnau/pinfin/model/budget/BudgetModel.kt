@file:UseSerializers(
    MutableStateFlowSerializer::class
)

package hnau.pinfin.model.budget

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.gen.sealup.annotations.SealUp
import hnau.common.gen.sealup.annotations.Variant
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.TransactionsModel
import hnau.pinfin.model.budget.analytics.AnalyticsModel
import hnau.pinfin.model.budget.config.BudgetConfigModel
import hnau.pinfin.model.filter.FilterModel
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
) {

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
        val pages: MutableList<BudgetPageModelSkeleton> = mutableListOf(),
    )

    @SealUp(
        variants = [
            Variant(
                type = TransactionsModel::class,
                identifier = "transactions",
            ),
            Variant(
                type = AnalyticsModel::class,
                identifier = "analytics",
            ),
            Variant(
                type = BudgetConfigModel::class,
                identifier = "config",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "BudgetPageModel",
    )
    interface Page {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = TransactionsModel.Skeleton::class,
                identifier = "transactions",
            ),
            Variant(
                type = AnalyticsModel.Skeleton::class,
                identifier = "analytics",
            ),
            Variant(
                type = BudgetConfigModel.Skeleton::class,
                identifier = "config",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "BudgetPageModelSkeleton",
        serializable = true,
    )
    interface PageSkeleton {

        companion object
    }

    private val tabsCache: MutableList<BudgetPageModel> = mutableListOf()

    private val BudgetPageModelSkeleton.tab: BudgetTab
        get() = fold(
            ifTransactions = { BudgetTab.Transactions },
            ifAnalytics = { BudgetTab.Analytics },
            ifConfig = { BudgetTab.Config },
        )

    private fun getModel(
        tab: BudgetTab,
    ): BudgetPageModel {

        fun getSkeleton(): BudgetPageModelSkeleton = skeleton
            .pages
            .firstOrNull { it.tab == tab }
            .ifNull {
                when (tab) {
                    BudgetTab.Transactions -> PageSkeleton.transactions(FilterModel.Skeleton.create())
                    BudgetTab.Analytics -> PageSkeleton.analytics()
                    BudgetTab.Config -> PageSkeleton.config()
                }.also(skeleton.pages::add)
            }

        return tabsCache
            .firstOrNull { it.tab == tab }
            .ifNull {
                getSkeleton().fold(
                    ifTransactions = { transactionsSkeleton ->
                        Page.transactions(
                            scope = scope,
                            skeleton = transactionsSkeleton,
                            dependencies = dependencies.transactions(),
                        )
                    },

                    ifAnalytics = { analyticsSkeleton ->
                        Page.analytics(
                            scope = scope,
                            skeleton = analyticsSkeleton,
                            dependencies = dependencies.analytics(),
                        )
                    },

                    ifConfig = { configSkeleton ->
                        Page.config(
                            scope = scope,
                            skeleton = configSkeleton,
                            dependencies = dependencies.config(),
                        )
                    },
                ).also(tabsCache::add)
            }
    }

    fun selectTab(
        tab: BudgetTab,
    ) {
        skeleton.selectedTab.value = tab
    }

    val currentModelWithTab: StateFlow<Pair<BudgetTab, BudgetPageModel>> = skeleton
        .selectedTab
        .mapState(scope) { tab ->
            val model = getModel(tab)
            tab to model
        }

    val currentModel: StateFlow<BudgetPageModel> = currentModelWithTab
        .mapState(scope, Pair<*, BudgetPageModel>::second)

    val goBackHandler: GoBackHandler = currentModelWithTab
        .flatMapWithScope(scope) { scope, (tab, model) ->
            model.goBackHandler.mapState(scope) { modelGoBackOrNull ->
                modelGoBackOrNull.ifNull {
                    tab.takeIf { it != BudgetTab.default }?.let {
                        { skeleton.selectedTab.value = BudgetTab.default }
                    }
                }
            }
        }
}

val BudgetPageModel.tab: BudgetTab
    get() = fold(
        ifTransactions = { BudgetTab.Transactions },
        ifAnalytics = { BudgetTab.Analytics },
        ifConfig = { BudgetTab.Config },
    )