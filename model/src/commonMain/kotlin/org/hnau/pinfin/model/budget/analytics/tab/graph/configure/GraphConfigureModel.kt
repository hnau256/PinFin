package org.hnau.pinfin.model.budget.analytics.tab.graph.configure

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsConfig

class GraphConfigureModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    onReady: (AnalyticsConfig) -> Unit,
    onCancel: () -> Unit,
) {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton(

    ) {
        companion object {

            fun create(
                initialConfig: AnalyticsConfig,
            ): Skeleton = TODO()
        }

    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}