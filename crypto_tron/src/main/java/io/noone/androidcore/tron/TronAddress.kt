package io.noone.androidcore.tron

import io.noone.androidcore.ECKey
import io.noone.androidcore.utils.Base58
import io.noone.androidcore.utils.keccak256

class TronAddress(
    private val encodedPubKey: ByteArray
) {
    companion object {
        const val PREFIX = 0x41

        fun fromEcKey(ecKey: ECKey): TronAddress = TronAddress(ecKey.pubKeyPoint.getEncoded(false))
        
    }

    val hash: ByteArray by lazy {
        encodedPubKey.copyOfRange(1, encodedPubKey.size)
            .keccak256
            .copyOfRange(12, 32)
    }

    val address: String by lazy {
        Base58.encodeChecked(PREFIX, hash)
    }

    override fun toString(): String = address

}