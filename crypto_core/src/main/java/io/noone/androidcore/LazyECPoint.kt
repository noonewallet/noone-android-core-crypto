package io.noone.androidcore


import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.util.Arrays

/**
 * A wrapper around ECPoint that delays decoding of the point for as long as possible. This is useful because point
 * encode/decode in Bouncy Castle is quite slow especially on Dalvik, as it often involves decompression/recompression.
 */
class LazyECPoint {

    private val curve: ECCurve?
    private val bits: ByteArray?

    private var point: ECPoint? = null
    private var compressed: Boolean = false

    val encoded: ByteArray
        get() = if (bits != null)
            Arrays.copyOf(bits, bits.size)
        else
            get().getEncoded(compressed)

    val isCompressed: Boolean
        get() = if (bits != null)
            bits[0].toInt() == 2 || bits[0].toInt() == 3
        else {
            get()
            compressed
        }

    val isValid: Boolean
        get() = get().isValid

    private val canonicalEncoding: ByteArray
        get() = getEncoded(true)

    constructor(curve: ECCurve, bits: ByteArray) {
        this.curve = curve
        this.bits = bits
    }

    constructor(point: ECPoint, compressed: Boolean) {
        this.point = checkNotNull(point)
        this.compressed = compressed
        this.curve = null
        this.bits = null
    }

    fun get(): ECPoint {
        if (point == null)
            point = curve?.decodePoint(bits!!)
        return point!!
    }

    fun multiply(k: BigInteger): ECPoint {
        return get().multiply(k)
    }

    fun equals(other: ECPoint): Boolean {
        return get().equals(other)
    }

    fun getEncoded(compressed: Boolean): ByteArray {
        return if (compressed == isCompressed && bits != null)
            Arrays.copyOf(bits, bits.size)
        else
            get().getEncoded(compressed)
    }

    fun add(b: ECPoint): ECPoint {
        return get().add(b)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        return if (o == null || javaClass != o.javaClass) false else canonicalEncoding.contentEquals((o as LazyECPoint).canonicalEncoding)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(canonicalEncoding)
    }
}
