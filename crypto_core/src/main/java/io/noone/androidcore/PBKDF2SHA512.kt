package io.noone.androidcore

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.ceil
import kotlin.math.pow

/**
 *
 * This is a clean-room implementation of PBKDF2 using RFC 2898 as a reference.
 *
 * [RFC 2898](http://tools.ietf.org/html/rfc2898#section-5.2)
 *
 */
object PBKDF2SHA512 {

    fun derive(
        password: ByteArray,
        salt: ByteArray,
        cycles: Int,
        keyLen: Int
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()
        try {
            val hLen = 20
            require(keyLen <= (2.0.pow(32) - 1) * hLen) { "derived key too long" }
            val l = ceil(keyLen.toDouble() / hLen.toDouble()).toInt()
            for (i in 1..l) {
                outputStream.write(F(password, salt, cycles, i))
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        val baDerived = ByteArray(keyLen)
        System.arraycopy(outputStream.toByteArray(), 0, baDerived, 0, baDerived.size)
        return baDerived
    }

    @Suppress("FunctionName")
    @Throws(Exception::class)
    private fun F(
        P: ByteArray,
        S: ByteArray,
        c: Int,
        i: Int
    ): ByteArray? {
        var uLast: ByteArray? = null
        var uXor: ByteArray? = null
        val key = SecretKeySpec(P, "HmacSHA512")
        val mac = Mac.getInstance(key.algorithm)
        mac.init(key)
        for (j in 0 until c) {
            if (j == 0) {
                val baI = i.asByteArray()
                val baU = ByteArray(S.size + baI.size)
                System.arraycopy(S, 0, baU, 0, S.size)
                System.arraycopy(baI, 0, baU, S.size, baI.size)
                uXor = mac.doFinal(baU)
                uLast = uXor
                mac.reset()
            } else {
                val baU = mac.doFinal(uLast)
                mac.reset()
                for (k in uXor!!.indices) {
                    uXor[k] = (uXor[k].toInt() xor baU[k].toInt()).toByte()
                }
                uLast = baU
            }
        }
        return uXor
    }

    private fun Int.asByteArray(): ByteArray {
        val bb = ByteBuffer.allocate(4)
        bb.order(ByteOrder.BIG_ENDIAN)
        bb.putInt(this)
        return bb.array()
    }
}