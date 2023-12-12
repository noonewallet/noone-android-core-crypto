package io.noone.androidcore.hd

import io.noone.androidcore.ECKey
import io.noone.androidcore.exceptions.HDDerivationException
import io.noone.androidcore.utils.hmacSHA512
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.SecureRandom

// Some arbitrary random number. Doesn't matter what it is.
private val RAND_INT: BigInteger = BigInteger(256, SecureRandom())

@Throws(HDDerivationException::class)
internal fun DeterministicKey.deriveChildKeyBytesFromPrivate(
    childNumber: ChildNumber
): RawKeyBytes {
    require(this.hasPrivKey()) {
        "Parent key must have private key bytes for this method."
    }

    val parentPublicKey = this.pubKeyPoint.getEncoded(true)

    require(parentPublicKey.size == 33) {
        "Parent pubkey must be 33 bytes, but is " + parentPublicKey.size
    }

    val data = ByteBuffer.allocate(37)

    if (childNumber.isHardened) {
        data.put(this.privKeyBytes33)
    } else {
        data.put(parentPublicKey)
    }

    data.putInt(childNumber.i)

    val i = data.array().hmacSHA512(this.chainCode)

    require(i.size == 64) { i.size.toString() }

    val il = i.copyOfRange(0, 32)
    val chainCode = i.copyOfRange(32, 64)
    val ilInt = BigInteger(1, il)

    assertLessThanN(ilInt, "Illegal derived key: I_L >= n")

    val priv = this.privKey
    val ki = priv.add(ilInt).mod(ECKey.CURVE.n)

    assertNonZero(ki, "Illegal derived key: derived private key equals 0.")

    return RawKeyBytes(ki.toByteArray(), chainCode)
}

@Throws(HDDerivationException::class)
fun DeterministicKey.deriveChildKeyBytesFromPublic(
    childNumber: ChildNumber,
    mode: PublicDeriveMode
): RawKeyBytes {
    require(!childNumber.isHardened) {
        "Can't use private derivation with public keys only."
    }
    val parentPublicKey = this.pubKeyPoint.getEncoded(true)
    require(parentPublicKey.size == 33) {
        "Parent pubkey must be 33 bytes, but is " + parentPublicKey.size
    }

    val data = ByteBuffer.allocate(37)
    data.put(parentPublicKey)
    data.putInt(childNumber.i)

    val i = data.array().hmacSHA512(this.chainCode)

    require(i.size == 64) { i.size }

    val il = i.copyOfRange(0, 32)
    val chainCode = i.copyOfRange(32, 64)
    val ilInt = BigInteger(1, il)

    assertLessThanN(ilInt, "Illegal derived key: I_L >= n")

    val N = ECKey.CURVE.n
    var Ki: ECPoint
    when (mode) {
        PublicDeriveMode.NORMAL -> {
            Ki = ECKey.publicPointFromPrivate(ilInt).add(this.pubKeyPoint)
        }
        PublicDeriveMode.WITH_INVERSION -> {
            Ki = ECKey.publicPointFromPrivate(ilInt.add(RAND_INT).mod(N))
            val additiveInverse = RAND_INT.negate().mod(N)
            Ki = Ki.add(ECKey.publicPointFromPrivate(additiveInverse))
            Ki = Ki.add(this.pubKeyPoint)
        }
    }

    assertNonInfinity(Ki, "Illegal derived key: derived public key equals infinity.")

    return RawKeyBytes(Ki.getEncoded(true), chainCode)
}


internal fun assertNonZero(integer: BigInteger, errorMessage: String) {
    if (integer == BigInteger.ZERO)
        throw HDDerivationException(errorMessage)
}


internal fun assertLessThanN(integer: BigInteger, errorMessage: String) {
    if (integer > ECKey.CURVE.n)
        throw HDDerivationException(errorMessage)
}

internal fun assertNonInfinity(point: ECPoint, errorMessage: String) {
    if (point.equals(ECKey.CURVE.curve.infinity))
        throw HDDerivationException(errorMessage)
}
