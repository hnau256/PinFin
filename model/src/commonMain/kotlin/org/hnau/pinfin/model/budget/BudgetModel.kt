@file:UseSerializers(
    MutableStateFlowSerializer::class
)

package org.hnau.pinfin.model.budget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifNull
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.TransactionsModel
import org.hnau.pinfin.model.budget.analytics.AnalyticsModel
import org.hnau.pinfin.model.budget.manage.BudgetManageModel
import org.hnau.pinfin.model.filter.FilterModel

class BudgetModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun transactions(): TransactionsModel.Dependencies

        fun analytics(): AnalyticsModel.Dependencies

        fun manage(): BudgetManageModel.Dependencies
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
                type = BudgetManageModel::class,
                identifier = "manage",
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
                type = BudgetManageModel.Skeleton::class,
                identifier = "manage",
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
            ifManage = { BudgetTab.Manage },
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
                    BudgetTab.Manage -> PageSkeleton.manage()
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

                    ifManage = { configSkeleton ->
                        Page.manage(
                            scope = scope,
                            skeleton = configSkeleton,
                            dependencies = dependencies.manage(),
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
        ifManage = { BudgetTab.Manage },
    )