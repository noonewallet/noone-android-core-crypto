package io.noone.androidcore.btclike.networks

import io.noone.androidcore.utils.toBytesBE

object LitecoinParams : NetworkParameters() {
    override val addressHeader = 48
    override val dumpedPrivateKeyHeader = 128 + addressHeader
    override val p2SHHeader = 50
    override val segwitAddressHrp = "ltc"
    override val slip32PrivKeyPrefix: ByteArray
        get() = 0x019d9cfe.toBytesBE()
    override val slip32PubKeyPrefix: ByteArray
        get() = 0x019da462.toBytesBE()
}