package org.hnau.pinfin.model.transaction.edit

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe

class TransactionEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

    }

    @Serializable
    /*data*/ class Skeleton(

    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}