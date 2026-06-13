@file:UseSerializers(
    MutableStateFlowSerializer::class,
    OptionSerializer::class,
)

package org.hnau.pinfin.model.utils

import arrow.core.None
import arrow.core.Option
import arrow.core.serialization.OptionSerializer
import arrow.core.some
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.withFallback
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

@Deprecated("Move to commons-app-model")
class ModelBlockBackDelegate<B>(
    private val scope: CoroutineScope,
    private val blockReason: StateFlow<Option<B>>,
    private val skeleton: Skeleton<B>,
    private val modelGoBackHandler: GoBackHandler,
) {

    @Serializable
    data class Skeleton<T>(
        val visibleBlockReason: MutableStateFlow<Option<T>> = None.toMutableStateFlowAsInitial()
    )

    data class BlockReasonDialog<B>(
        val blockReason: B,
        val close: () -> Unit,
    )

    val dialog: StateFlow<BlockReasonDialog<B>?> = skeleton
        .visibleBlockReason
        .mapState(scope) { reasonOrNone ->
            reasonOrNone
                .map { reason ->
                    BlockReasonDialog(
                        blockReason = reason,
                        close = { skeleton.visibleBlockReason.value = None },
                    )
                }
                .getOrNull()
        }

    private fun createShowDialogIfNecessaryGoBackHandler(
        scope: CoroutineScope,
    ): GoBackHandler = blockReason.mapState(scope) { resultOrNone ->
        resultOrNone
            .map { blockReason -> { skeleton.visibleBlockReason.value = blockReason.some() } }
            .getOrNull()
    }

    val goBackHandler: GoBackHandler = dialog.flatMapWithScope(
        scope = scope,
    ) { scope, stateOrNull ->
        stateOrNull.foldNullable(
            ifNotNull = {
                { skeleton.visibleBlockReason.value = None }.toMutableStateFlowAsInitial()
            },
            ifNull = {
                modelGoBackHandler.withFallback(
                    scope = scope,
                    createFallback = ::createShowDialogIfNecessaryGoBackHandler,
                )
            },
        )
    }
}