package org.hnau.pinfin.model.utils

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.withFallback
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.kotlin.coroutines.InProgressRegistry
import org.hnau.commons.kotlin.coroutines.actionOrNullIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.foldNullable

@Deprecated("Move to commons-app-model")
class ModelSavableDelegate<T>(
    private val scope: CoroutineScope,
    private val result: StateFlow<Editable<T>>,
    private val skeleton: Skeleton<T>,
    private val modelGoBackHandler: GoBackHandler,
    private val close: () -> Unit,
    private val save: suspend (T) -> Unit,
) {

    @Serializable
    data class Skeleton<T>(
        val dialog: MutableStateFlow<DialogState<T>> =
            DialogState.None.toMutableStateFlowAsInitial()
    ) {

        sealed interface DialogState<out T> {

            data object None : DialogState<Nothing>

            data class Visible<out T>(
                val valueToSave: Option<T>,
            ) : DialogState<T>
        }
    }

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

    val dialog: StateFlow<ExitWithoutSavingDialog?> = skeleton
        .dialog
        .mapWithScope(scope) { scope, dialog ->
            when (dialog) {
                Skeleton.DialogState.None -> null
                is Skeleton.DialogState.Visible<T> -> ExitWithoutSavingDialog(
                    returnToEditing = { skeleton.dialog.value = Skeleton.DialogState.None },
                    exitWithoutSaving = close,
                    saveAndExitIfPossible = dialog
                        .valueToSave
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

    private fun createShowDialogIfNecessaryGoBackHandler(
        scope: CoroutineScope,
    ): GoBackHandler = result.mapState(scope) { result ->
        when (result) {
            Editable.Incorrect -> {
                {
                    skeleton.dialog.value = Skeleton.DialogState.Visible(
                        valueToSave = None,
                    )
                }
            }

            is Editable.Value<T> -> result.changed.foldBoolean(
                ifFalse = { null },
                ifTrue = {
                    {
                        skeleton.dialog.value =
                            Skeleton.DialogState.Visible(
                                valueToSave = result.value.some(),
                            )
                    }
                }
            )
        }
    }

    val goBackHandler: GoBackHandler = dialog.flatMapWithScope(
        scope = scope,
    ) { scope, dialogOrNull ->
        dialogOrNull.foldNullable(
            ifNotNull = {
                { skeleton.dialog.value = Skeleton.DialogState.None }.toMutableStateFlowAsInitial()
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