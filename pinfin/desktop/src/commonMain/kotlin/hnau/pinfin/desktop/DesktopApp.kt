package hnau.pinfin.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import hnau.common.app.preferences.impl.FileBasedPreferences
import hnau.common.compose.utils.ThemeBrightness
import hnau.pinfin.app.PinFinApp
import hnau.pinfin.app.SavedState
import hnau.pinfin.app.impl
import hnau.pinfin.compose.AppContentDependencies
import hnau.pinfin.compose.Content
import hnau.pinfin.compose.impl
import hnau.pinfin.model.utils.budget.storage.impl.FileBasedBudgetsStorage
import org.slf4j.simple.SimpleLogger
import java.io.File

@OptIn(InternalComposeApi::class)
fun main() = application {
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
    val scale = 2f
    val appScope = rememberCoroutineScope()
    val app = PinFinApp(
        scope = appScope,
        savedState = SavedState(null),
        dependencies = PinFinApp.Dependencies.impl(
            budgetsStorageFactory = FileBasedBudgetsStorage.Factory(
                budgetsDir = File("budgets"),
            ),
            preferencesFactory = FileBasedPreferences.Factory(
                preferencesFile = File("preferences.txt")
            )
        ),
    )
    val appContentDependencies = AppContentDependencies.impl(
        dynamicColorsGenerator = null,
    )
    Window(
        onCloseRequest = { exitApplication() },
        title = "PinFin",
        state = rememberWindowState(
            width = 480.dp * scale,
            height = 640.dp * scale,
        ),
        //icon = rememberVectorPainter(pinfinIcon.s256),
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(scale),
        ) {
            app.Content(
                dependencies = appContentDependencies,
                forcedBrightness = ThemeBrightness.Dark,
            )
        }
    }
}
