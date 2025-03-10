package io.noone.androidcore.utils

import io.noone.androidcore.ECKey
import io.noone.androidcore.hd.ChildNumber
import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.util.encoders.Base64
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.Arrays

const val HEX_PREFIX = "0x"
val ByteArray.hexWithPrefix: String
    get() = HEX_PREFIX + HexUtils.toHex(this)

val ByteArray.base64: String
    get() = Base64.toBase64String(this)

val String.base64: ByteArray
    get() = Base64.decode(this)

val ByteArray.hex: String
    get() = HexUtils.toHex(this)

val String.hex: ByteArray
    get() = HexUtils.toBytes(this)

fun String.safeToByteArray(): ByteArray = try {
    HexUtils.toBytes(this)
} catch (e: Exception) {
    byteArrayOf()
}

fun storeInt32BE(value: Int, bytes: ByteArray, offSet: Int) {
    bytes[offSet + 3] = value.toByte()
    bytes[offSet + 2] = (value ushr 8).toByte()
    bytes[offSet + 1] = (value ushr 16).toByte()
    bytes[offSet] = (value ushr 24).toByte()
}

val ByteArray.blake2b512: ByteArray
    get() {
        val digest = Blake2bDigest(512)
        digest.update(this, 0, this.size)
        val hash = ByteArray(64)
        digest.doFinal(hash, 0)
        return hash
    }

val ByteArray.blake2b256: ByteArray
    get() {
        val digest = Blake2bDigest(256)
        digest.update(this, 0, this.size)
        val hash = ByteArray(32)
        digest.doFinal(hash, 0)
        return hash
    }

fun blake2b256(vararg data: ByteArray): ByteArray {
    val digest = Blake2bDigest(256)
    data.forEach {
        digest.update(it, 0, it.size)
    }
    val hash = ByteArray(32)
    digest.doFinal(hash, 0)
    return hash
}

val ByteArray.blake2b224: ByteArray
    get() {
        val digest = Blake2bDigest(224)
        digest.update(this, 0, this.size)
        val hash = ByteArray(28)
        digest.doFinal(hash, 0)
        return hash
    }

val ByteArray.blake2b128: ByteArray
    get() {
        val digest = Blake2bDigest(128)
        digest.update(this, 0, this.size)
        val hash = ByteArray(16)
        digest.doFinal(hash, 0)
        return hash
    }

val ByteArray.keccak256: ByteArray
    get() {
        return with(Keccak.Digest256()) {
            update(this@keccak256)
            digest()
        }
    }

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * The regular {@link BigInteger#toByteArray()} includes the sign bit of the number and
 * might result in an extra byte addition. This method removes this extra byte.
 *
 * @param numBytes the desired size of the resulting byte array
 * @return numBytes byte long array.
 */
fun BigInteger.toByteArrayUnsigned(numBytes: Int): ByteArray {
    require(this.signum() >= 0) { "b must be positive or zero" }
    require(numBytes > 0) { "numBytes must be positive" }
    val src = this.toByteArray()
    val dest = ByteArray(numBytes)
    val isFirstByteOnlyForSign = src[0].toInt() == 0
    val length = if (isFirstByteOnlyForSign) src.size - 1 else src.size
    require(length <= numBytes) { "The given number does not fit in $numBytes" }
    val srcPos = if (isFirstByteOnlyForSign) 1 else 0
    val destPos = numBytes - length
    System.arraycopy(src, srcPos, dest, destPos, length)
    return dest
}


fun ByteArray.hmacSHA512(key: ByteArray): ByteArray {
    val digest = SHA512Digest()
    val hmacSha512 = HMac(digest)
    hmacSha512.init(KeyParameter(key))
    hmacSha512.reset()
    hmacSha512.update(this, 0, this.size)
    val out = ByteArray(64)
    hmacSha512.doFinal(out, 0)
    return out
}

val ByteArray.firstHalf: ByteArray
    get() {
        val half = ByteArray(this.size / 2)
        System.arraycopy(this, 0, half, 0, half.size)
        return half
    }

val ByteArray.lashHalf: ByteArray
    get() {
        val half = ByteArray(this.size / 2)
        System.arraycopy(this, this.size / 2, half, 0, half.size)
        return half
    }

/**
 * Calculates RIPEMD160(SHA256(input)).
 */
val ByteArray.sha256hash160: ByteArray
    get() {
        val sha256 = this.sha256
        val digest = RIPEMD160Digest()
        digest.update(sha256, 0, sha256.size)
        val out = ByteArray(20)
        digest.doFinal(out, 0)
        return out
    }

/**
 * Calculates SHA256(input).
 */
val ByteArray.sha256: ByteArray
    get() {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(this, 0, this.size)
        return digest.digest()
    }

/**
 * Calculates SHA512(input).
 */
val ByteArray.sha512: ByteArray
    get() {
        val digest = MessageDigest.getInstance("SHA-512")
        digest.update(this, 0, this.size)
        return digest.digest()
    }

/**
 * Calculates SHA256(input).
 */
val ByteArray.sha256sha256: ByteArray
    get() {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(this, 0, this.size)
        return digest.digest(digest.digest())
    }

fun ByteArray.sha256sha256(offset: Int = 0, length: Int = this.size): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(this, offset, length)
    return digest.digest(digest.digest())
}
/**
 * Encode string to hex Keccak 256.
 */
