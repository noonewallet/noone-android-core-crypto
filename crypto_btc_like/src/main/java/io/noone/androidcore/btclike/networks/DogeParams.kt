package io.noone.androidcore.btclike.networks

import io.noone.androidcore.utils.toBytesBE


object DogeParams : NetworkParameters() {
    override val dumpedPrivateKeyHeader = 158
    override val addressHeader = 30
    override val p2SHHeader = 22
    override val slip32PrivKeyPrefix: ByteArray
        get() = 0x02fac398.toBytesBE()
    override val slip32PubKeyPrefix: ByteArray
        get() = 0x02facafd.toBytesBE()
}
