package hnau.pinfin.client.android

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hnau.common.app.storage.Storage
import hnau.pinfin.client.app.PinFinApp
import hnau.pinfin.client.app.SavedState
import hnau.pinfin.client.app.commonImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AppViewModel(
    context: Context,
    private val state: SavedStateHandle,
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob())

    val app = PinFinApp(
        scope = scope,
        dependencies = PinFinApp.Dependencies.commonImpl(
            storageFactory = Storage.Factory.dataStore(
                scope = scope,
                context = context,
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
