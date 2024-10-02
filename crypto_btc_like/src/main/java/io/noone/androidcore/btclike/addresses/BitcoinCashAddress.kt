package io.noone.androidcore.btclike.addresses


import io.noone.androidcore.ECKey
import io.noone.androidcore.btclike.networks.NetworkParameters
import io.noone.androidcore.btclike.transaction.ScriptType
import io.noone.androidcore.exceptions.AddressFormatException
import io.noone.androidcore.utils.Base58

class BitcoinCashAddress @Throws(AddressFormatException::class) private constructor(
    params: NetworkParameters,
    hash160: ByteArray
) : Address(params, hash160) {

    companion object {

        @Throws(AddressFormatException::class)
        fun fromPubKeyHash(params: NetworkParameters, hash160: ByteArray): BitcoinCashAddress =
            BitcoinCashAddress(params, hash160)

        fun fromKey(params: NetworkParameters, key: ECKey): BitcoinCashAddress {
            return fromPubKeyHash(params, key.pubKeyHash)
        }
    }

    val version: Int
        get() = params.addressHeader

    override val hash: ByteArray
        get() = bytes

    override val outputScriptType: ScriptType
        get() = ScriptType.P2PKH

    val bech32: String by lazy {
        Bech32Bch.encode(byteArrayOf(version.toByte(), *bytes))
    }

    val base58: String by lazy {
        Base58.encodeChecked(version, bytes)
    }

    init {
        if (hash160.size != 20)
            throw AddressFormatException.InvalidDataLength(
                "Legacy addresses are 20 byte (160 bit) hashes, but got: " + hash160.size
            )
    }

    override fun toString(): String = "bch format: %s\nlegacy format: %s".format(bech32, base58)

}