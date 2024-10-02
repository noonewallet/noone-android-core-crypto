package io.noone.androidcore.btclike.addresses

import io.noone.androidcore.btclike.networks.NetworkParameters
import io.noone.androidcore.btclike.transaction.ScriptType
import java.util.*

abstract class Address(
    @field:Transient
    protected val params: NetworkParameters,
    protected val bytes: ByteArray
) {

    abstract val hash: ByteArray

    abstract val outputScriptType: ScriptType

    override fun hashCode(): Int {
        var hash = 17
        hash = hash * 23 + params.hashCode()
        hash = hash * 23 + bytes.hashCode()
        return hash
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val other = o as Address?
        return this.params == other!!.params && Arrays.equals(this.bytes, other.bytes)
    }
}
