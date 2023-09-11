package io.noone.androidcore

import io.noone.androidcore.exceptions.SignatureDecodeException
import io.noone.androidcore.utils.currentTimeSeconds
import io.noone.androidcore.utils.hex
import io.noone.androidcore.utils.sha256hash160
import io.noone.androidcore.utils.toByteArrayUnsigned
import io.noone.androidcore.utils.toBytes
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequenceGenerator
import org.bouncycastle.asn1.DLSequence
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.asn1.x9.X9IntegerConverter
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.bouncycastle.math.ec.FixedPointUtil
import org.bouncycastle.util.Properties
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.SecureRandom

open class ECKey {

    companion object {

        // The parameters of the secp256k1 curve that Bitcoin uses.
        val SECP256_K1_CURVE_PARAMS: X9ECParameters = CustomNamedCurves.getByName("secp256k1")
        val SECP256_R1_CURVE_PARAMS: X9ECParameters = CustomNamedCurves.getByName("secp256r1")

        /** The parameters of the secp256k1 curve that Bitcoin uses.  */
        val CURVE: ECDomainParameters

        /**
         * Equal to CURVE.getN().shiftRight(1)
         */
        val HALF_CURVE_ORDER: BigInteger

        private val SECURE_RANDOM: SecureRandom

        init {
            // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations.
            FixedPointUtil.precompute(SECP256_K1_CURVE_PARAMS.g)
            CURVE = ECDomainParameters(
                SECP256_K1_CURVE_PARAMS.curve, SECP256_K1_CURVE_PARAMS.g, SECP256_K1_CURVE_PARAMS.n,
                SECP256_K1_CURVE_PARAMS.h
            )
            HALF_CURVE_ORDER = SECP256_K1_CURVE_PARAMS.n.shiftRight(1)
            SECURE_RANDOM = SecureRandom()
        }

        fun compressPoint(point: ECPoint): ECPoint {
            return getPointWithCompression(point, true)
        }

        fun compressPoint(point: LazyECPoint): LazyECPoint {
            return /*if (point.isCompressed) */point //else LazyECPoint(compressPoint(point.get()))
        }

        private fun decompressKey(xBN: BigInteger, yBit: Boolean): ECPoint {
            val x9 = X9IntegerConverter()
            val compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.curve))
            compEnc[0] = (if (yBit) 0x03 else 0x02).toByte()
            return CURVE.curve.decodePoint(compEnc)
        }

        private fun getPointWithCompression(point: ECPoint, compressed: Boolean): ECPoint {
            var point = point
            /*if (point.isCompressed == compressed)
                return point*/
            point = point.normalize()
            val x = point.affineXCoord.toBigInteger()
            val y = point.affineYCoord.toBigInteger()
            return CURVE.curve.createPoint(x, y)
        }

        @JvmOverloads
        fun fromPrivate(privKey: BigInteger, compressed: Boolean = true): ECKey {
            val point = publicPointFromPrivate(privKey)
            return ECKey(privKey, getPointWithCompression(point, compressed))
        }

        fun fromPrivate(privKeyBytes: ByteArray): ECKey {
            return fromPrivate(BigInteger(1, privKeyBytes))
        }

        fun fromPrivate(privKeyBytes: ByteArray, compressed: Boolean): ECKey {
            return fromPrivate(BigInteger(1, privKeyBytes), compressed)
        }

        fun publicPointFromPrivate(privKey: BigInteger): ECPoint {
            //TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group order, but that could change in future versions.
            var localPrivKey = privKey
            if (localPrivKey.bitLength() > CURVE.n.bitLength()) {
                localPrivKey = localPrivKey.mod(CURVE.n)
            }
            return FixedPointCombMultiplier().multiply(CURVE.g, localPrivKey)
        }

        fun recoverPubBytesFromSignature(
            recId: Int, sig: ECDSASignature, messageHash: ByteArray
        ): ByteArray? {
            check(recId >= 0) { "recId must be positive" }
            check(sig.r.signum() >= 0) { "r must be positive" }
            check(sig.s.signum() >= 0) { "s must be positive" }
            val n = CURVE.n // Curve order.
            val i = BigInteger.valueOf(recId.toLong() / 2)
            val x = sig.r.add(i.multiply(n))

            val curve = CURVE.curve
            val prime = curve.field.characteristic
            if (x >= prime) {
                return null
            }

            val R: ECPoint = decompressKey(x, recId and 1 == 1)
            if (!R.multiply(n).isInfinity) {
                return null
            }

            val e = BigInteger(1, messageHash)
            val eInv = BigInteger.ZERO.subtract(e).mod(n)
            val rInv = sig.r.modInverse(n)
            val srInv = rInv.multiply(sig.s).mod(n)
            val eInvrInv = rInv.multiply(eInv).mod(n)
            val q = ECAlgorithms.sumOfTwoMultiplies(CURVE.g, eInvrInv, R, srInv)
            return q.getEncoded( /* compressed */false)
        }

        /**
         * Returns true if the given pubkey is canonical, i.e. the correct length taking into account compression.
         */
        fun isPubKeyCanonical(pubkey: ByteArray): Boolean {
            if (pubkey.size < 33)
                return false
            if (pubkey[0].toInt() == 0x04) {
                // Uncompressed pubkey
                if (pubkey.size != 65)
                    return false
            } else if (pubkey[0].toInt() == 0x02 || pubkey[0].toInt() == 0x03) {
                // Compressed pubkey
                if (pubkey.size != 33)
                    return false
            } else
                return false
            return true
        }
    }

    class MissingPrivateKeyException : RuntimeException()

    protected val priv: BigInteger?
    protected val pub: LazyECPoint

    protected open var creationTimeSeconds: Long = 0
        set(value) {
            require(value >= 0) { "Cannot set creation time to negative value: $value" }
            field = value
        }

    var pubKeyHash: ByteArray = byteArrayOf()
        get() = this.pub.encoded.sha256hash160
        private set

    open val isPubKeyOnly: Boolean
        get() = priv == null

    val pubKey: ByteArray
        get() = pub.encoded

    val pubKeyPoint: ECPoint
        get() = pub.get()

    open val privKey: BigInteger
        get() = priv ?: throw MissingPrivateKeyException()

    val isCompressed: Boolean
        get() = pub.isCompressed

    val privKeyBytes: ByteArray
        get() = privKey.toByteArrayUnsigned(32)

    val privateKeyAsHex: String
        get() = privKeyBytes.hex

    val publicKeyAsHex: String
        get() = pub.encoded.hex

    @JvmOverloads
    constructor(secureRandom: SecureRandom = SECURE_RANDOM) {
        val generator = ECKeyPairGenerator()
        val keygenParams = ECKeyGenerationParameters(CURVE, secureRandom)
        generator.init(keygenParams)
        val keypair = generator.generateKeyPair()
        val privParams = keypair.private as ECPrivateKeyParameters
        val pubParams = keypair.public as ECPublicKeyParameters
        priv = privParams.d
        pub = LazyECPoint(CURVE.curve, pubParams.q.getEncoded(true))
        creationTimeSeconds = currentTimeSeconds
    }

    protected constructor(priv: BigInteger?, pub: ECPoint) : this(
        priv,
        LazyECPoint(checkNotNull<ECPoint>(pub))
    ) {
    }

    protected constructor(priv: BigInteger?, pub: LazyECPoint) {
        if (priv != null) {
            require(priv.bitLength() <= 32 * 8) {
                "private key exceeds 32 bytes: %s bits".format(priv.bitLength())
            }
            require(priv != BigInteger.ZERO)
            require(priv != BigInteger.ONE)
        }
        this.priv = priv
        this.pub = pub
    }

    open fun hasPrivKey(): Boolean = priv != null

    /**
     * Groups the two components that make up a signature, and provides a way to encode to DER form, which is
     * how ECDSA signatures are represented when embedded in other data structures in the Bitcoin protocol. The raw
     * components can be useful for doing further EC maths on them.
     */
    class ECDSASignature(val r: BigInteger, val s: BigInteger) {

        var v: Byte = 0

        fun toByteArray(): ByteArray {
            val fixedV = if (this.v >= 27) { (this.v - 27) } else this.v
            return this.r.toBytes(32) + s.toBytes(32) + byteArrayOf(fixedV.toByte())
        }

        /**
         * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
         * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
         * the same message.
         */
        fun toCanonicalised(): ECDSASignature {
            return if (s > HALF_CURVE_ORDER) {
                ECDSASignature(r, CURVE.n.subtract(s))
            } else {
                this
            }
        }

        /**
         * DER is an international standard for serializing data structures which is widely used in cryptography.
         */
        fun encodeToDER(): ByteArray {
            try {
                val bos = ByteArrayOutputStream(72)
                val seq = DERSequenceGenerator(bos)
                seq.addObject(ASN1Integer(r))
                seq.addObject(ASN1Integer(s))
                seq.close()
                return bos.toByteArray()
            } catch (e: IOException) {
                throw RuntimeException(e)  // Cannot happen.
            }

        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val castedOther = (other as? ECDSASignature) ?: return false
            return r == castedOther.r && s == castedOther.s
        }

        override fun hashCode(): Int {
            var hash = 17
            hash = hash * 23 + r.hashCode()
            hash = hash * 23 + s.hashCode()
            return hash
        }

        companion object {

            @Throws(SignatureDecodeException::class)
            fun decodeFromDER(bytes: ByteArray): ECDSASignature {
                var decoder: ASN1InputStream? = null
                try {
                    Properties.setThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer", true)
                    decoder = ASN1InputStream(bytes)
                    val seqObj = decoder.readObject()
                        ?: throw SignatureDecodeException("Reached past end of ASN.1 stream.")
                    if (seqObj !is DLSequence)
                        throw SignatureDecodeException("Read unexpected class: " + seqObj.javaClass.name)
                    val r: ASN1Integer
                    val s: ASN1Integer
                    try {
                        r = seqObj.getObjectAt(0) as ASN1Integer
                        s = seqObj.getObjectAt(1) as ASN1Integer
                    } catch (e: ClassCastException) {
                        throw SignatureDecodeException(e)
                    }

                    return ECDSASignature(r.positiveValue, s.positiveValue)
                } catch (e: IOException) {
                    throw SignatureDecodeException(e)
                } finally {
                    if (decoder != null)
                        try {
                            decoder.close()
                        } catch (x: IOException) {
                        }

                    Properties.removeThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer")
                }
            }
        }
    }

    /**
     * Signs the given hash and returns the R and S components as BigIntegers.
     */
    fun sign(input: ByteArray): ECDSASignature {
        require(input.size == 32)
        if (priv == null)
            throw MissingPrivateKeyException()

        val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
        val privKey = ECPrivateKeyParameters(priv, CURVE)
        signer.init(true, privKey)
        val components = signer.generateSignature(input)
        return ECDSASignature(components[0], components[1]).toCanonicalised()
    }

    fun signWithV(messageHash: ByteArray): ECDSASignature {
        val sig: ECDSASignature = sign(messageHash)
        var recId = -1
        val thisKey: ByteArray = this.pub.getEncoded( /* compressed */false)
        for (i in 0..3) {
            val k: ByteArray? = recoverPubBytesFromSignature(i, sig, messageHash)
            if (k != null && k.contentEquals(thisKey)) {
                recId = i
                break
            }
        }
        if (recId == -1) {
            throw RuntimeException("Could not construct a recoverable key. This should never happen.")
        }
        sig.v = (recId + 27).toByte()
        return sig
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is ECKey) return false
        return this.priv?.equals(other.priv) != false &&
                this.pub == other.pub &&
                this.creationTimeSeconds == other.creationTimeSeconds
    }

    override fun hashCode(): Int = pub.hashCode()

}