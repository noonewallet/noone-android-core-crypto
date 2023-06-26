package io.noone.androidcore.hd

import io.noone.androidcore.ECKey
import io.noone.androidcore.exceptions.HDDerivationException
import io.noone.androidcore.utils.currentTimeSeconds
import io.noone.androidcore.utils.hex
import io.noone.androidcore.utils.hmacSHA512
import java.math.BigInteger
import java.util.*

class DeterministicSeed(
    var seedBytes: ByteArray,
    private val creationTimeSeconds: Long = currentTimeSeconds
) {

    constructor(entropy: Entropy) : this(entropy.seedBytes, entropy.creationTimeSeconds)

    override fun toString(): String = toString(false)

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val other = o as DeterministicSeed?
        return creationTimeSeconds == other?.creationTimeSeconds && seedBytes.equals(other.seedBytes)
    }

    override fun hashCode(): Int {
        var hash = 17
        hash = hash * 23 + creationTimeSeconds.hashCode()
        hash = hash * 23 + seedBytes.hashCode()
        return hash
    }

    fun toString(includePrivate: Boolean): String {
        return if (includePrivate) toHexString() else "unencrypted"
    }

    fun toHexString(): String = seedBytes.hex

    fun createMasterPrivateKey() = createMasterPrivateKey(seedBytes)

    fun getKeyFromSeed(path: String): DeterministicKey {
        var key = createMasterPrivateKey()
        for (chunk in path.split( '/')) {
            var hardened = false
            var indexText = chunk

            if (chunk.contains("'") || chunk.contains("H")) {
                hardened = true
                indexText = indexText.dropLast(1)
            }

            val index = indexText.toInt()

            key = key.deriveChildKey(ChildNumber(index, hardened))
        }
        return key
    }

    companion object {

        @Throws(HDDerivationException::class)
        fun createMasterPrivateKey(seed: ByteArray): DeterministicKey {
            require(seed.size > 8) { "Seed is too short and could be brute forced" }
            val i = seed.hmacSHA512("Bitcoin seed".toByteArray())
            // Use Il as master secret key, and Ir as master chain code.
            require(i.size == 64) { i.size }
            val il = i.copyOfRange(0, 32)
            val ir = i.copyOfRange(32, 64)
            Arrays.fill(i, 0.toByte())
            val masterPrivKey = createMasterPrivKeyFromBytes(il, ir)
            Arrays.fill(il, 0.toByte())
            Arrays.fill(ir, 0.toByte())
            masterPrivKey.creationTimeSeconds = currentTimeSeconds
            return masterPrivKey
        }

        /**
         * @throws HDDerivationException if privKeyBytes is invalid (not between 0 and n inclusive).
         */
        @Throws(HDDerivationException::class)
        private fun createMasterPrivKeyFromBytes(
            privKeyBytes: ByteArray,
            chainCode: ByteArray,
            childNumberPath: List<ChildNumber> = listOf()
        ): DeterministicKey {
            val priv = BigInteger(1, privKeyBytes)
            assertNonZero(priv, "Generated master key is invalid.")
            assertLessThanN(priv, "Generated master key is invalid.")
            return DeterministicKey(childNumberPath, chainCode, priv, null)
        }

        private fun assertNonZero(integer: BigInteger, errorMessage: String) {
            if (integer == BigInteger.ZERO)
                throw HDDerivationException(errorMessage)
        }

        private fun assertLessThanN(integer: BigInteger, errorMessage: String) {
            if (integer > ECKey.CURVE.n)
                throw HDDerivationException(errorMessage)
        }
    }
}
