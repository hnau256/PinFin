package hnau.pinfin.client.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import hnau.common.app.storage.Storage
import hnau.common.app.storage.file
import hnau.pinfin.client.app.pinfinApp
import hnau.pinfin.client.app.SavedState
import hnau.pinfin.client.app.commonImpl
import hnau.pinfin.client.compose.Content
import hnau.pinfin.client.compose.utils.pinfinIcon
import hnau.pinfin.client.compose.utils.LocalizerImpl
import hnau.pinfin.client.projector.common.Localizer
import org.slf4j.simple.SimpleLogger
import java.io.File

@OptIn(InternalComposeApi::class)
fun main() = application {
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
    val scale = 2f
    val appScope = rememberCoroutineScope()
    val app = pinfinApp(
        scope = appScope,
        savedState = SavedState(null),
        dependencies = pinfinApp.Dependencies.commonImpl(
            storageFactory = Storage.Factory.file(
                file = File("storage.json"),
            ),
        ),
    )
    val localizer: Localizer = LocalizerImpl
    Window(
        onCloseRequest = { exitApplication() },
        title = "pinfin",
        state = rememberWindowState(width = 720.dp * scale, height = 640.dp * scale),
        icon = rememberVectorPainter(pinfinIcon.s256),
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(scale),
            LocalSystemTheme provides SystemTheme.Dark,
        ) {
            app.Content(
                localizer = localizer,
            )
        }
    }
}
