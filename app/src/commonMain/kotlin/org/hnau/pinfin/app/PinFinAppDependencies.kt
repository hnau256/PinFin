package org.hnau.pinfin.app

import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.data.Currency

@Pipe
interface PinFinAppDependencies {

    val currency: Currency

    companion object
}