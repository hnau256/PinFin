@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.analytics

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.budget.analytics.tab.AccountsModel
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import hnau.pinfin.model.budget.analytics.tab.CategoriesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AnalyticsModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun accounts(): AccountsModel.Dependencies

        fun categories(): CategoriesModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val selectedTab: MutableStateFlow<AnalyticsTab> =
            AnalyticsTab.default.toMutableStateFlowAsInitial(),
        val accounts: AccountsModel.Skeleton = AccountsModel.Skeleton(),
    )

    val accounts: AccountsModel = AccountsModel(
        scope = scope,
        dependencies = dependencies.accounts(),
        skeleton = skeleton.accounts
    )

    val categories: CategoriesModel = CategoriesModel(
        scope = scope,
        dependencies = dependencies.categories(),
    )

    val selectedTab: MutableStateFlow<AnalyticsTab>
        get() = skeleton.selectedTab

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}