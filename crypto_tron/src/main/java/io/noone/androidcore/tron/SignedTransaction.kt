package io.noone.androidcore.tron

class SignedTransaction(
    val transaction: Transaction,
    val signature: ByteArray
): ObjectBlock() {

    companion object {
        val SIGNATURE_BLOCK_TYPE = byteArrayOf(0x12)
    }

    init {
        require(signature.size == 65)
        add(transaction)
        add(BytesBlock(SIGNATURE_BLOCK_TYPE, signature))
    }

    val raw: ByteArray
        get() = encodeBlocksOnly()

}