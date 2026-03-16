package org.hnau.pinfin.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.remember
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalPlatformWindowInsets
import androidx.compose.ui.platform.PlatformInsets
import androidx.compose.ui.platform.PlatformWindowInsets
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.app.model.app.DesktopApp
import org.hnau.commons.app.model.theme.ThemeBrightness
import kotlinx.coroutines.runBlocking

@OptIn(InternalComposeApi::class, InternalComposeUiApi::class)
fun main() = runBlocking {
    val app = DesktopApp(
        scope = this,
        seed = createPinFinAppSeed(
            defaultBrightness = ThemeBrightness.Dark,
            dependencies = PinFinAppDependencies.impl(
                inetAddressesProvider = JvmInetAddressesProvider,
                sha256 = JvmSha256,
            )
        ),
    )
    val projector = createAppProjector(
        scope = this,
        model = app,
    )
    application {
        val scale = 2f
        Window(
            onCloseRequest = { exitApplication() },
            title = "PinFin",
            state = rememberWindowState(
                width = 320.dp * scale,
                height = 400.dp * scale,
            ),
            //icon = rememberVectorPainter(pinfinIcon.s256),
        ) {
            val density = remember(scale) { Density(scale) }
            val insets = remember(density) { WindowInsets(density) }
            CompositionLocalProvider(
                LocalDensity provides density,
                LocalPlatformWindowInsets provides insets,
            ) {
                projector.Content()
            }
        }
    }
}

@OptIn(InternalComposeUiApi::class)
private class WindowInsets(
    density: Density,
) : PlatformWindowInsets {

    override val systemBars: PlatformInsets = density.PlatformInsets(
        top = 16.dp,
        bottom = 16.dp,
    )
}