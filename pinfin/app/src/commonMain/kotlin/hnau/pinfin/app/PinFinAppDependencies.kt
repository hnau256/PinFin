package hnau.pinfin.app

import hnau.pinfin.model.sync.server.InetAddressesProvider
import hnau.pinfin.model.utils.budget.upchain.Sha256
import org.hnau.commons.gen.pipe.annotations.Pipe

@Pipe
interface PinFinAppDependencies {

    val inetAddressesProvider: InetAddressesProvider

    val sha256: Sha256

    companion object
}