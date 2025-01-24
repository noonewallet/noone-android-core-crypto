package io.noone.androidcore.btclike.networks

import io.noone.androidcore.utils.toBytesBE

object LitecoinParams : NetworkParameters() {
    override val addressHeader = 48
    override val dumpedPrivateKeyHeader = 128 + addressHeader
    override val p2SHHeader = 50
    override val segwitAddressHrp = "ltc"

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