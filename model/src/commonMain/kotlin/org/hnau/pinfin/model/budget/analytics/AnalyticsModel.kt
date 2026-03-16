@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.budget.analytics.tab.AccountsModel
import org.hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import org.hnau.pinfin.model.budget.analytics.tab.graph.GraphModel

class AnalyticsModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun accounts(): AccountsModel.Dependencies

        fun graph(): GraphModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val selectedTab: MutableStateFlow<AnalyticsTab> =
            AnalyticsTab.default.toMutableStateFlowAsInitial(),
        val accounts: AccountsModel.Skeleton = AccountsModel.Skeleton(),
        val graph: GraphModel.Skeleton = GraphModel.Skeleton(),
    )

    val accounts: AccountsModel = AccountsModel(
        scope = scope,
        dependencies = dependencies.accounts(),
        skeleton = skeleton.accounts
    )

    val graph: GraphModel = GraphModel(
        scope = scope,
        dependencies = dependencies.graph(),
        skeleton = skeleton.graph,
    )

    val selectedTab: MutableStateFlow<AnalyticsTab>
        get() = skeleton.selectedTab

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}