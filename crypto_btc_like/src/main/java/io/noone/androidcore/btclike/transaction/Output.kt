package io.noone.androidcore.btclike.transaction

import io.noone.androidcore.btclike.ErrorMessages
import io.noone.androidcore.btclike.addresses.SegwitAddress
import io.noone.androidcore.btclike.networks.NetworkParameters
import io.noone.androidcore.types.ULong
import io.noone.androidcore.types.VarInt
import io.noone.androidcore.utils.Base58
import io.noone.androidcore.utils.ByteBuffer
import io.noone.androidcore.utils.hex

class Output(
    val satoshi: Long,
    private val destination: String,
    private val type: OutputType,
    private val scriptPubKeyProducer: ScriptPubKeyProducer,
    private val networkParameters: NetworkParameters,
) {

    private val decodedAddress: ByteArray
    private val addressType: ScriptType

    private val lockingScript: ByteArray
        get() = scriptPubKeyProducer.produceScript(decodedAddress, addressType)

    init {
        validateOutputData(satoshi, destination)
        this.addressType = decodeAddressType(destination)
        this.decodedAddress = decodeAndValidateAddress(destination, addressType)
    }

    fun serializeForSigHash(): ByteArray {
        val lockingScript = lockingScript
        return ByteBuffer().apply {
            append(*ULong.of(satoshi).asLitEndBytes())
            append(*VarInt.of(lockingScript.size.toLong()).asLitEndBytes())
            append(*lockingScript)
        }
            .bytes
    }

    fun fillTransaction(transaction: Transaction) {
        transaction.addHeader(Transaction.TxPart.OUTPUT)
        transaction.addData(Transaction.TxPart.AMOUNT, ULong.of(satoshi).toString())
        transaction.addData(
            Transaction.TxPart.LOCK_LENGTH,
            VarInt.of(lockingScript.size.toLong()).toString()
        )
        transaction.addData(Transaction.TxPart.LOCK, lockingScript.hex)
    }

    override fun toString(): String {
        return "$destination $satoshi"
    }

    fun getDestination(): String = destination

    private fun validateOutputData(satoshi: Long, destination: String) {
        validateDestinationAddress(destination)
        validateAmount(satoshi);
    }

    private fun validateAmount(satoshi: Long) {
        require(satoshi > 0) { ErrorMessages.OUTPUT_AMOUNT_NOT_POSITIVE }
    }

    private fun validateDestinationAddress(destination: String) {
        require(destination.isNotBlank()) { ErrorMessages.OUTPUT_ADDRESS_EMPTY }

        require(
            networkParameters.segwitAddressHrp?.let { destination.startsWith(it) } ?: false ||
                    Base58.decodeChecked(destination)[0] == networkParameters.addressHeader.toByte() ||
                    Base58.decodeChecked(destination)[0] == networkParameters.p2SHHeader.toByte()
        ) { ErrorMessages.OUTPUT_ADDRESS_WRONG_PREFIX }
    }

    private fun decodeAddressType(address: String): ScriptType {
        return ScriptType.fromAddressPrefix(address, networkParameters)
    }


    private fun decodeAndValidateAddress(address: String, addressType: ScriptType): ByteArray {
        return when (addressType) {
            ScriptType.P2WPKH -> {
                SegwitAddress.fromBech32(networkParameters, address).witnessProgram
            }
            ScriptType.P2PKH, ScriptType.P2SH -> {
                require(destination.isBase58()) { ErrorMessages.OUTPUT_ADDRESS_NOT_BASE_58 }
                Base58.decodeChecked(address).drop(1).toByteArray()
            }
            else ->
                throw IllegalArgumentException(ErrorMessages.OUTPUT_ADDRESS_WRONG_PREFIX)
        }
    }

    private fun String.isBase58(): Boolean {
        return this.matches("[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]+".toRegex())
    }

}