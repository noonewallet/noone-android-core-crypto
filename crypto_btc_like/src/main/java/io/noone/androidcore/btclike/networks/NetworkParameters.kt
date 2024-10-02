package io.noone.androidcore.btclike.networks

import io.noone.androidcore.btclike.transaction.HashType

abstract class NetworkParameters {
    abstract val addressHeader: Int
    abstract val p2SHHeader: Int
    abstract val dumpedPrivateKeyHeader: Int
    abstract val slip32PubKeyPrefix: ByteArray
    abstract val slip32PrivKeyPrefix: ByteArray

    open val segwitAddressHrp: String? = null
    open val hashType: HashType = HashType.REVERSED

}