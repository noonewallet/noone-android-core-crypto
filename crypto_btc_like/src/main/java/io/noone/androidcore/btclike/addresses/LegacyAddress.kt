package io.noone.androidcore.btclike.addresses

import io.noone.androidcore.ECKey
import io.noone.androidcore.btclike.networks.NetworkParameters
import io.noone.androidcore.btclike.transaction.ScriptType
import io.noone.androidcore.exceptions.AddressFormatException
import io.noone.androidcore.utils.Base58

class LegacyAddress @Throws(AddressFormatException::class) private constructor(
    params: NetworkParameters,
    val p2sh: Boolean,
    hash160: ByteArray
) : Address(params, hash160) {

    companion object {

        @Throws(AddressFormatException::class)
        fun fromPubKeyHash(params: NetworkParameters, hash160: ByteArray, p2sh: Boolean = false): LegacyAddress {
            return LegacyAddress(params, p2sh, hash160)
        }

        fun fromKey(params: NetworkParameters, key: ECKey): LegacyAddress {
            return fromPubKeyHash(
                params,
                key.pubKeyHash
            )
        }
    }

    val version: Int
        get() = if (p2sh) params.p2SHHeader else params.addressHeader

    override val hash: ByteArray
        get() = bytes

    override val outputScriptType: ScriptType
        get() = if (p2sh) ScriptType.P2SH else ScriptType.P2PKH

    init {
        if (hash160.size != 20)
            throw AddressFormatException.InvalidDataLength(
                "Legacy addresses are 20 byte (160 bit) hashes, but got: " + hash160.size
            )
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun toBase58(): String = Base58.encodeChecked(version, bytes)

    override fun equals(o: Any?): Boolean {
        if (this === o)
            return true
        if (o == null || javaClass != o.javaClass)
            return false
        val other = o as LegacyAddress?
        return super.equals(other) && this.p2sh == other.p2sh
    }

    override fun hashCode(): Int {
        var hash = 17
        hash = hash * 23 + super.hashCode()
        hash = hash * 23 + p2sh.hashCode()
        return hash
    }

    override fun toString(): String = toBase58()

}
