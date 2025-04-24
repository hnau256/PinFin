package hnau.pinfin.android

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hnau.pinfin.app.PinFinApp
import hnau.pinfin.app.SavedState
import hnau.pinfin.app.impl
import hnau.pinfin.data.repository.a.FileBasedBudgetsRepositoryFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.File

class AppViewModel(
    context: Context,
    private val state: SavedStateHandle,
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob())

    val app = PinFinApp(
        scope = scope,
        dependencies = PinFinApp.Dependencies.impl(
            budgetsRepositoryFactory = FileBasedBudgetsRepositoryFactory(
                budgetsDirectory = File(context.filesDir, "budgets"),
            ),
        ),
        savedState = SavedState(
            state
                .get<Bundle>(StateKey)
                ?.getString(StateKey),
        ),
    )

    init {
        state.setSavedStateProvider(StateKey) {
            Bundle().apply { putString(StateKey, app.savableState.savedState) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    companion object {

        private const val StateKey = "state"

        fun factory(
            context: Context,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                AppViewModel(
                    context = context,
                    state = savedStateHandle,
                )
            }
        }
    }
}
