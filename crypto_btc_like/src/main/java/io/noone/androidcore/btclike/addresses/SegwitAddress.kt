package io.noone.androidcore.btclike.addresses

import io.noone.androidcore.ECKey
import io.noone.androidcore.btclike.networks.NetworkParameters
import io.noone.androidcore.btclike.opcodes.OpCodes
import io.noone.androidcore.btclike.transaction.ScriptType
import io.noone.androidcore.exceptions.AddressFormatException
import io.noone.androidcore.utils.Bech32
import io.noone.androidcore.utils.BitsConverter.convertBits
import kotlin.experimental.and

class SegwitAddress @Throws(AddressFormatException::class) private constructor(
    params: NetworkParameters,
    data: ByteArray
) : Address(params, data) {

    companion object {
        const val WITNESS_PROGRAM_LENGTH_PKH = 20
        const val WITNESS_PROGRAM_MIN_LENGTH = 2
        const val WITNESS_PROGRAM_MAX_LENGTH = 40

        @Throws(AddressFormatException::class)
        private fun encode(witnessVersion: Int, witnessProgram: ByteArray): ByteArray {
            val convertedProgram = convertBits(witnessProgram, 0, witnessProgram.size, 8, 5, true)
            val bytes = ByteArray(1 + convertedProgram.size)
            bytes[0] = (OpCodes.encodeToOpN(witnessVersion) and 0xff).toByte()
            System.arraycopy(convertedProgram, 0, bytes, 1, convertedProgram.size)
            return bytes
        }

        fun fromBech32(
            params: NetworkParameters,
            bech32address: String
        ): SegwitAddress {
            val bech32data = Bech32.decode(bech32address)
            require(bech32data.hrp == params.segwitAddressHrp) {
                "Network params hrp is: ${params.segwitAddressHrp}.\nAddress hrp is: ${bech32data.hrp}"
            }
            return SegwitAddress(params, bech32data.data)
        }

        fun fromHash(
            params: NetworkParameters,
            hash: ByteArray
        ): SegwitAddress = SegwitAddress(params, 0, hash)

        fun fromKey(
            params: NetworkParameters,
            key: ECKey
        ): SegwitAddress {
            require(key.isCompressed) { "only compressed keys allowed" }
            return fromHash(params, key.pubKeyHash)
        }

        fun validate(address: String, networkParameters: NetworkParameters): Boolean {
            return networkParameters.segwitAddressHrp
                ?.takeIf { address.startsWith(it) }
                ?.let {
                    fromBech32(
                        networkParameters,
                        address
                    ).witnessProgram.size == WITNESS_PROGRAM_LENGTH_PKH
                } ?: false
        }

    }

    @Throws(AddressFormatException::class)
    private constructor(
        params: NetworkParameters,
        witnessVersion: Int,
        witnessProgram: ByteArray
    ) : this(params, encode(witnessVersion, witnessProgram))

    init {
        if (data.isEmpty())
            throw AddressFormatException.InvalidDataLength("Zero data found")
        if (witnessVersion < 0 || witnessVersion > 16)
            throw AddressFormatException("Invalid script version: $witnessVersion")
        if (witnessProgram.size < WITNESS_PROGRAM_MIN_LENGTH || witnessProgram.size > WITNESS_PROGRAM_MAX_LENGTH)
            throw AddressFormatException.InvalidDataLength("Invalid length: ${witnessProgram.size}")
        if (witnessVersion == 0 && witnessProgram.size != WITNESS_PROGRAM_LENGTH_PKH)
            throw AddressFormatException.InvalidDataLength(
                "Invalid length for address version 0: ${witnessProgram.size}"
            )
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val witnessVersion: Int
        get() = (bytes[0] and 0xff.toByte()).toInt()

    val witnessProgram: ByteArray
        get() = convertBits(bytes, 1, bytes.size - 1, 5, 8, false)

    override val hash: ByteArray
        get() = witnessProgram

    override val outputScriptType: ScriptType
        get() {
            require(witnessVersion == 0)
            if (witnessProgram.size == WITNESS_PROGRAM_LENGTH_PKH)
                return ScriptType.P2WPKH
            throw IllegalStateException("Cannot happen.")
        }


    override fun toString(): String = toBech32()

    @Suppress("MemberVisibilityCanBePrivate")
    fun toBech32(): String = Bech32.encode(params.segwitAddressHrp, bytes)

}
