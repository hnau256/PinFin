package org.hnau.pinfin.app

import org.hnau.commons.app.model.utils.ClipboardAccessor
import org.hnau.commons.gen.pipe.annotations.Pipe

@Pipe
interface PinFinAppDependencies {

    val clipboardAccessor: ClipboardAccessor

    companion object
}