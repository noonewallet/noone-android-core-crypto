package io.noone.androidcore.btclike.opcodes

object OpCodes {

    const val FALSE = 0x00.toByte()
    const val DUP = 0x76.toByte()
    const val HASH160 = 0xA9.toByte()
    const val EQUAL = 0x87.toByte()
    const val EQUALVERIFY = 0x88.toByte()
    const val CHECKSIG = 0xAC.toByte()

    const val OP_0 = 0x00 // push empty vector
    const val OP_1NEGATE = 0x4f
    const val OP_1 = 0x51

    fun encodeToOpN(value: Int): Int {
        require(value >= -1 && value <= 16) {
            "encodeToOpN called for $value which we cannot encode in an opcode."
        }
        return when (value) {
            0 -> OP_0
            -1 -> OP_1NEGATE
            else -> value - 1 + OP_1
        }
    }
}
