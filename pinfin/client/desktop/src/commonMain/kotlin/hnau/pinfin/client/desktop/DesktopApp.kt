package hnau.pinfin.client.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import hnau.common.compose.utils.AppInsets
import hnau.common.compose.utils.ThemeBrightness
import hnau.pinfin.client.app.PinFinApp
import hnau.pinfin.client.app.SavedState
import hnau.pinfin.client.app.impl
import hnau.pinfin.client.compose.AppContentDependencies
import hnau.pinfin.client.compose.Content
import hnau.pinfin.client.compose.impl
import hnau.pinfin.client.data.FileBasedBudgetsRepositoryFactory
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
            budgetsRepositoryFactory = FileBasedBudgetsRepositoryFactory(
                budgetsDirectory = File("budgets"),
            ),
        ),
    )
    val appContentDependencies = AppContentDependencies.impl(
        dynamicColorsGenerator = null,
        appInsets = AppInsets.empty,
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
