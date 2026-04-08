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
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.kotlin.coroutines.InProgressRegistry
import org.hnau.commons.kotlin.coroutines.actionOrNullIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

@Deprecated("Move to commons-app-model")
class ModelSavableDelegate<T>(
    private val scope: CoroutineScope,
    private val result: StateFlow<Editable<T>>,
    private val skeleton: Skeleton<T>,
    private val modelGoBackHandler: GoBackHandler,
    private val close: () -> Unit,
    private val save: suspend (T) -> Unit,
) {

    private val blockBackDelegate: ModelBlockBackDelegate<Option<T>> = ModelBlockBackDelegate(
        scope = scope,
        skeleton = skeleton.blockBack,
        modelGoBackHandler = modelGoBackHandler,
        blockReason = result.mapState(scope) { editable ->
            when (editable) {
                Editable.Incorrect -> None.some()
                is Editable.Value<T> -> editable
                    .changed
                    .foldBoolean(
                        ifTrue = { editable.value.some().some() },
                        ifFalse = { None }
                    )
            }
        }
    )

    @Serializable
    data class Skeleton<T>(
        val blockBack: ModelBlockBackDelegate.Skeleton<Option<T>> = ModelBlockBackDelegate.Skeleton()
    )

    private val inProgressRegistry = InProgressRegistry(scope)

    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.inProgress

    private fun saveAndCloseFlow(
        scope: CoroutineScope,
        value: T,
    ): StateFlow<(() -> Unit)?> = actionOrNullIfExecuting(
        scope = scope,
    ) {
        inProgressRegistry.executeRegistered {
            save(value)
            close()
        }
    }

    val saveOrInactive: StateFlow<StateFlow<(() -> Unit)?>?> = result
        .mapWithScope(scope) { scope, result ->
            when (result) {
                Editable.Incorrect -> null
                is Editable.Value<T> -> result.changed.foldBoolean(
                    ifFalse = { MutableStateFlow(close) },
                    ifTrue = {
                        saveAndCloseFlow(
                            scope = scope,
                            value = result.value,
                        )
                    }
                )
            }
        }

    data class ExitWithoutSavingDialog(
        val returnToEditing: () -> Unit,
        val exitWithoutSaving: () -> Unit,
        val saveAndExitIfPossible: (StateFlow<(() -> Unit)?>)?
    )

    val dialog: StateFlow<ExitWithoutSavingDialog?> = blockBackDelegate
        .dialog
        .mapState(scope) { dialogOrNull ->
            dialogOrNull?.let { dialog ->
                ExitWithoutSavingDialog(
                    returnToEditing = dialog.close,
                    exitWithoutSaving = close,
                    saveAndExitIfPossible = dialog
                        .blockReason
                        .map { value ->
                            saveAndCloseFlow(
                                scope = scope,
                                value = value,
                            )
                        }
                        .getOrNull()
                )
            }
        }

    val goBackHandler: GoBackHandler
        get() = blockBackDelegate.goBackHandler
}