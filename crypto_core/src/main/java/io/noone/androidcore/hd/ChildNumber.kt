package io.noone.androidcore.hd


import io.noone.androidcore.utils.toBytesBE
import java.util.Locale

class ChildNumber : Comparable<ChildNumber> {


    companion object {

        const val HARDENED_BIT = -0x80000000

        val ZERO = ChildNumber(0)
        val ZERO_HARDENED = ChildNumber(0, true)
        val ONE = ChildNumber(1)
        val ONE_HARDENED = ChildNumber(1, true)

        private fun hasHardenedBit(a: Int): Boolean {
            return a and HARDENED_BIT != 0
        }
    }

    val sequence: ByteArray
        get() = if (isHardened) {
            i or HARDENED_BIT
        } else {
            i
        }
            .toBytesBE()

    /** uint32 encoded form of the path element, including the most significant bit.  */
    val i: Int

    val isHardened: Boolean
        get() = hasHardenedBit(i)

    constructor(childNumber: Int, isHardened: Boolean) {
        require(!hasHardenedBit(childNumber)) { "Most significant bit is reserved and shouldn't be set: $childNumber" }
        i = if (isHardened) childNumber or HARDENED_BIT else childNumber
    }

    constructor(i: Int) {
        this.i = i
    }

    /** Returns the child number without the hardening bit set (i.e. index in that part of the tree).  */
    fun num(): Int = i and HARDENED_BIT.inv()

    override fun toString(): String {
        return String.format(Locale.US, "%d%s", num(), if (isHardened) "H" else "")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other == null || javaClass != other.javaClass) false else i == (other as ChildNumber).i
    }

    override fun hashCode(): Int = i

    override fun compareTo(other: ChildNumber): Int = this.num().compareTo(other.num())

}