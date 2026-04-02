package org.hnau.pinfin.app

import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.model.utils.budget.upchain.Sha256

@Pipe
interface PinFinAppDependencies {

    val sha256: Sha256

    val currency: Currency

    companion object
}