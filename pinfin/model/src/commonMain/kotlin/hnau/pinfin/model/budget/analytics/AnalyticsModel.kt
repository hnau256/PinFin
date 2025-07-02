@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.analytics

import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.goback.NeverGoBackHandler
import hnau.pinfin.model.budget.analytics.tab.AccountsModel
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTabValues
import hnau.pinfin.model.budget.analytics.tab.CategoriesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AnalyticsModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

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
        val categories: CategoriesModel.Skeleton = CategoriesModel.Skeleton(),
    )

    val accounts: AccountsModel = AccountsModel(
        scope = scope,
        dependencies = dependencies.accounts(),
        skeleton = skeleton.accounts
    )

    val categories: CategoriesModel = CategoriesModel(
        scope = scope,
        dependencies = dependencies.categories(),
        skeleton = skeleton.categories
    )

    val selectedTab: MutableStateFlow<AnalyticsTab>
        get() = skeleton.selectedTab

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}