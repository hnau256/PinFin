package org.hnau.pinfin.android

import kotlinx.io.files.Path
import org.hnau.commons.app.model.app.RootModelFactory
import org.hnau.commons.app.model.file.File
import org.hnau.commons.app.model.utils.ClipboardAccessor
import org.hnau.commons.app.model.utils.createForAndroid
import org.hnau.commons.app.projector.app.RootProjectorFactory
import org.hnau.pinfin.app.PinFinAppDependencies
import org.hnau.pinfin.app.PinFinRootProjectorFactory
import org.hnau.pinfin.app.createPinFinRootModelFactory
import org.hnau.pinfin.app.impl
import org.hnau.pinfin.model.RootModel
import org.hnau.pinfin.projector.RootProjector
import org.hnau.commons.app.projector.app.AppActivity

class PinFinActivity : AppActivity<RootModel.Skeleton, RootModel, RootProjector>() {

    override fun createRootModelFactory(): RootModelFactory<RootModel.Skeleton, RootModel> =
        createPinFinRootModelFactory(
            dependencies = PinFinAppDependencies.impl(
                clipboardAccessor = ClipboardAccessor.createForAndroid(
                    context = applicationContext,
                ),
            ),
            appFilesDir = filesDir
                .absolutePath
                .let(::Path)
                .let(::File),
        )

    override fun createRootProjectorFactory(): RootProjectorFactory<RootModel, RootProjector> =
        PinFinRootProjectorFactory
}
