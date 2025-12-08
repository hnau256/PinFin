package hnau.pinfin.app

import hnau.pinfin.model.sync.server.InetAddressesProvider
import java.net.InetAddress
import java.net.NetworkInterface

object JvmInetAddressesProvider: InetAddressesProvider {

    override val addresses: List<String> = NetworkInterface
    .getNetworkInterfaces()
    .asSequence()
    .filter { it.isUp && !it.isLoopback }
    .flatMap { it.inetAddresses.asSequence() }
    .filterNotNull()
    .map(InetAddress::getHostAddress)
    .mapNotNull { it.takeIf(String::isNotEmpty) }
    .toList()
    .sortedBy(String::length)
}