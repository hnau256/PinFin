package hnau.pinfin.projector.budget.analytics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import hnau.pinfin.model.budget.analytics.tab.AnalyticsTab
import hnau.pinfin.projector.budget.analytics.graph.GraphProjector

sealed interface AnalyticsTabProjector {

    val tab: AnalyticsTab

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    )

    data class Accounts(
        val projector: AccountsProjector,
    ) : AnalyticsTabProjector {

        override val tab: AnalyticsTab
            get() = AnalyticsTab.Accounts

        @Composable
        override fun Content(
            contentPadding: PaddingValues,
        ) {
            projector.Content(
                contentPadding = contentPadding,
            )
        }
    }

    data class Categories(
        val projector: CategoriesProjector,
    ) : AnalyticsTabProjector {

        override val tab: AnalyticsTab
            get() = AnalyticsTab.Categories

        @Composable
        override fun Content(
            contentPadding: PaddingValues,
        ) {
            projector.Content(
                contentPadding = contentPadding,
            )
        }
    }

    data class Graph(
        val projector: GraphProjector,
    ) : AnalyticsTabProjector {

        override val tab: AnalyticsTab
            get() = AnalyticsTab.Graph

        @Composable
        override fun Content(
            contentPadding: PaddingValues,
        ) {
            projector.Content(
                contentPadding = contentPadding,
            )
        }
    }
}