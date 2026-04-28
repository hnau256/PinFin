package org.hnau.pinfin.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.remember
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import kotlinx.coroutines.runBlocking
import org.hnau.commons.app.model.app.AppFilesDirProvider
import org.hnau.commons.app.model.app.DesktopApp
import org.hnau.commons.app.model.theme.ThemeBrightness
import org.hnau.commons.app.model.theme.palette.SystemPalettes
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.pinfin.data.Currency

@OptIn(InternalComposeApi::class, InternalComposeUiApi::class)
fun main() = runBlocking {

    Logger.setLogWriters(platformLogWriter())

    val app = DesktopApp(
        scope = this,
        seed = createPinFinAppSeed(
            dependencies = PinFinAppDependencies.impl(
                currency = Currency.default, //TODO
            ),
            appFilesDirProvider = AppFilesDirProvider(),
        ),
    )
    val projector = createAppProjector(
        scope = this,
        model = app,
        createSystemPalettes = { SystemPalettes.None },
    )
    application {
        val scale = 2f
        Window(
            onCloseRequest = { exitApplication() },
            title = "PinFin",
            state = rememberWindowState(
                width = 256.dp * scale,
                height = 400.dp * scale,
            ),
            //icon = rememberVectorPainter(pinfinIcon.s256),
        ) {
            val density = remember(scale) { Density(scale) }
            CompositionLocalProvider(
                LocalDensity provides density,
            ) {
                projector.Content(
                    contentPadding = PaddingValues(
                        vertical = Dimens.separation,
                    )
                )
            }
        }
    }
}