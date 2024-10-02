package io.noone.androidcore.btclike.transaction

import io.noone.androidcore.btclike.ErrorMessages
import io.noone.androidcore.btclike.PrivateKey
import io.noone.androidcore.btclike.networks.NetworkParameters
import io.noone.androidcore.btclike.opcodes.OpSize
import io.noone.androidcore.types.UInt
import io.noone.androidcore.types.VarInt
import io.noone.androidcore.utils.hex
import io.noone.androidcore.utils.safeToByteArray

class Input(
    private val hash: ByteArray,
    val index: Int,
    val lock: String,
    val satoshi: Long,
    val privateKey: PrivateKey,
    private val witnessProducer: WitnessProducer,
    private val scriptSigProducer: ScriptSigProducer,
    val sequence: UInt,
    private val networkParameters: NetworkParameters,
) {

    val transactionHashBytesLitEnd: ByteArray
    val isSegWit: Boolean

    val transactionHash: ByteArray
        get() = hash

    private val outHash: ByteArray
        get() {
            return when (networkParameters.hashType) {
                HashType.DEFAULT -> transactionHash
                HashType.REVERSED -> transactionHashBytesLitEnd
            }
        }

    init {
        validateInputData(hash, index, lock, satoshi)
        transactionHashBytesLitEnd = hash.reversedArray()
        isSegWit = ScriptType.forLock(lock).isSegWit()
    }

    fun fillTransaction(sigHash: ByteArray, transaction: Transaction, scriptType: ScriptType) {
        transaction.addHeader(Transaction.TxPart.INPUT)

        val unlocking = scriptSigProducer.produceScriptSig(sigHash, privateKey, scriptType)
        transaction.addData(Transaction.TxPart.TRANSACTION_OUT, outHash.hex)
        transaction.addData(Transaction.TxPart.TOUT_INDEX, UInt.of(index).toString())
        transaction.addData(
            Transaction.TxPart.UNLOCK_LENGTH,
            VarInt.of(unlocking.size.toLong()).asLitEndBytes().hex
        )
        transaction.addData(Transaction.TxPart.UNLOCK, unlocking.hex)
        transaction.addData(Transaction.TxPart.SEQUENCE, sequence.toString())
    }

    fun getWitness(sigHash: ByteArray): ByteArray =
        if (isSegWit)
            witnessProducer.produceWitness(sigHash, privateKey)
        else
            byteArrayOf(0x00.toByte())

    override fun toString(): String = "$hash\n$index\n$lock\n$satoshi"

    private fun validateInputData(hash: ByteArray, index: Int, lock: String, satoshi: Long) {
        validateTransactionId(hash)
        validateOutputIndex(index)
        validateLockingScript(lock)
        validateAmount(satoshi)
    }

    private fun validateTransactionId(hash: ByteArray) {
        require(hash.size == 32) { ErrorMessages.INPUT_TRANSACTION_NOT_64_HEX }
    }

    private fun validateOutputIndex(index: Int) {
        require(index >= 0) { ErrorMessages.INPUT_INDEX_NEGATIVE }
    }

    private fun validateLockingScript(lock: String) {
        require(lock.isNotBlank()) { ErrorMessages.INPUT_LOCK_EMPTY }
        require(lock.isHex()) { ErrorMessages.INPUT_LOCK_NOT_HEX }
        val lockBytes = lock.safeToByteArray()
        when (ScriptType.forLock(lock)) {
            ScriptType.P2PKH -> {
                val pubKeyHashSize = OpSize.ofByte(lockBytes[2])
                require(pubKeyHashSize.toInt() == lockBytes.size - 5) {
                    String.format(ErrorMessages.INPUT_WRONG_PKH_SIZE, lock)
                }
            }
            ScriptType.P2SH -> {
                val pubKeyHashSize = OpSize.ofByte(lockBytes[1])
                require(pubKeyHashSize.toInt() == lockBytes.size - 3) {
                    String.format(ErrorMessages.INPUT_WRONG_RS_SIZE, lock)
                }
            }
            ScriptType.P2WPKH -> {
                val pubKeyHash = privateKey.key.pubKeyHash
                require(lockBytes[0] == 0x00.toByte()) {
                    String.format(ErrorMessages.INPUT_LOCK_WRONG_FORMAT, lock)
                }
                require(OpSize.ofByte(lockBytes[1]).toInt() == pubKeyHash.size) {
                    String.format(ErrorMessages.INPUT_WRONG_WPKH_SIZE, lock)
                }
                require(lockBytes.drop(2).toByteArray().contentEquals(pubKeyHash)) {
                    String.format(ErrorMessages.INPUT_LOCK_WRONG_FORMAT, lock)
                }
            }
            else -> throw IllegalArgumentException("Provided locking script is not P2PKH, P2WPKH or P2SH [$lock]")
        }
    }

    private fun validateAmount(satoshi: Long) {
        require(satoshi > 0) { ErrorMessages.INPUT_AMOUNT_NOT_POSITIVE }
    }

    private fun String.isHex(): Boolean = this.matches("\\p{XDigit}+".toRegex())
}