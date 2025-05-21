package hnau.pinfin.android

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hnau.common.model.preferences.impl.FileBasedPreferences
import hnau.pinfin.app.PinFinApp
import hnau.pinfin.app.SavedState
import hnau.pinfin.app.impl
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pinfin.model.utils.budget.storage.impl.files
import java.io.File

class AppViewModel(
    context: Context,
    private val state: SavedStateHandle,
) : ViewModel() {

    val appFilesDir: File = context.filesDir
    val app = PinFinApp(
        scope = viewModelScope,
        dependencies = PinFinApp.Dependencies.impl(
            budgetsStorageFactory = BudgetsStorage.Factory.files(
                budgetsDir = File(appFilesDir, "budgets"),
            ),
            preferencesFactory = FileBasedPreferences.Factory(
                preferencesFile = File(appFilesDir, "preferences.txt")
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
