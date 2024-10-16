package io.noone.androidcore.btclike.networks

import io.noone.androidcore.utils.toBytesBE

object LitecoinParams : NetworkParameters() {
    override val addressHeader = 48
    override val dumpedPrivateKeyHeader = 128 + addressHeader
    override val p2SHHeader = 50
    override val segwitAddressHrp = "ltc"

    override val slip32PubKeyPrefixXpub: ByteArray
        get() = 0x019da462.toBytesBE()
    override val slip32PubKeyPrefixYpub: ByteArray
        get() = 0x01b26ef6.toBytesBE()
    override val slip32PubKeyPrefixZpub: ByteArray
        get() = 0x01b258ad.toBytesBE()

    override val slip32PrivKeyPrefixXprv: ByteArray
        get() = 0x019d9cfe.toBytesBE()
    override val slip32PrivKeyPrefixYprv: ByteArray
        get() = 0x01b26792.toBytesBE()
    override val slip32PrivKeyPrefixZprv: ByteArray
        get() = 0x01b255bc.toBytesBE()
}