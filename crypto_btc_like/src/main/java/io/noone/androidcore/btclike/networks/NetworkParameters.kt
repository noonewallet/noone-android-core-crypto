package io.noone.androidcore.btclike.networks

import io.noone.androidcore.btclike.transaction.HashType

abstract class NetworkParameters {
    abstract val addressHeader: Int
    abstract val p2SHHeader: Int
    abstract val dumpedPrivateKeyHeader: Int
    abstract val slip32PubKeyPrefixXpub: ByteArray
    abstract val slip32PubKeyPrefixYpub: ByteArray
    abstract val slip32PubKeyPrefixZpub: ByteArray

    abstract val slip32PrivKeyPrefixXprv: ByteArray
    abstract val slip32PrivKeyPrefixYprv: ByteArray
    abstract val slip32PrivKeyPrefixZprv: ByteArray


    open val segwitAddressHrp: String? = null
    open val hashType: HashType = HashType.REVERSED

}