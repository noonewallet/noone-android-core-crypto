package io.noone.androidcore.bnb

import io.noone.androidcore.ECKey
import io.noone.androidcore.utils.Bech32
import io.noone.androidcore.utils.BitsConverter
import io.noone.androidcore.utils.sha256hash160

class Address {

    companion object {

        const val ADDRESS_PREFIX = "bnb"

        fun isValid(address: String?): Boolean {
            return try {
                val data: Bech32.Bech32Data = Bech32.decode(address)
                if (data.hrp != ADDRESS_PREFIX) return false
                val converted = BitsConverter.convertBits(data.data, 0, data.data.size, 5, 8, false)
                val convertedLength = converted?.size ?: 0
                convertedLength in 2..40
            } catch (_: Exception) {
                false
            }
        }
    }

    constructor(privateKey: ECKey) : this(privateKey.pubKey)

    constructor(publicKey: ByteArray) {
        val hash: ByteArray = publicKey.sha256hash160
        val converted = BitsConverter.convertBits(hash, 0, hash.size, 8, 5, true)
        address = Bech32.encode(ADDRESS_PREFIX, converted)
    }

    constructor(address: String) {
        require(isValid(address)) { "Address is invalid" }
        this.address = address
    }

    val bytes: ByteArray
        get() {
            val dec: ByteArray = Bech32.decode(address).data
            return BitsConverter.convertBits(dec, 0, dec.size, 5, 8, false)
        }

    private val address: String

    override fun toString(): String = address
}