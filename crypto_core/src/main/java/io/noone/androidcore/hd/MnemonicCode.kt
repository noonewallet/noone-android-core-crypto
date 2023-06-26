package io.noone.androidcore.hd

import io.noone.androidcore.PBKDF2SHA512
import io.noone.androidcore.exceptions.MnemonicException
import io.noone.androidcore.exceptions.MnemonicException.*
import io.noone.androidcore.utils.hex
import io.noone.androidcore.utils.sha256
import java.io.*
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

class MnemonicCode @JvmOverloads constructor(
    wordstream: InputStream? = openDefaultWords(),
    wordListDigest: String? = BIP39_ENGLISH_SHA256
) {
    private val wordList: ArrayList<String>

    /**
     * Creates an MnemonicCode object, initializing with words read from the supplied input stream.  If a wordListDigest
     * is supplied the digest of the words will be checked.
     */
    init {
        val br = BufferedReader(InputStreamReader(wordstream, StandardCharsets.UTF_8))
        wordList = ArrayList(WORDLIST_SIZE)
        val md = MessageDigest.getInstance("SHA-256")
        var word: String
        while (br.readLine().also { word = it ?: "" } != null) {
            md.update(word.toByteArray())
            wordList.add(word)
        }
        br.close()
        require(wordList.size == WORDLIST_SIZE) { "input stream did not contain 2048 words" }

        // If a wordListDigest is supplied check to make sure it matches.
        if (wordListDigest != null) {
            val digest = md.digest()
            val hexdigest = digest.hex
            require(hexdigest == wordListDigest) { "wordlist digest mismatch" }
        }
    }

    /**
     * Gets the word list this code uses.
     */
    fun getWordList(): List<String> = wordList

    /**
     * Convert mnemonic word list to original entropy value.
     */
    @Throws(MnemonicException::class)
    fun toEntropy(
        words: List<String>,
        ignoreChecksum: Boolean
    ): ByteArray {
        if (words.size % 3 > 0) throw MnemonicLengthException("Word list size must be multiple of three words.")
        if (words.isEmpty()) throw MnemonicLengthException("Word list is empty.")

        // Look up all the words in the list and construct the
        // concatenation of the original entropy and the checksum.
        val concatLenBits = words.size * WORDLIST_BITS_SIZE
        val concatBits = BooleanArray(concatLenBits)
        for ((wordindex, word) in words.withIndex()) {
            // Find the words index in the wordlist.
            val ndx = Collections.binarySearch(wordList, word)
            if (ndx < 0) throw MnemonicWordException(word)

            // Set the next 11 bits to the value of the index.
            for (ii in 0 until WORDLIST_BITS_SIZE) concatBits[wordindex * WORDLIST_BITS_SIZE + ii] =
                ndx and (1 shl 10 - ii) != 0
        }
        val checksumLengthBits = concatLenBits / 33
        val entropyLengthBits = concatLenBits - checksumLengthBits

        // Extract original entropy as bytes.
        val entropy = ByteArray(entropyLengthBits / 8)
        for (ii in entropy.indices) for (jj in 0..7) if (concatBits[ii * 8 + jj]) entropy[ii] =
            (entropy[ii].toInt() or (1 shl 7 - jj)).toByte()

        // Take the digest of the entropy.
        val hash = entropy.sha256
        val hashBits = bytesToBits(hash)
        if (ignoreChecksum) return entropy
        // Check all the checksum bits.
        for (i in 0 until checksumLengthBits) {
            if (concatBits[entropyLengthBits + i] != hashBits[i]) {
                throw MnemonicChecksumException()
            }
        }
        return entropy
    }

    /**
     * Convert entropy data to mnemonic word list.
     */
    @Throws(MnemonicLengthException::class)
    fun toMnemonic(entropy: ByteArray): List<String> {
        if (entropy.size % 4 > 0) throw MnemonicLengthException("Entropy length not multiple of 32 bits.")
        if (entropy.isEmpty()) throw MnemonicLengthException("Entropy is empty.")

        // We take initial entropy of ENT bits and compute its
        // checksum by taking first ENT / 32 bits of its SHA256 hash.
        val hash = entropy.sha256
        val hashBits = bytesToBits(hash)
        val entropyBits = bytesToBits(entropy)
        val checksumLengthBits = entropyBits.size / 32

        // We append these bits to the end of the initial entropy.
        val concatBits = BooleanArray(entropyBits.size + checksumLengthBits)
        System.arraycopy(entropyBits, 0, concatBits, 0, entropyBits.size)
        System.arraycopy(hashBits, 0, concatBits, entropyBits.size, checksumLengthBits)

        // Next we take these concatenated bits and split them into
        // groups of 11 bits. Each group encodes number from 0-2047
        // which is a position in a wordlist.
        val words = ArrayList<String>()
        val nwords = concatBits.size / 11
        for (i in 0 until nwords) {
            var index = 0
            for (j in 0 until WORDLIST_BITS_SIZE) {
                index = index shl 1
                if (concatBits[i * WORDLIST_BITS_SIZE + j]) index = index or 0x1
            }
            words.add(wordList[index])
        }
        return words
    }

    /**
     * Check to see if a mnemonic word list is valid.
     */
    @Throws(MnemonicException::class)
    fun check(
        words: List<String>,
        ignoreChecksum: Boolean
    ) {
        toEntropy(words, ignoreChecksum)
    }

    companion object {
        private const val BIP39_ENGLISH_RESOURCE_NAME = "/assets/bip39-wordlist-en"
        private const val BIP39_ENGLISH_SHA256 =
            "ad90bf3beb7b0eb7e5acd74727dc0da96e0a280a258354e7293fb7e211ac03db"
        private const val PBKDF2_ROUNDS = 2048
        private const val WORDLIST_SIZE = 2048
        const val WORDLIST_BITS_SIZE = 11

        @Throws(IOException::class)
        private fun openDefaultWords(): InputStream {
            return MnemonicCode::class.java.getResourceAsStream(BIP39_ENGLISH_RESOURCE_NAME)
                ?: throw FileNotFoundException(BIP39_ENGLISH_RESOURCE_NAME)
        }

        /**
         * Convert mnemonic word list to seed.
         */
        fun toSeed(words: List<String>, passphrase: String): ByteArray {
            val pass = words.joinToString(separator = " ") { it }
            val salt = "mnemonic$passphrase"
            return PBKDF2SHA512.derive(pass, salt, PBKDF2_ROUNDS, 64)
        }

        private fun bytesToBits(data: ByteArray): BooleanArray {
            val bits = BooleanArray(data.size * 8)
            for (i in data.indices){
                for (j in 0..7) {
                    bits[i * 8 + j] = data[i].toInt() and (1 shl (7 - j)) != 0
                }
            }
            return bits
        }

        private fun getEntropyLengthFromMnemonicSize(mnemonicSize: Int): Int {
            val checksumLengthBits = mnemonicSize * WORDLIST_BITS_SIZE / 33
            return mnemonicSize * WORDLIST_BITS_SIZE - checksumLengthBits
        }
    }
}