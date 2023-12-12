package io.noone.androidcore.hd

import io.noone.androidcore.ECKey
import io.noone.androidcore.LazyECPoint
import io.noone.androidcore.exceptions.HDDerivationException
import io.noone.androidcore.utils.hex
import io.noone.androidcore.utils.sha256hash160
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*

class DeterministicKey : ECKey {

    companion object {

        /** Convert to a string path, starting with "M/"  */
        fun formatPath(path: List<ChildNumber>): String {
            return path.joinToString(prefix = "M/", separator = "/") { it.toString() }
        }

        fun deriveChildKey(
            parent: DeterministicKey,
            childNumber: Int
        ): DeterministicKey = parent.deriveChildKey(ChildNumber(childNumber))
    }

    private var parent: DeterministicKey? = null

    val path: List<ChildNumber>
    val depth: Int

    var parentFingerprint: Int = 0
        private set // 0 if this key is root node of key hierarchy

    /** 32 bytes  */
    val chainCode: ByteArray

    /**
     * Returns the path of this key as a human readable string starting with M to indicate the master key.
     */
    val pathAsString: String
        get() = formatPath(path)

    val childNumber: ChildNumber
        get() = if (path.isEmpty()) ChildNumber.ZERO else path[path.size - 1]

    val identifier: ByteArray
        get() = pubKey.sha256hash160

    /** Returns the first 32 bits of the result of [identifier].  */
    val fingerprint: Int
        get() = ByteBuffer.wrap(identifier.copyOfRange(0, 4)).int

    val privKeyBytes33: ByteArray
        get() {
            val bytes33 = ByteArray(33)
            System.arraycopy(
                privKeyBytes,
                0,
                bytes33,
                33 - privKeyBytes.size,
                privKeyBytes.size
            )
            return bytes33
        }

    /** Constructs a key from its components. This is not normally something you should use.  */
    constructor(
        childNumberPath: List<ChildNumber>,
        chainCode: ByteArray,
        publicAsPoint: LazyECPoint,
        priv: BigInteger?,
        parent: DeterministicKey?
    ) : super(
        priv,
        compressPoint(publicAsPoint)
    ) {
        require(chainCode.size == 32)
        this.parent = parent
        this.path = childNumberPath
        this.chainCode = chainCode.copyOf(chainCode.size)
        this.depth = if (parent == null) 0 else parent.depth + 1
        this.parentFingerprint = parent?.fingerprint ?: 0
    }

    /** Constructs a key from its components. This is not normally something you should use.  */
    constructor(
        childNumberPath: List<ChildNumber>,
        chainCode: ByteArray,
        priv: BigInteger,
        parent: DeterministicKey?
    ) : super(
        priv,
        compressPoint(publicPointFromPrivate(priv))
    ) {
        require(chainCode.size == 32)
        this.parent = parent
        this.path = checkNotNull(childNumberPath)
        this.chainCode = chainCode.copyOf(chainCode.size)
        this.depth = if (parent == null) 0 else parent.depth + 1
        this.parentFingerprint = parent?.fingerprint ?: 0
    }

    fun dropParent(): DeterministicKey {
        val key = DeterministicKey(path, chainCode, pub, priv, null)
        key.parentFingerprint = parentFingerprint
        return key
    }

    override val isPubKeyOnly: Boolean
        get() = super.isPubKeyOnly && (parent?.isPubKeyOnly ?: true)

    override fun hasPrivKey(): Boolean {
        return findParentWithPrivKey() != null
    }

    private fun findParentWithPrivKey(): DeterministicKey? {
        var cursor: DeterministicKey? = this
        while (cursor != null) {
            if (cursor.priv != null) break
            cursor = cursor.parent
        }
        return cursor
    }

    private fun findOrDerivePrivateKey(): BigInteger? {
        val cursor = findParentWithPrivKey() ?: return null
        return derivePrivateKeyDownwards(cursor, cursor.priv!!.toByteArray())
    }

    private fun derivePrivateKeyDownwards(
        cursor: DeterministicKey,
        parentalPrivateKeyBytes: ByteArray
    ): BigInteger {
        var downCursor = DeterministicKey(
            cursor.path, cursor.chainCode,
            cursor.pub, BigInteger(1, parentalPrivateKeyBytes), cursor.parent
        )
        val path = this.path.subList(cursor.path.size, this.path.size)
        for (num in path) {
            downCursor = downCursor.deriveChildKey(num)
        }
        return downCursor.priv!!
    }

    override val privKey: BigInteger
        get() {
            val key = findOrDerivePrivateKey()
            require(key != null) { "Private key bytes not available" }
            return key
        }

    public override var creationTimeSeconds: Long = 0L
        set(value) {
            check(parent == null) { "Creation time can only be set on root keys." }
            field = value
            super.creationTimeSeconds = value
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val other = other as DeterministicKey?
        return (super.equals(other)
                && this.chainCode.contentEquals(other.chainCode)
                && this.path == other.path)
    }

    override fun hashCode(): Int {
        var hash = 17
        hash = hash * 23 + super.hashCode()
        hash = hash * 23 + chainCode.hashCode()
        hash = hash * 23 + path.hashCode()
        return hash
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
            .append("pub : ")
            .appendLine(pub.encoded.hex)
            .append("chainCode : ")
            .appendLine(chainCode.hex)
            .append("path : ")
            .appendLine(pathAsString)

        if (creationTimeSeconds > 0) {
            stringBuilder
                .append("creationTimeSeconds : ")
                .appendLine(creationTimeSeconds)
        }
        stringBuilder
            .append("isPubKeyOnly : ")
            .appendLine(isPubKeyOnly)

        return stringBuilder.toString()
    }


    /**
     * @throws HDDerivationException if private derivation is attempted for a public-only parent key, or
     * if the resulting derived key is invalid (eg. private key == 0).
     */
    @Throws(HDDerivationException::class)
    fun deriveChildKey(childNumber: ChildNumber): DeterministicKey {
        return if (this.hasPrivKey()) {
            val rawKey = this.deriveChildKeyBytesFromPrivate(childNumber)
            DeterministicKey(
                this.path + childNumber,
                rawKey.chainCode,
                BigInteger(1, rawKey.keyBytes),
                this
            )
        } else {
            val rawKey = this.deriveChildKeyBytesFromPublic(childNumber, PublicDeriveMode.NORMAL)
            DeterministicKey(
                this.path + childNumber,
                rawKey.chainCode,
                LazyECPoint(CURVE.curve, rawKey.keyBytes),
                null,
                this
            )
        }
    }

    fun deriveChildKey(
        i: Int,
        hardened: Boolean = false
    ): DeterministicKey = deriveChildKey(ChildNumber(i, hardened))


}
