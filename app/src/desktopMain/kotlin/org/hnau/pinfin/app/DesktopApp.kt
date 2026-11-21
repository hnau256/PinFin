package org.hnau.pinfin.app

import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import kotlinx.io.files.Path
import org.hnau.commons.app.model.file.File
import org.hnau.commons.app.model.utils.ClipboardAccessor
import org.hnau.commons.app.model.utils.createForJvm
import org.hnau.commons.app.projector.app.launchDesktopApplication

fun main() {
    Logger.setLogWriters(platformLogWriter())
    launchDesktopApplication(
        title = "PinFin",
        windowWidth = 512.dp,
        windowHeight = 800.dp,
        rootModelFactory = createPinFinRootModelFactory(
            dependencies = PinFinAppDependencies.impl(
                clipboardAccessor = ClipboardAccessor.createForJvm(),
            ),
            appFilesDir = File(Path("")),
        ),
        rootProjectorFactory = PinFinRootProjectorFactory,
    )
}
