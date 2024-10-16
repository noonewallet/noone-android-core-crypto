package io.noone.androidcore.btclike.networks

import io.noone.androidcore.utils.toBytesBE

object BitcoinParams : NetworkParameters() {
    override val dumpedPrivateKeyHeader = 128
    override val addressHeader = 0
    override val p2SHHeader = 5
    override val segwitAddressHrp = "bc"

    override val slip32PubKeyPrefixXpub: ByteArray
        get() = 0x0488b21e.toBytesBE()
    override val slip32PubKeyPrefixYpub: ByteArray
        get() = 0x049d7cb2.toBytesBE()
    override val slip32PubKeyPrefixZpub: ByteArray
        get() = 0x04b24746.toBytesBE()

    override val slip32PrivKeyPrefixXprv: ByteArray
        get() = 0x0488ade4.toBytesBE()
    override val slip32PrivKeyPrefixYprv: ByteArray
        get() = 0x049d7878.toBytesBE()
    override val slip32PrivKeyPrefixZprv: ByteArray
        get() = 0x04b2430c.toBytesBE()
}