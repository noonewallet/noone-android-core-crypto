package io.noone.androidcore.btclike.networks

import io.noone.androidcore.utils.toBytesBE


object DogeParams : NetworkParameters() {
    override val dumpedPrivateKeyHeader = 158
    override val addressHeader = 30
    override val p2SHHeader = 22
    override val slip32PrivKeyPrefixXprv: ByteArray
        get() = 0x02fac398.toBytesBE()
    override val slip32PubKeyPrefixXpub: ByteArray
        get() = 0x02facafd.toBytesBE()

    override val slip32PubKeyPrefixYpub: ByteArray
        get() = throw NotImplementedError()
    override val slip32PubKeyPrefixZpub: ByteArray
        get() = throw NotImplementedError()
    override val slip32PrivKeyPrefixYprv: ByteArray
        get() = throw NotImplementedError()
    override val slip32PrivKeyPrefixZprv: ByteArray
        get() = throw NotImplementedError()
}