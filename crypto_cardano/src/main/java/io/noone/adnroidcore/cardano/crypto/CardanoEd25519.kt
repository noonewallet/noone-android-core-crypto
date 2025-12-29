package io.noone.adnroidcore.cardano.crypto

import io.noone.adnroidcore.cardano.crypto.eddsa.EdDSAEngine
import io.noone.adnroidcore.cardano.crypto.eddsa.EdDSAPrivateKey
import io.noone.adnroidcore.cardano.crypto.eddsa.spec.EdDSANamedCurveTable
import io.noone.adnroidcore.cardano.crypto.eddsa.spec.EdDSAPrivateKeySpec
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter
import java.security.MessageDigest

/**
 * Pure Java/Kotlin implementation of Cardano Ed25519 cryptography.
 * Replaces native C++ code for cross-platform compatibility.
 *
 * Cardano uses BIP32-Ed25519 with extended keys (64 bytes) where:
 * - First 32 bytes: clamped scalar (already processed, no SHA-512 hashing)
 * - Second 32 bytes: nonce/prefix for signing
 */
object CardanoEd25519 {

    private val ED25519_SPEC = EdDSANamedCurveTable.getByName("Ed25519")

    /**
     * Derive public key from Cardano extended private key.
     *
     * @param privateKey 64-byte extended private key (scalar || nonce)
     * @return 32-byte public key
     */
    fun publicKey(privateKey: ByteArray): ByteArray {
        require(privateKey.size == 64) { "Private key must be 64 bytes, got ${privateKey.size}" }

        // Use clamp=false because Cardano keys are already clamped
        val keySpec = EdDSAPrivateKeySpec(ED25519_SPEC, privateKey, false)
        val key = EdDSAPrivateKey(keySpec)
        return key.abyte
    }

    /**
     * Sign a message using Cardano extended private key.
     *
     * @param message message to sign
     * @param privateKey 64-byte extended private key
     * @return 64-byte signature
     */
    fun sign(message: ByteArray, privateKey: ByteArray): ByteArray {
        require(privateKey.size == 64) { "Private key must be 64 bytes, got ${privateKey.size}" }

        // Use clamp=false because Cardano keys are already clamped
        val keySpec = EdDSAPrivateKeySpec(ED25519_SPEC, privateKey, false)
        val key = EdDSAPrivateKey(keySpec)

        val engine = EdDSAEngine(MessageDigest.getInstance("SHA-512"))
        engine.initSign(key)
        engine.update(message, 0, message.size)
        return engine.sign()
    }

    /**
     * HMAC-SHA512
     */
    fun hmacSha512(key: ByteArray, message: ByteArray): ByteArray {
        val hmac = HMac(SHA512Digest())
        hmac.init(KeyParameter(key))
        hmac.update(message, 0, message.size)
        val result = ByteArray(64)
        hmac.doFinal(result, 0)
        return result
    }

    /**
     * Derive child key using Cardano/Icarus derivation scheme (V2).
     *
     * @param parentKey 64-byte parent private key
     * @param chainCode 32-byte chain code
     * @param index child index
     * @param hardened true for hardened derivation
     * @return 96-byte result: derived key (64 bytes) + new chain code (32 bytes)
     */
    fun deriveChildKey(
        parentKey: ByteArray,
        chainCode: ByteArray,
        index: Int,
        hardened: Boolean
    ): ByteArray {
        require(parentKey.size == 64) { "Parent key must be 64 bytes" }
        require(chainCode.size == 32) { "Chain code must be 32 bytes" }

        val indexBytes = encodeIndex(index, hardened)

        // Compute Z
        val zData = if (hardened) {
            byteArrayOf(0x00) + parentKey + indexBytes
        } else {
            val publicKey = publicKey(parentKey)
            byteArrayOf(0x02) + publicKey + indexBytes
        }
        val z = hmacSha512(chainCode, zData)

        // Compute derived key
        val derivedKey = ByteArray(64)

        // kL' = 8 * zL + kL (using add_left logic from C++)
        addLeftV2(derivedKey, z, parentKey)

        // kR' = zR + kR (using add_right logic from C++)
        addRightV2(derivedKey, z, parentKey)

        // Compute new chain code
        val ccData = if (hardened) {
            byteArrayOf(0x01) + parentKey + indexBytes
        } else {
            val publicKey = publicKey(parentKey)
            byteArrayOf(0x03) + publicKey + indexBytes
        }
        val cc = hmacSha512(chainCode, ccData)
        val newChainCode = cc.copyOfRange(32, 64)

        return derivedKey + newChainCode
    }

    private fun encodeIndex(index: Int, hardened: Boolean): ByteArray {
        val value = if (hardened) (index or 0x80000000.toInt()) else index
        // Little-endian encoding (V2 scheme)
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    /**
     * Multiply by 8 (V2 scheme) - processes only first 28 bytes
     * Returns 32-byte array (last 3 bytes are zero)
     */
    private fun multiply8V2(src: ByteArray): ByteArray {
        val dst = ByteArray(32) // Initialize with zeros
        var prevAcc = 0
        for (i in 0 until 28) {
            val current = src[i].toInt() and 0xFF
            dst[i] = (((current shl 3) + (prevAcc and 0x07)) and 0xFF).toByte()
            prevAcc = current shr 5
        }
        dst[28] = ((src[27].toInt() and 0xFF) shr 5).toByte()
        // dst[29], dst[30], dst[31] remain zero
        return dst
    }

    /**
     * Add 256-bit numbers with carry (V2 scheme)
     */
    private fun add256BitsV2(src1: ByteArray, src1Off: Int, src2: ByteArray, src2Off: Int, dst: ByteArray, dstOff: Int) {
        var carry = 0
        for (i in 0 until 32) {
            val a = src1[src1Off + i].toInt() and 0xFF
            val b = src2[src2Off + i].toInt() and 0xFF
            val r = a + b + carry
            dst[dstOff + i] = (r and 0xFF).toByte()
            carry = if (r >= 0x100) 1 else 0
        }
    }

    /**
     * Scalar addition without overflow (for V2 derivation)
     */
    private fun scalarAddNoOverflow(sk1: ByteArray, sk2: ByteArray, res: ByteArray) {
        var r = 0
        for (i in 0 until 32) {
            r = (sk1[i].toInt() and 0xFF) + (sk2[i].toInt() and 0xFF) + r
            res[i] = (r and 0xFF).toByte()
            r = r shr 8
        }
    }

    private fun addLeftV2(derivedKey: ByteArray, z: ByteArray, parentKey: ByteArray) {
        val zl8 = multiply8V2(z)
        scalarAddNoOverflow(zl8, parentKey, derivedKey)
    }

    private fun addRightV2(derivedKey: ByteArray, z: ByteArray, parentKey: ByteArray) {
        add256BitsV2(z, 32, parentKey, 32, derivedKey, 32)
    }
}
