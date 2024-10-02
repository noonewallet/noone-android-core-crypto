package io.noone.androidcore.btclike.networks

import io.noone.androidcore.utils.toBytesBE

object BitcoinParams : NetworkParameters() {
    override val dumpedPrivateKeyHeader = 128
    override val addressHeader = 0
    override val p2SHHeader = 5
    override val segwitAddressHrp = "bc"
    override val slip32PrivKeyPrefix: ByteArray
        get() = 0x0488ade4.toBytesBE()
    override val slip32PubKeyPrefix: ByteArray
        get() = 0x0488b21e.toBytesBE()
}