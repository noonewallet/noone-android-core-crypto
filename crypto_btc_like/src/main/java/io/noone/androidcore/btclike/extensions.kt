package io.noone.androidcore.btclike

import io.noone.androidcore.btclike.networks.NetworkParameters
import io.noone.androidcore.hd.DeterministicKey
import io.noone.androidcore.utils.Base58
import io.noone.androidcore.utils.sha256sha256
import io.noone.androidcore.utils.toBytesBE
import io.noone.androidcore.utils.toBytesLE

fun DeterministicKey.getExtendedPublicKey(netParams: NetworkParameters): String {
    val data = ByteArray(78)
    var cursor = 0
    netParams.slip32PubKeyPrefix.copyInto(data)
    cursor += netParams.slip32PubKeyPrefix.size

    data[4] = depth.toByte()
    cursor += 1

    parentFingerprint.toBytesBE().let {
        it.copyInto(data, cursor)
        cursor += it.size
    }

    this.sequence.toBytesLE().let {
        it.copyInto(data, cursor)
        cursor += it.size
    }

    chainCode.copyInto(data, cursor)
    cursor += chainCode.size

    pubKey.copyInto(data, cursor)
    cursor += pubKey.size

    val checksum = data.sha256sha256
    val output = ByteArray(data.size + 4)

    data.copyInto(output)
    checksum.copyInto(output, data.size, 0, 4)

    return Base58.encode(output)
}

fun DeterministicKey.getExtendedPrivateKey(netParams: NetworkParameters): String {
    val data = ByteArray(78)
    var cursor = 0
    netParams.slip32PrivKeyPrefix.copyInto(data)
    cursor += netParams.slip32PrivKeyPrefix.size

    data[4] = depth.toByte()
    cursor += 1

    parentFingerprint.toBytesBE().let {
        it.copyInto(data, cursor)
        cursor += it.size
    }

    this.sequence.toBytesLE().let {
        it.copyInto(data, cursor)
        cursor += it.size
    }

    chainCode.copyInto(data, cursor)
    cursor += chainCode.size

    privKeyBytes33.copyInto(data, cursor)
    cursor += privKeyBytes33.size

    val checksum = data.sha256sha256
    val output = ByteArray(data.size + 4)

    data.copyInto(output)
    checksum.copyInto(output, data.size, 0, 4)

    return Base58.encode(output)
}