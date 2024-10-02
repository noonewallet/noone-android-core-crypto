package io.noone.androidcore.tron

import java.math.BigInteger

class TransactionBuilder(
    private val allowFeeLimit: Boolean = true
) {

    companion object {
        val BLOCK_TYPE_REF_BYTES = byteArrayOf(0x0a)
        val BLOCK_TYPE_REF_HASH = byteArrayOf(0x22)
        val BLOCK_TYPE_EXPIRATION_TIME = byteArrayOf(0x40)
        val BLOCK_TYPE_TIMESTAMP = byteArrayOf(0x70)
        val BLOCK_TYPE_FEE_LIMIT = byteArrayOf(0x90.toByte(), 0x01)
        val BLOCK_TYPE_MEMO = byteArrayOf(0x52)

        val TRANSFER_TX_SIZE = BigInteger.valueOf(264L)
        val TRC20_TRANSFER_TX_SIZE = BigInteger.valueOf(277L)
    }

    private val transaction = Transaction()

    var blockRef: ByteArray? = null
        private set
    var blockHash: ByteArray? = null
        private set
    var expirationTime: Long? = null
        private set
    var contract: Contract? = null
        private set
    var timestamp: Long? = null
        private set
    var energyLimit: BigInteger? = null
        private set

    var memo: String? = null
        private set

    fun setMemo(memo: String): TransactionBuilder {
        this.memo = memo
        return this
    }
    fun setRefBlockBytes(refBlockBytes: ByteArray): TransactionBuilder {
        require(refBlockBytes.size == 2)
        blockRef = refBlockBytes
        return this
    }

    fun setRefBlockHash(refBlockHash: ByteArray): TransactionBuilder {
        require(refBlockHash.size == 8)
        blockHash = refBlockHash
        return this
    }

    fun setExpirationTime(time: Long): TransactionBuilder {
        expirationTime = time
        return this
    }

    fun setContract(contractBlock: Contract): TransactionBuilder {
        contract = contractBlock
        return this
    }

    fun setTimestamp(time: Long): TransactionBuilder {
        timestamp = time
        return this
    }

    fun setEnergyLimit(limit: BigInteger): TransactionBuilder {
        require(allowFeeLimit)
        energyLimit = limit
        return this
    }

    fun build(): Transaction {
        checkNotNull(blockRef)
        checkNotNull(blockHash)
        checkNotNull(expirationTime)
        checkNotNull(contract)
        checkNotNull(timestamp)

        transaction.add(BytesBlock(BLOCK_TYPE_REF_BYTES, blockRef!!))
        transaction.add(BytesBlock(BLOCK_TYPE_REF_HASH, blockHash!!))
        transaction.add(VarIntBlock(BLOCK_TYPE_EXPIRATION_TIME, expirationTime!!.toBigInteger()))
        memo?.let { memo ->
            transaction.add(BytesBlock(BLOCK_TYPE_MEMO, memo.toByteArray(Charsets.UTF_8)))
        }
        transaction.add(contract!!)
        transaction.add(VarIntBlock(BLOCK_TYPE_TIMESTAMP, timestamp!!.toBigInteger()))
        if (allowFeeLimit) {
            checkNotNull(energyLimit)
            transaction.add(VarIntBlock(BLOCK_TYPE_FEE_LIMIT, energyLimit!!))
        }

        return transaction
    }

}