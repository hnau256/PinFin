package hnau.pinfin.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import hnau.common.app.model.app.AppViewModel
import hnau.pinfin.model.RootModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppActivity : ComponentActivity() {

    private val viewModel: AppViewModel<RootModel, RootModel.Skeleton> by viewModels {
        AppViewModel.Companion.factory(
            context = applicationContext,
            seed = createPinFinAppSeed(),
        )
    }

    private val goBackHandler: StateFlow<(() -> Unit)?>
        get() = viewModel.appModel.goBackHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initOnBackPressedDispatcherCallback()
        val projector = createAppProjector(
            scope = lifecycleScope,
            model = viewModel.appModel,
        )
        setContent {
            projector.Content()
        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onBackPressed() {
        if (useOnBackPressedDispatcher) {
            super.onBackPressed()
        }
        goBackHandler
            .value
            ?.invoke()
            ?: super.onBackPressed()
    }

    private fun initOnBackPressedDispatcherCallback() {
        if (!useOnBackPressedDispatcher) {
            return
        }
        val callback = object : OnBackPressedCallback(
            enabled = goBackHandler.value != null,
        ) {
            override fun handleOnBackPressed() {
                goBackHandler.value?.invoke()
            }
        }
        lifecycleScope.launch {
            goBackHandler
                .map { it != null }
                .distinctUntilChanged()
                .collect { goBackIsAvailable ->
                    callback.isEnabled = goBackIsAvailable
                }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    companion object {

        private val useOnBackPressedDispatcher: Boolean = Build.VERSION.SDK_INT >= 33
    }
}