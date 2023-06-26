package io.noone.androidcore.hd

import io.noone.androidcore.exceptions.MnemonicException
import io.noone.androidcore.utils.currentTimeSeconds
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

class Entropy  {

    companion object {
        const val DEFAULT_SEED_ENTROPY_BITS: Int = 128
        private const val MAX_SEED_ENTROPY_BITS = 512

        private fun getEntropy(random: SecureRandom, bits: Int): ByteArray {
            require(bits <= MAX_SEED_ENTROPY_BITS) { "entropy size too large" }

            val seed = ByteArray(bits / 8)
            random.nextBytes(seed)
            return seed
        }
    }

    val mnemonicCode: List<String>
    val seedBytes: ByteArray
    var creationTimeSeconds: Long = currentTimeSeconds
    private set

    val mnemonicAsBytes: ByteArray
        get() = (mnemonicCode.joinToString(separator = " ") { it }).toByteArray(StandardCharsets.UTF_8)

    val entropyBytes: ByteArray
        @Throws(MnemonicException::class)
        get() = MnemonicCode().toEntropy(mnemonicCode, true)


    constructor(
        mnemonicCode: List<String>,
        passphrase: String = "",
        creationTimeSeconds: Long = 1409478661L
    ) {
        this.mnemonicCode = mnemonicCode
        this.seedBytes = MnemonicCode.toSeed(mnemonicCode, passphrase)
        this.creationTimeSeconds = creationTimeSeconds
    }

    constructor(
        random: SecureRandom = SecureRandom(),
        bits: Int = DEFAULT_SEED_ENTROPY_BITS,
        passphrase: String = ""
    ) {
        val entropy: ByteArray = getEntropy(random, bits)

        require(entropy.size % 4 == 0) {
            "entropy size in bits not divisible by 32"
        }
        require(entropy.size * 8 >= DEFAULT_SEED_ENTROPY_BITS) {
            "entropy size too small"
        }

        this.mnemonicCode = MnemonicCode().toMnemonic(entropy)
        this.seedBytes = MnemonicCode.toSeed(mnemonicCode, passphrase)
        this.creationTimeSeconds = currentTimeSeconds
    }

    /**
     * Check if our mnemonic is a valid mnemonic phrase for our word list.
     */
    @Throws(MnemonicException::class)
    fun check(ignoreChecksum: Boolean) {
        MnemonicCode().check(mnemonicCode, ignoreChecksum)
    }

}