val String.keccak256: ByteArray
    get() {
        val dataBytes = this.toByteArray()
        val keccak256 = Keccak.Digest256()
        keccak256.reset()
        keccak256.update(dataBytes, 0, dataBytes.size)
        return keccak256.digest()
    }

val currentTimeSeconds: Long get() = System.currentTimeMillis() / 1000

val Long.as4byteArray: ByteArray
    get() {
        val bytes = Arrays.copyOfRange(ByteBuffer.allocate(8).putLong(this).array(), 4, 8)
        assert(bytes.size == 4) { bytes.size }
        return bytes
    }


internal fun ByteArray.toCompressed(): ByteArray {
    return ECKey.CURVE.curve.decodePoint(this).getEncoded(true)
}

/**
 * The path is a human-friendly representation of the deterministic path. For example:
 *
 * "44H / 0H / 0H / 1 / 1"
 *
 * Where a letter "H" means hardened key. Spaces are ignored.
 */
fun parsePath(path: String): List<ChildNumber> {
    val parsedNodes =
        path.replace("M", "").split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val nodes = ArrayList<ChildNumber>()

    for (n in parsedNodes) {
        var N = n.replace(" ".toRegex(), "")
        if (N.isEmpty()) continue
        val isHard = N.endsWith("H")
        if (isHard) N = N.substring(0, n.length - 1)
        val nodeNumber = Integer.parseInt(n)
        nodes.add(ChildNumber(nodeNumber, isHard))
    }

    return nodes
}

fun Short.toBytesLE(): ByteArray = ByteBufferUtils.littleEndian(2).putShort(this).array()

fun Int.toBytes(): ByteArray = ByteBuffer.allocate(4).putInt(this).array()

fun Int.toBytesLE(): ByteArray = ByteBufferUtils.littleEndian(4).putInt(this).array()

fun Int.toBytesBE(): ByteArray = ByteBufferUtils.bigEndian(4).putInt(this).array()

fun Long.toBytesLE(): ByteArray = ByteBufferUtils.littleEndian(8).putLong(this).array()

fun Long.toBytesBE(): ByteArray = ByteBufferUtils.bigEndian(8).putLong(this).array()

fun ByteArray.toBytesLE(): ByteArray = ByteBufferUtils.littleEndian(size).put(this).array()

fun ByteArray.trimLeadingBytes(b: Byte): ByteArray {
    var offset = 0
    while (offset < size - 1) {
        if (this[offset] != b) {
            break
        }
        offset++
    }
    return this.copyOfRange(offset, size)
}

fun ByteArray.trimLeadingZeroes(): ByteArray = this.trimLeadingBytes(0.toByte())

fun ByteArray.removeFromStart(count: Int): ByteArray = this.removeRange(0, count)

fun ByteArray.removeRange(startIndex: Int, count: Int): ByteArray {
    if (size <= count) return ByteArray(0)
    val newArray = ByteArray(size - count)
    System.arraycopy(this, 0, newArray, 0, startIndex)
    System.arraycopy(
        this,
        startIndex + count,
        newArray,
        startIndex,
        size - count - startIndex
    )
    return newArray
}

fun ByteArray.startsWith(startsWith: ByteArray): Boolean {
    if (size < startsWith.size) return false
    for (i in startsWith.indices) {
        if (this[i] != startsWith[i]) return false
    }
    return true
}

fun BigInteger.toBytes(numBytes: Int): ByteArray {
    val bytes = ByteArray(numBytes)
    val biBytes = this.toByteArray()
    val start = if (biBytes.size == numBytes + 1) 1 else 0
    val length = biBytes.size.coerceAtMost(numBytes)
    System.arraycopy(biBytes, start, bytes, numBytes - length, length)
    return bytes
}